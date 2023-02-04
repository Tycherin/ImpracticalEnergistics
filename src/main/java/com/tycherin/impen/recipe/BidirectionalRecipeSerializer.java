package com.tycherin.impen.recipe;

import com.google.gson.JsonObject;

import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Extension of the RecipeSerializer interface that can convert to and from JSON. Used for datagenning custom recipe
 * types.
 */
public interface BidirectionalRecipeSerializer<T extends BidirectionalRecipe<?>> extends RecipeSerializer<T> {
    /** Serializes the given recipe into the given JSON object */
    void toJson(T recipe, JsonObject json);
}
