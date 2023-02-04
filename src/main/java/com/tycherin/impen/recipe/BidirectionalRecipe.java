package com.tycherin.impen.recipe;

import com.google.gson.JsonObject;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;

public interface BidirectionalRecipe<C extends Container> extends Recipe<C> {
    /** Serializes the recipe's data into the provided JSON object */
    void serializeRecipeData(JsonObject json);

    /** Gets the registry name of the recipe type for this recipe */
    String getRecipeTypeName();
}
