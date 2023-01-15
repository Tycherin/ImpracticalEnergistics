package com.tycherin.impen.recipe;

import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.item.RiftedSpatialCellItem;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpatialRiftManipulatorRecipeManager {

    public static Optional<? extends SpatialRiftManipulatorRecipe> getRecipe(final Level level,
            final ItemStack topInput, final ItemStack bottomInput) {
        var stream = level.getRecipeManager().getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_RECIPE_TYPE.get())
                .stream();

        if (topInput.getItem() instanceof RiftedSpatialCellItem) {
            return stream.filter(recipe -> (recipe instanceof SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe))
                    .map(recipe -> (SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe)recipe)
                    .filter(recipe -> recipe.getBottomInput().test(bottomInput))
                    .findFirst();
        }
        else {
            return stream.filter(recipe -> (recipe instanceof SpatialRiftManipulatorRecipe.GenericManipulatorRecipe))
                    .map(recipe -> (SpatialRiftManipulatorRecipe.GenericManipulatorRecipe)recipe)
                    .filter(recipe -> {
                        return recipe.getTopInput().test(topInput)
                                && recipe.getBottomInput().test(bottomInput);
                    })
                    .findFirst();
        }
    }
}
