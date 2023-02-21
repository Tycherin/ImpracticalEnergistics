package com.tycherin.impen.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tycherin.impen.ImpenRegistry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * A recipe manager class for SpatialRiftBaseRecipes. Since these are special recipes, the usual RecipeManager methods
 * don't work, so this class provides a more convenient interface for fetching them.
 */
public class SpatialRiftBaseRecipeManager {

    // Recipes are stored per level, so these need to be split out accordingly
    private static final Map<Level, SpatialRiftBaseRecipeManager> MANAGERS = new HashMap<>();

    public static Optional<SpatialRiftBaseRecipe> getRecipeForInput(final ItemStack is, final Level level) {
        return getManager(level).getRecipeForInput(is);
    }

    public static Optional<SpatialRiftBaseRecipe> getRecipeForBlock(final Block block, final Level level) {
        return getManager(level).getRecipeForBlock(block);
    }

    private static SpatialRiftBaseRecipeManager getManager(final Level level) {
        return MANAGERS.computeIfAbsent(level, (lvl) -> new SpatialRiftBaseRecipeManager(lvl));
    }

    private final Map<Item, SpatialRiftBaseRecipe> itemToRecipe;
    private final Map<Block, SpatialRiftBaseRecipe> blockToRecipe;

    private SpatialRiftBaseRecipeManager(final Level level) {
        final var recipes = level.getRecipeManager()
                .getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_BASE_RECIPE_TYPE.get());

        this.itemToRecipe = recipes.stream()
                .collect(Collectors.toMap(
                        SpatialRiftBaseRecipe::getIngredient,
                        Function.identity()));
        this.blockToRecipe = recipes.stream()
                .collect(Collectors.toMap(
                        SpatialRiftBaseRecipe::getBlock,
                        Function.identity()));
    }

    private Optional<SpatialRiftBaseRecipe> getRecipeForInput(final ItemStack is) {
        if (this.itemToRecipe.containsKey(is.getItem())) {
            final SpatialRiftBaseRecipe recipe = this.itemToRecipe.get(is.getItem());
            if (is.getCount() >= recipe.getIngredientCount()) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    private Optional<SpatialRiftBaseRecipe> getRecipeForBlock(final Block block) {
        return Optional.ofNullable(this.blockToRecipe.get(block));
    }
}
