package com.tycherin.impen.recipe;

import com.google.gson.JsonObject;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;

public interface BidirectionalRecipe<C extends Container> extends Recipe<C> {
    void serializeRecipeData(JsonObject json);

    String getRecipeTypeName();
}
