package com.tycherin.impen.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.item.SpatialRiftCellItem;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class SpatialRiftManipulatorRecipeManager {

    private static final Map<Level, Map<InputPair, Optional<? extends SpatialRiftManipulatorRecipe>>> RECIPE_CACHE = new HashMap<>();
    private static final Map<Level, Map<Block, SpatialRiftManipulatorBaseBlockRecipe>> BASE_BLOCK_CACHE = new HashMap<>();

    private static record InputPair(ItemStack topStack, ItemStack bottomStack) {
    }

    /**
     * Returns a recipe matching the specified inputs. Note that for empty inputs, this will return the first matching
     * recipe.
     * 
     * @return A recipe matching the specified inputs, or empty if none exists
     */
    public static Optional<? extends SpatialRiftManipulatorRecipe> getRecipe(final Level level,
            final ItemStack topStack, final ItemStack bottomStack) {
        if (!RECIPE_CACHE.containsKey(level)) {
            RECIPE_CACHE.put(level, new HashMap<>());
        }

        final var levelCache = RECIPE_CACHE.get(level);
        final InputPair input = new InputPair(topStack, bottomStack);
        if (levelCache.containsKey(input)) {
            return levelCache.get(input);
        }
        else {
            final Optional<? extends SpatialRiftManipulatorRecipe> recipeOpt = findRecipe(level, input);
            levelCache.put(input, recipeOpt);
            return recipeOpt;
        }
    }

    private static Optional<? extends SpatialRiftManipulatorRecipe> findRecipe(final Level level,
            final InputPair input) {
        final var recipeManager = level.getRecipeManager();

        final Ingredient spatialCell = SpatialRiftCellItem.getIngredient();
        Optional<? extends SpatialRiftManipulatorRecipe> recipeOpt = recipeManager
                .getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_BASE_BLOCK_RECIPE_TYPE.get())
                .stream()
                .filter(recipe -> {
                    return (input.topStack.isEmpty() || spatialCell.test(input.topStack))
                            && (input.bottomStack.isEmpty() ||
                                    (recipe.getIngredient().getItem().equals(input.bottomStack.getItem())
                                            && recipe.getIngredient().getCount() <= input.bottomStack.getCount()));
                })
                .findFirst();
        if (recipeOpt.isEmpty()) {
            recipeOpt = recipeManager
                    .getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_BLOCK_WEIGHT_RECIPE_TYPE.get())
                    .stream()
                    .filter(recipe -> {
                        return (input.topStack.isEmpty() || spatialCell.test(input.topStack))
                                && (input.bottomStack.isEmpty() || recipe.getBottomInput().test(input.bottomStack));
                    })
                    .findFirst();
        }
        if (recipeOpt.isEmpty()) {
            recipeOpt = recipeManager
                    .getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_SPECIAL_RECIPE_TYPE.get())
                    .stream()
                    .filter(recipe -> {
                        return (input.topStack.isEmpty() || spatialCell.test(input.topStack))
                                && (input.bottomStack.isEmpty() || recipe.getBottomInput().test(input.bottomStack));
                    })
                    .findFirst();
        }
        if (recipeOpt.isEmpty()) {
            recipeOpt = recipeManager
                    .getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_CRAFTING_RECIPE_TYPE.get())
                    .stream()
                    .filter(recipe -> {
                        return (input.topStack.isEmpty() || recipe.getTopInput().test(input.topStack))
                                && (input.bottomStack.isEmpty() || recipe.getBottomInput().test(input.bottomStack));
                    })
                    .findFirst();
        }
        return recipeOpt;
    }

    public static Optional<SpatialRiftManipulatorBaseBlockRecipe> getRecipe(final Level level, final Block baseBlock) {
        if (!BASE_BLOCK_CACHE.containsKey(level)) {
            buildBaseBlockCache(level);
        }
        return Optional.ofNullable(BASE_BLOCK_CACHE.get(level).get(baseBlock));
    }

    private static void buildBaseBlockCache(final Level level) {
        final Map<Block, SpatialRiftManipulatorBaseBlockRecipe> recipes = new HashMap<>();
        level.getRecipeManager().getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_BASE_BLOCK_RECIPE_TYPE.get())
                .forEach(recipe -> recipes.put(recipe.getBaseBlock(), recipe));
        BASE_BLOCK_CACHE.put(level, Map.copyOf(recipes));
    }
}
