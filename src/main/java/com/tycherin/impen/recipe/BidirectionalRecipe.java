package com.tycherin.impen.recipe;

import com.google.gson.JsonObject;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;

public interface BidirectionalRecipe extends Recipe<Container> {
    /** Serializes the recipe's data into the provided JSON object */
    void serializeRecipeData(final JsonObject json);

    /** Gets the registry name of the recipe type for this recipe */
    String getRecipeTypeName();
}
