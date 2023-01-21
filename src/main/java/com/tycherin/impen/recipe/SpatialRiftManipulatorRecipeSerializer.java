package com.tycherin.impen.recipe;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe.SpecialSpatialRecipe.SpecialSpatialRecipeType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SpatialRiftManipulatorRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<SpatialRiftManipulatorRecipe> {

    public static final SpatialRiftManipulatorRecipeSerializer INSTANCE = new SpatialRiftManipulatorRecipeSerializer();

    private static final char GENERIC_RECIPE_FLAG = 'g';
    private static final char SPATIAL_RECIPE_FLAG = 's';

    private SpatialRiftManipulatorRecipeSerializer() {
    }

    @Override
    public SpatialRiftManipulatorRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
        final Ingredient bottomInput = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "bottom_input"));

        if (json.has("spatial_effect")) {
            final JsonObject spatialJson = GsonHelper.getAsJsonObject(json, "spatial_effect");
            if (spatialJson.has("block")) {
                final Block block = getAsBlock(spatialJson);
                return new SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe(recipeId, bottomInput, block);                
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
            return new SpatialRiftManipulatorRecipe.GenericManipulatorRecipe(recipeId, topInput, bottomInput, output);
        }
    }

    @Nullable
    @Override
    public SpatialRiftManipulatorRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
        final Ingredient bottomInput = Ingredient.fromNetwork(buffer);

        final char typeFlag = buffer.readChar();

        if (typeFlag == SPATIAL_RECIPE_FLAG) {
            final Block block = ForgeRegistries.BLOCKS.getValue(buffer.readRegistryId());
            return new SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe(recipeId, bottomInput, block);
        }
        else {
            final Ingredient topInput = Ingredient.fromNetwork(buffer);
            final ItemStack output = buffer.readItem();
            return new SpatialRiftManipulatorRecipe.GenericManipulatorRecipe(recipeId, topInput, bottomInput, output);
        }
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buffer, final SpatialRiftManipulatorRecipe recipe) {
        recipe.getBottomInput().toNetwork(buffer);

        if (recipe instanceof SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe spatialRecipe) {
            buffer.writeChar(SPATIAL_RECIPE_FLAG);
            buffer.writeRegistryId(spatialRecipe.getBlock());
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

    private Block getAsBlock(final JsonObject json) {
        final String name = GsonHelper.getAsString(json, "block");
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
