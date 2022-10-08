package com.tycherin.impen.recipe;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SpatialCrystallizerRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<SpatialCrystallizerRecipe> {

    public static final SpatialCrystallizerRecipeSerializer INSTANCE = new SpatialCrystallizerRecipeSerializer();

    private SpatialCrystallizerRecipeSerializer() {
    }

    @Override
    public SpatialCrystallizerRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
        // Ideally we would validate that the dimension exists here, but unfortunately, the dimension registry isn't
        // available at the time when recipes are loaded
        // TODO Consider some sort of post-load plugin to remove invalid recipes
        final ResourceLocation dimensionKey = new ResourceLocation(GsonHelper.getAsString(json, "dimension"));
        final ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
        return new SpatialCrystallizerRecipe(recipeId, dimensionKey, result);
    }

    @Nullable
    @Override
    public SpatialCrystallizerRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
        final ResourceLocation dimensionKey = buffer.readResourceLocation();
        final ItemStack result = buffer.readItem();
        return new SpatialCrystallizerRecipe(recipeId, dimensionKey, result);
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buffer, final SpatialCrystallizerRecipe recipe) {
        buffer.writeResourceLocation(recipe.getDimensionKey());
        buffer.writeItemStack(recipe.getResultItem(), false);
    }
}
