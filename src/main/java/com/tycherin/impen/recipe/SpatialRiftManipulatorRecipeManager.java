package com.tycherin.impen.recipe;

import java.util.Optional;
import java.util.stream.Stream;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.item.SpatialRiftCellItem;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpatialRiftManipulatorRecipeManager {

    public static Optional<? extends SpatialRiftManipulatorRecipe> getRecipe(final Level level,
            final ItemStack topInput, final ItemStack bottomInput) {
        Stream<? extends SpatialRiftManipulatorRecipe> stream = level.getRecipeManager()
                .getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_RECIPE_TYPE.get())
                .stream()
                .filter(recipe -> recipe.getBottomInput().test(bottomInput));

        if (topInput.getItem() instanceof SpatialRiftCellItem) {
            // We know this will only match a SpatialRiftEffectRecipe or SpecialEffectRecipe, so we don't need to do any
            // additional filtering
        }
        else {
            // This is a generic recipe, so we need to check the top input as well
            stream = stream.filter(recipe -> (recipe instanceof SpatialRiftManipulatorRecipe.GenericManipulatorRecipe))
                    .map(recipe -> (SpatialRiftManipulatorRecipe.GenericManipulatorRecipe)recipe)
                    .filter(recipe -> {
                        return recipe.getTopInput().test(topInput);
                    });
        }

        return stream.findFirst();
    }
}
