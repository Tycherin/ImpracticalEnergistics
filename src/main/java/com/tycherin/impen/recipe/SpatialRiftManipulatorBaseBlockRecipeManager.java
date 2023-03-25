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
 * A recipe manager class for SpatialRiftBaseBlockRecipes. Since these are special recipes, the usual RecipeManager
 * methods don't work, so this class provides a more convenient interface for fetching them.
 */
public class SpatialRiftManipulatorBaseBlockRecipeManager {

    // Recipes are stored per level, so these need to be split out accordingly
    private static final Map<Level, SpatialRiftManipulatorBaseBlockRecipeManager> MANAGERS = new HashMap<>();

    public static Optional<SpatialRiftManipulatorBaseBlockRecipe> getRecipeForInput(final ItemStack is, final Level level) {
        return getManager(level).getRecipeForInput(is);
    }

    public static Optional<SpatialRiftManipulatorBaseBlockRecipe> getRecipeForBlock(final Block block, final Level level) {
        return getManager(level).getRecipeForBlock(block);
    }

    private static SpatialRiftManipulatorBaseBlockRecipeManager getManager(final Level level) {
        return MANAGERS.computeIfAbsent(level, (lvl) -> new SpatialRiftManipulatorBaseBlockRecipeManager(lvl));
    }

    private final Map<Item, SpatialRiftManipulatorBaseBlockRecipe> itemToRecipe;
    private final Map<Block, SpatialRiftManipulatorBaseBlockRecipe> blockToRecipe;

    private SpatialRiftManipulatorBaseBlockRecipeManager(final Level level) {
        final var recipes = level.getRecipeManager()
                .getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_BASE_BLOCK_RECIPE_TYPE.get());

        this.itemToRecipe = recipes.stream()
                .collect(Collectors.toMap(
                        recipe -> recipe.getIngredient().getItem(),
                        Function.identity()));
        this.blockToRecipe = recipes.stream()
                .collect(Collectors.toMap(
                        SpatialRiftManipulatorBaseBlockRecipe::getBaseBlock,
                        Function.identity()));
    }

    private Optional<SpatialRiftManipulatorBaseBlockRecipe> getRecipeForInput(final ItemStack is) {
        if (this.itemToRecipe.containsKey(is.getItem())) {
            final SpatialRiftManipulatorBaseBlockRecipe recipe = this.itemToRecipe.get(is.getItem());
            if (is.getCount() >= recipe.getIngredient().getCount()) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    private Optional<SpatialRiftManipulatorBaseBlockRecipe> getRecipeForBlock(final Block block) {
        return Optional.ofNullable(this.blockToRecipe.get(block));
    }
}
