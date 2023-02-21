package com.tycherin.impen.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.util.RegistryUtil;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistryEntry;

@Slf4j
@Getter
@RequiredArgsConstructor
public class SpatialRiftBaseRecipe implements SpecialRecipe {

    public static final String RECIPE_TYPE_NAME = "spatial_rift_base";

    private final ResourceLocation id;
    private final Block block;
    private final Map<Block, Integer> baseWeights;
    private final Item ingredient;
    private final int ingredientCount;

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.SPATIAL_RIFT_BASE_RECIPE_TYPE.get();
    }

    @Data
    @Builder
    public static class RecipeData {
        private final String recipeName;
        private final String baseBlockId;
        private final String ingredientId;
        private final Integer ingredientCount;
        private final Map<String, Integer> baseWeights;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements RecipeSerializer<SpatialRiftBaseRecipe> {

        public static final SpatialRiftBaseRecipe.Serializer INSTANCE = new SpatialRiftBaseRecipe.Serializer();

        private Serializer() {
        }

        private static class JsonFields {
            private static final String BASE_BLOCK = "base_block";
            private static final String ITEM = "item";
            private static final String COUNT = "count";
            private static final String INGREDIENT = "ingredient";
            private static final String BLOCK = "block";
            private static final String VALUE = "value";
            private static final String WEIGHTS = "weights";
        }

        public void toJson(final RecipeData data, final JsonObject json) {
            json.addProperty(JsonFields.BASE_BLOCK, data.baseBlockId);

            final JsonObject ingredientJson = new JsonObject();
            ingredientJson.addProperty(JsonFields.ITEM, data.ingredientId);
            ingredientJson.addProperty(JsonFields.COUNT, data.ingredientCount);
            json.add(JsonFields.INGREDIENT, ingredientJson);

            final JsonArray weightsJson = new JsonArray();
            data.baseWeights.forEach((blockId, value) -> {
                final JsonObject weightJson = new JsonObject();
                weightJson.addProperty(JsonFields.BLOCK, blockId);
                weightJson.addProperty(JsonFields.VALUE, value);
                weightsJson.add(weightJson);
            });
            json.add(JsonFields.WEIGHTS, weightsJson);
        }

        @Override
        public SpatialRiftBaseRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
            final Block block = RegistryUtil.getBlock(json, JsonFields.BASE_BLOCK);

            final JsonObject ingredientJson = GsonHelper.getAsJsonObject(json, JsonFields.INGREDIENT);
            final Item ingredient = RegistryUtil.getItem(ingredientJson);
            final Integer ingredientCount = GsonHelper.getAsInt(ingredientJson, JsonFields.COUNT);

            final JsonArray weightsJson = GsonHelper.getAsJsonArray(json, JsonFields.WEIGHTS);
            final Map<Block, Integer> weights = new HashMap<>();
            weightsJson.forEach(jsonElement -> {
                if (jsonElement instanceof JsonObject jsonObject) {
                    final String blockId = GsonHelper.getAsString(jsonObject, JsonFields.BLOCK);
                    final Optional<Block> blockOpt = RegistryUtil.getBlockOptional(blockId);
                    if (blockOpt.isEmpty()) {
                        // The most likely cause here is that the block is from a mod that isn't present
                        log.debug("Ignoring missing block: {}", blockId);
                    }
                    else {
                        final Integer value = GsonHelper.getAsInt(jsonObject, JsonFields.VALUE);
                        weights.put(blockOpt.get(), value);
                    }
                }
                else {
                    log.warn("Ignoring malformed list element: {}", jsonElement);
                }
            });

            return new SpatialRiftBaseRecipe(recipeId, block, weights, ingredient, ingredientCount);
        }

        @Override
        public void toNetwork(final FriendlyByteBuf buffer, final SpatialRiftBaseRecipe recipe) {
            buffer.writeRegistryId(recipe.block);

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
        public SpatialRiftBaseRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
            final Block baseBlock = buffer.readRegistryIdSafe(Block.class);

            final Item ingredient = buffer.readRegistryIdSafe(Item.class);
            final Integer ingredientCount = buffer.readInt();

            final int weightCount = buffer.readByte();
            final Map<Block, Integer> baseWeights = new HashMap<>();
            for (int i = 0; i < weightCount; i++) {
                baseWeights.put(
                        buffer.readRegistryIdSafe(Block.class),
                        buffer.readInt());
            }

            return new SpatialRiftBaseRecipe(recipeId, baseBlock, baseWeights, ingredient, ingredientCount);
        }
    }
}
