package com.tycherin.impen.recipe;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SpatialRiftCollapserRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<SpatialRiftCollapserRecipe> {

    public static final SpatialRiftCollapserRecipeSerializer INSTANCE = new SpatialRiftCollapserRecipeSerializer();

    private SpatialRiftCollapserRecipeSerializer() {
    }

    @Override
    public SpatialRiftCollapserRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
        final Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
        final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
        return new SpatialRiftCollapserRecipe(recipeId, input, output);
    }

    @Nullable
    @Override
    public SpatialRiftCollapserRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
        final Ingredient input = Ingredient.fromNetwork(buffer);
        final ItemStack output = buffer.readItem();
        return new SpatialRiftCollapserRecipe(recipeId, input, output);
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buffer, final SpatialRiftCollapserRecipe recipe) {
        recipe.getInput().toNetwork(buffer);
        buffer.writeItemStack(recipe.getResultItem(), true);
    }
}
