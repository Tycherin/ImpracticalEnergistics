package com.tycherin.impen.recipe;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe.SpecialSpatialRecipe.SpecialSpatialRecipeType;

import appeng.datagen.providers.recipes.AE2RecipeProvider;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SpatialRiftManipulatorRecipe implements BidirectionalRecipe<Container> {

    public static final String RECIPE_TYPE_NAME = "spatial_rift_manipulator";

    private final ResourceLocation id;

    @Getter
    protected final Ingredient bottomInput;

    public SpatialRiftManipulatorRecipe(final ResourceLocation id, @NonNull final Ingredient bottomInput) {
        this.id = id;
        this.bottomInput = bottomInput;
    }

    @Override
    public void serializeRecipeData(final JsonObject json) {
        this.getSerializer().toJson(this, json);
    }

    // ***
    // Recipe boilerplate
    // ***

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public SpatialRiftManipulatorRecipe.Serializer getSerializer() {
        return SpatialRiftManipulatorRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_RECIPE_TYPE.get();
    }

    @Override
    public String getRecipeTypeName() {
        return RECIPE_TYPE_NAME;
    }

    /**
     * Normal crafting recipe for Spatial Rift Manipulator, one that doesn't operate on Spatial Rift Cells
     */
    @Getter
    public static class GenericManipulatorRecipe extends SpatialRiftManipulatorRecipe {
        private final Ingredient topInput;
        private final ItemStack output;

        public GenericManipulatorRecipe(final ResourceLocation id, @NonNull final Ingredient topInput,
                @NonNull final Ingredient bottomInput, @NonNull final ItemStack output) {
            super(id, bottomInput);
            this.topInput = topInput;
            this.output = output;
        }
    }

    /**
     * Recipe that manipulates the values inside a Spatial Rift Cell
     * <p>
     * The input for this recipe is always some type of Spatial Rift Cell, and the output is the same cell with modified
     * properties
     */
    @Getter
    public static class SpatialRiftEffectRecipe extends SpatialRiftManipulatorRecipe {
        private final Block block;
        private final Block baseBlock;

        public SpatialRiftEffectRecipe(final ResourceLocation id, @NonNull final Ingredient bottomInput,
                @NonNull final Block block, @NonNull final Block baseBlock) {
            super(id, bottomInput);
            this.block = block;
            this.baseBlock = baseBlock;
        }
    }

    /**
     * There are a couple of recipes that manipulate rift cells, but don't have an associated block, and therefore need
     * special handling
     */
    @Getter
    public static class SpecialSpatialRecipe extends SpatialRiftManipulatorRecipe {

        public static enum SpecialSpatialRecipeType {
            CLEAR_INPUTS,
            BOOST_PRECISION
        }

        private final SpecialSpatialRecipeType specialType;

        public SpecialSpatialRecipe(final ResourceLocation id, @NonNull final Ingredient bottomInput,
                @NonNull final SpecialSpatialRecipeType specialType) {
            super(id, bottomInput);
            this.specialType = specialType;
        }
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements BidirectionalRecipeSerializer<SpatialRiftManipulatorRecipe> {

        public static final SpatialRiftManipulatorRecipe.Serializer INSTANCE = new SpatialRiftManipulatorRecipe.Serializer();

        private static final char GENERIC_RECIPE_FLAG = 'g';
        private static final char SPATIAL_RECIPE_FLAG = 's';

        private Serializer() {
        }

        @Override
        public SpatialRiftManipulatorRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
            final Ingredient bottomInput = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "bottom_input"));

            if (json.has("spatial_effect")) {
                final JsonObject spatialJson = GsonHelper.getAsJsonObject(json, "spatial_effect");
                if (spatialJson.has("block")) {
                    final Block block = getAsBlock(spatialJson, "block");
                    final Block baseBlock = getAsBlock(spatialJson, "baseBlock");
                    return new SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe(recipeId, bottomInput, block,
                            baseBlock);
                }
                else if (spatialJson.has("special_effect")) {
                    final var type = SpecialSpatialRecipeType.valueOf(spatialJson.get("special_effect").getAsString());
                    return new SpatialRiftManipulatorRecipe.SpecialSpatialRecipe(recipeId, bottomInput, type);
                }
                else {
                    throw new RuntimeException("Unknown spatial effect type for " + recipeId);
                }
            }
            else {
                final Ingredient topInput = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "top_input"));
                final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
                return new SpatialRiftManipulatorRecipe.GenericManipulatorRecipe(recipeId, topInput, bottomInput,
                        output);
            }
        }

        @Override
        public void toJson(final SpatialRiftManipulatorRecipe recipe, final JsonObject json) {
            json.add("bottom_input", recipe.bottomInput.toJson());
            if (recipe instanceof SpatialRiftManipulatorRecipe.GenericManipulatorRecipe genericRecipe) {
                json.add("top_input", genericRecipe.topInput.toJson());
                json.add("output", AE2RecipeProvider.toJson(genericRecipe.output));
            }
            else if (recipe instanceof SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe spatialRecipe) {
                final JsonObject spatialJson = new JsonObject();
                spatialJson.addProperty("block", spatialRecipe.block.getRegistryName().toString());
                spatialJson.addProperty("baseBlock", spatialRecipe.baseBlock.getRegistryName().toString());
                json.add("spatial_effect", spatialJson);
            }
            else if (recipe instanceof SpatialRiftManipulatorRecipe.SpecialSpatialRecipe specialRecipe) {
                final JsonObject spatialJson = new JsonObject();
                spatialJson.addProperty("special_effect", specialRecipe.specialType.toString());
                json.add("spatial_effect", spatialJson);
            }
        }

        @Nullable
        @Override
        public SpatialRiftManipulatorRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
            final Ingredient bottomInput = Ingredient.fromNetwork(buffer);

            final char typeFlag = buffer.readChar();

            if (typeFlag == SPATIAL_RECIPE_FLAG) {
                final Block block = ForgeRegistries.BLOCKS.getValue(buffer.readRegistryId());
                final Block baseBlock = ForgeRegistries.BLOCKS.getValue(buffer.readRegistryId());
                return new SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe(recipeId, bottomInput, block,
                        baseBlock);
            }
            else {
                final Ingredient topInput = Ingredient.fromNetwork(buffer);
                final ItemStack output = buffer.readItem();
                return new SpatialRiftManipulatorRecipe.GenericManipulatorRecipe(recipeId, topInput, bottomInput,
                        output);
            }
        }

        @Override
        public void toNetwork(final FriendlyByteBuf buffer, final SpatialRiftManipulatorRecipe recipe) {
            recipe.getBottomInput().toNetwork(buffer);

            if (recipe instanceof SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe spatialRecipe) {
                buffer.writeChar(SPATIAL_RECIPE_FLAG);
                buffer.writeRegistryId(spatialRecipe.getBlock());
                buffer.writeRegistryId(spatialRecipe.getBaseBlock());
            }
            else if (recipe instanceof SpatialRiftManipulatorRecipe.GenericManipulatorRecipe genericRecipe) {
                buffer.writeChar(GENERIC_RECIPE_FLAG);
                genericRecipe.getTopInput().toNetwork(buffer);
                buffer.writeItemStack(genericRecipe.getOutput(), true);
            }
            else {
                throw new RuntimeException("Unrecognized recipe type " + recipe);
            }
        }

        private Block getAsBlock(final JsonObject json, final String jsonKey) {
            final String name = GsonHelper.getAsString(json, jsonKey);
            final ResourceLocation key = new ResourceLocation(name);
            if (!ForgeRegistries.BLOCKS.containsKey(key)) {
                throw new JsonSyntaxException(String.format("Unknown block '%s'", key));
            }

            final Block block = ForgeRegistries.BLOCKS.getValue(key);
            if (block == Blocks.AIR) {
                throw new JsonSyntaxException(String.format("Invalid block '%s'", key));
            }

            return Objects.requireNonNull(block);
        }
    }
}
