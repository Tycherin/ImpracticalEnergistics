package com.tycherin.impen.recipe;

import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpatialRiftManipulatorRecipeManager {

    public static Optional<SpatialRiftManipulatorRecipe> getRecipe(final Level level, final ItemStack topInput,
            final ItemStack bottomInput) {
        return level.getRecipeManager().getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_RECIPE_TYPE.get())
                .stream()
                .filter(recipe -> recipe.getTopInput().equals(topInput, false)
                        && recipe.getBottomInput().test(bottomInput))
                .findFirst();
    }

    public static Optional<SpatialRiftManipulatorRecipe> getRecipe(final Level level, final String id) {
        return level.getRecipeManager().getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_RECIPE_TYPE.get())
                .stream()
                .filter(recipe -> recipe.getId().toString().equals(id))
                .findFirst();
    }
}
