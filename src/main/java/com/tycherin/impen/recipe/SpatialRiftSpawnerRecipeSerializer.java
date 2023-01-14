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

public class SpatialRiftSpawnerRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<SpatialRiftSpawnerRecipe> {

    public static final SpatialRiftSpawnerRecipeSerializer INSTANCE = new SpatialRiftSpawnerRecipeSerializer();

    private SpatialRiftSpawnerRecipeSerializer() {
    }

    @Override
    public SpatialRiftSpawnerRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
        final Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
        final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
        final int fuelCost = GsonHelper.getAsInt(json, "fuel_cost");
        return new SpatialRiftSpawnerRecipe(recipeId, input, output, fuelCost);
    }

    @Nullable
    @Override
    public SpatialRiftSpawnerRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
        final Ingredient input = Ingredient.fromNetwork(buffer);
        final ItemStack output = buffer.readItem();
        final int fuelCost = buffer.readInt();
        return new SpatialRiftSpawnerRecipe(recipeId, input, output, fuelCost);
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buffer, final SpatialRiftSpawnerRecipe recipe) {
        recipe.getInput().toNetwork(buffer);
        buffer.writeItemStack(recipe.getResultItem(), true);
        buffer.writeInt(recipe.getFuelCost());
    }
}
