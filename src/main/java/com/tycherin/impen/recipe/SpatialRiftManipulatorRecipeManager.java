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
                .filter(recipe -> ItemStack.isSame(recipe.getTopInput(), topInput)
                        && recipe.getBottomInput().test(bottomInput))
                .findFirst();
    }
}
