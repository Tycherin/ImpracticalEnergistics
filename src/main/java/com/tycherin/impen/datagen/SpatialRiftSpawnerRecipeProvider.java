package com.tycherin.impen.datagen;

import java.util.function.Consumer;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.SpatialRiftSpawnerRecipe;

import appeng.core.definitions.AEItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class SpatialRiftSpawnerRecipeProvider {

    public SpatialRiftSpawnerRecipeProvider() {
    }

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        final BuilderHelper helper = new BuilderHelper(consumer);

        helper.add(ImpenRegistry.AEROCRYSTAL_PRISM, ImpenRegistry.RIFT_PRISM, 100);

        helper.add(AEItems.SPATIAL_CELL2, ImpenRegistry.SPATIAL_RIFT_CELL_2_ITEM, 20);
        helper.add(AEItems.SPATIAL_CELL16, ImpenRegistry.SPATIAL_RIFT_CELL_16_ITEM, 40);
        helper.add(AEItems.SPATIAL_CELL128, ImpenRegistry.SPATIAL_RIFT_CELL_128_ITEM, 80);
    }

    protected static class BuilderHelper {
        private final Consumer<FinishedRecipe> consumer;

        public BuilderHelper(final Consumer<FinishedRecipe> consumer) {
            this.consumer = consumer;
        }

        public void add(final ItemLike input, final ItemLike output, final int fuelCost) {
            final String recipeName = input.asItem().getRegistryName().getPath();
            final var result = new RecipeBuilder()
                    .recipeName(recipeName)
                    .input(Ingredient.of(input))
                    .output(output.asItem().getDefaultInstance())
                    .fuelCost(fuelCost)
                    .build();
            consumer.accept(result);
        }
    }

    protected static class RecipeBuilder {
        private String recipeName;
        private Ingredient input;
        private ItemStack output;
        private int fuelCost = -1;

        public FinishedRecipe build() {
            final var recipe = new SpatialRiftSpawnerRecipe(null, input, output, fuelCost);
            return new CustomRecipeResult(recipeName, recipe);
        }

        public RecipeBuilder recipeName(final String s) {
            this.recipeName = s;
            return this;
        }

        public RecipeBuilder input(final Ingredient input) {
            this.input = input;
            return this;
        }

        public RecipeBuilder output(final ItemStack output) {
            this.output = output;
            return this;
        }

        public RecipeBuilder fuelCost(final int fuelCost) {
            this.fuelCost = fuelCost;
            return this;
        }
    }
}
