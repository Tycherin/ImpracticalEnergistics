package com.tycherin.impen.datagen;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.tycherin.impen.recipe.BidirectionalRecipe;
import com.tycherin.impen.util.ImpenIdUtil;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CustomRecipeResult implements FinishedRecipe {

    private final String recipeName;
    private final BidirectionalRecipe recipe;

    public CustomRecipeResult(final String recipeName, final BidirectionalRecipe recipe) {
        if (recipeName == null) {
            throw new IllegalArgumentException("Recipe name cannot be null");
        }
        this.recipeName = recipeName;
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe cannot be null");
        }
        this.recipe = recipe;
    }

    @Override
    public void serializeRecipeData(final JsonObject json) {
        recipe.serializeRecipeData(json);
    }

    @Override
    public ResourceLocation getId() {
        return ImpenIdUtil.makeId(recipe.getRecipeTypeName() + "/" + recipeName);
    }

    @Override
    public RecipeSerializer<?> getType() {
        return recipe.getSerializer();
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return null;
    }
}