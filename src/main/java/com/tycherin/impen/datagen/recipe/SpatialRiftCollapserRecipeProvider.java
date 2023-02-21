package com.tycherin.impen.datagen.recipe;

import java.util.function.Consumer;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.SpatialRiftCollapserRecipe;

import appeng.core.definitions.AEItems;
import lombok.Builder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class SpatialRiftCollapserRecipeProvider {

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        final BuilderHelper helper = new BuilderHelper(consumer);

        helper.add(ImpenRegistry.RIFT_PRISM, ImpenRegistry.STABILIZED_RIFT_PRISM);

        helper.add(ImpenRegistry.SPATIAL_RIFT_CELL_2_ITEM, AEItems.SPATIAL_CELL2);
        helper.add(ImpenRegistry.SPATIAL_RIFT_CELL_16_ITEM, AEItems.SPATIAL_CELL16);
        helper.add(ImpenRegistry.SPATIAL_RIFT_CELL_128_ITEM, AEItems.SPATIAL_CELL128);
    }

    protected static class BuilderHelper {
        private final Consumer<FinishedRecipe> consumer;

        public BuilderHelper(final Consumer<FinishedRecipe> consumer) {
            this.consumer = consumer;
        }

        public void add(final ItemLike input, final ItemLike output) {
            final String recipeName = input.asItem().getRegistryName().getPath();
            final FinishedRecipe result = RecipeArgs.builder()
                    .recipeName(recipeName)
                    .input(Ingredient.of(input))
                    .output(output.asItem().getDefaultInstance())
                    .build().toRecipe();
            consumer.accept(result);
        }
    }

    @Builder
    protected static class RecipeArgs {
        private String recipeName;
        private Ingredient input;
        private ItemStack output;

        protected FinishedRecipe toRecipe() {
            final var recipe = new SpatialRiftCollapserRecipe(null, input, output);
            return new CustomRecipeResult(recipeName, recipe);
        }
    }
}
