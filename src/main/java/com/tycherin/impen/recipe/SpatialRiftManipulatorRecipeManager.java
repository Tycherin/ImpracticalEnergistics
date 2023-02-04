package com.tycherin.impen.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.item.SpatialRiftCellItem;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class SpatialRiftManipulatorRecipeManager {

    private static final Map<Block, Optional<SpatialRiftEffectRecipe>> RECIPE_CACHE = new HashMap<>();

    public static Optional<SpatialRiftEffectRecipe> getRecipe(final Level level,
            final Block input) {
        return RECIPE_CACHE.computeIfAbsent(input, (block) -> {
            return level.getRecipeManager()
                    .getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_RECIPE_TYPE.get())
                    .stream()
                    .filter(recipe -> recipe instanceof SpatialRiftEffectRecipe)
                    .map(SpatialRiftEffectRecipe.class::cast)
                    .filter(recipe -> recipe.getBlock().equals(input))
                    .findFirst();
        });
    }

    public static Optional<? extends SpatialRiftManipulatorRecipe> getRecipe(final Level level,
            final ItemStack topInput, final ItemStack bottomInput) {
        if (topInput.isEmpty() || bottomInput.isEmpty()) {
            return Optional.empty();
        }

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
