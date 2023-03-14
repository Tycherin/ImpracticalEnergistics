package com.tycherin.impen.recipe;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.annotate.NoSerialize;
import com.tycherin.impen.util.GsonUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

@Getter
@AllArgsConstructor
public class SpatialRiftManipulatorBaseBlockRecipe implements SpatialRiftManipulatorRecipe {

    // See docs for BaseWeightMapSerDe for an explanation of why this is needed
    private static final Gson GSON_INSTANCE = GsonUtil.getStandardGsonBuilder()
            .registerTypeAdapter(BaseWeightMap.class, new BaseWeightMapSerDe())
            .create();

    @NoSerialize
    private ResourceLocation id;
    /**
     * Base block to set. This block will be used as the default block for the plot.
     */
    private Block baseBlock;
    /**
     * How to read this map:
     * <ul>
     * <li>Block X should replace the base block a certain percentage of the time</li>
     * <li>The replacement rate is the value. 1 = 1 occurrence of Block X per 4000 blocks.</li>
     * <li>Replacement rates are independent of one another. In other words, the occurrence rate of each block is equal
     * to its value, and the occurrence rate of the base block is equal to 4000 minus the sum of all weights in the
     * map.</li>
     * <li>These are just the base rates; other parts of the Spatial Rift system will modify these before they are
     * applied to an actual plot.</li>
     * </ul>
     */
    private BaseWeightMap baseWeights;
    /**
     * Ingredient that sets this base block. In other words, if the SRM adds this item to an unformatted cell, that will
     * mark the cell as using this recipe.
     */
    private Item ingredient;
    /**
     * Number of the ingredient item to require for the SRM recipe.
     * <p>
     * There's probably a smarter way to do this, but here we are.
     */
    private int ingredientCount;

    @Override
    public Serializer getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_BASE_BLOCK_RECIPE_TYPE.get();
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements RecipeSerializer<SpatialRiftManipulatorBaseBlockRecipe> {

        public static final SpatialRiftManipulatorBaseBlockRecipe.Serializer INSTANCE = new SpatialRiftManipulatorBaseBlockRecipe.Serializer();

        private Serializer() {
        }

        @Override
        public SpatialRiftManipulatorBaseBlockRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
            final var recipe = GSON_INSTANCE.fromJson(json, SpatialRiftManipulatorBaseBlockRecipe.class);
            recipe.id = recipeId;
            return recipe;
        }

        @Override
        public void toNetwork(final FriendlyByteBuf buffer, final SpatialRiftManipulatorBaseBlockRecipe recipe) {
            buffer.writeRegistryId(recipe.baseBlock);

            buffer.writeRegistryId(recipe.ingredient);
            buffer.writeInt(recipe.ingredientCount);

            buffer.writeByte(recipe.baseWeights.size());
            recipe.baseWeights.forEach((block, value) -> {
                buffer.writeRegistryId(block);
                buffer.writeInt(value);
            });
        }

        @Nullable
        @Override
        public SpatialRiftManipulatorBaseBlockRecipe fromNetwork(final ResourceLocation recipeId,
                final FriendlyByteBuf buffer) {
            final Block baseBlock = buffer.readRegistryIdSafe(Block.class);

            final Item ingredient = buffer.readRegistryIdSafe(Item.class);
            final Integer ingredientCount = buffer.readInt();

            final int weightCount = buffer.readByte();
            final BaseWeightMap baseWeights = new BaseWeightMap();
            for (int i = 0; i < weightCount; i++) {
                baseWeights.put(
                        buffer.readRegistryIdSafe(Block.class),
                        buffer.readInt());
            }

            return new SpatialRiftManipulatorBaseBlockRecipe(recipeId, baseBlock, baseWeights, ingredient,
                    ingredientCount);
        }
    }

    /** Special named class type to make Gson serialization easy */
    public static class BaseWeightMap extends HashMap<Block, Integer> {
        private static final long serialVersionUID = -1563117414080514054L;
    }

    /**
     * Special serializer to enable non-existent blocks in the weights map without erroring
     * <p>
     * By default, Gson will attempt to serialize {@code Map<Block, Integer>} using a normal map. However, the input
     * JSON contains keys which might not map to actual blocks, which can happen when a key refers to a block from a mod
     * that isn't loaded. This breaks Gson, because either A) the Block serializer returns null, which is bad, or B) the
     * Block serializer returns {@code Blocks.AIR}, which tends to break if it appears more than once.
     * <p>
     * To avoid that problem, the simplest solution is to use this custom SerDe to explicitly handl the missing block
     * case.
     */
    private static class BaseWeightMapSerDe implements GsonUtil.GsonSerDe<BaseWeightMap> {
        @Override
        public JsonElement serialize(final BaseWeightMap src, final Type typeOfSrc,
                final JsonSerializationContext context) {
            // The default serialization behavior is fine, since the blocks are guaranteed to be "real" here
            return context.serialize(src, Map.class);
        }

        @Override
        public BaseWeightMap deserialize(final JsonElement json, final Type type,
                final JsonDeserializationContext context) throws JsonParseException {
            // Deserialization is a problem, because we need to tell Gson that keys which don't translate to real blocks
            // should be ignored, so we have to do this by hand
            final JsonObject mapJson = (JsonObject)json;

            final BaseWeightMap map = new BaseWeightMap();
            mapJson.entrySet().forEach(entry -> {
                final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.getKey()));
                if (block.equals(Blocks.AIR)) {
                    // This indicates that a block wasn't found, probably because it's a modded block that isn't present
                    // Skip it and move on
                    return;
                }
                final Integer weight = entry.getValue().getAsNumber().intValue();
                map.put(block, weight);
            });

            if (map.isEmpty()) {
                throw new JsonParseException("Expected blockWeight map to contain at least one block, but found none ("
                        + mapJson.entrySet().size() + " total entries)");
            }

            return map;
        }
    }
}
