package com.tycherin.impen.datagen;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.SpatialRiftSpawnerRecipeSerializer;
import com.tycherin.impen.util.ImpenIdUtil;

import appeng.core.definitions.AEItems;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class SpatialRiftSpawnerRecipeProvider {

    public static final String RECIPE_TYPE_NAME = "spatial_rift_spawner";

    public SpatialRiftSpawnerRecipeProvider() {
    }

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        final BuilderHelper helper = new BuilderHelper(consumer);

        // TODO These are just here for testing, get rid of them later
        helper.add(Items.APPLE, Items.GOLDEN_APPLE, 50);
        helper.add(ImpenRegistry.RIFT_PRISM, ImpenRegistry.STABILIZED_RIFT_PRISM, 100);

        // TODO Do I want to differentiate these at the recipe level, or just have special logic?
        // Related: how do you tell RSCs apart?
        helper.add(AEItems.SPATIAL_CELL2, ImpenRegistry.RIFTED_SPATIAL_CELL_ITEM, 200);
        helper.add(AEItems.SPATIAL_CELL16, ImpenRegistry.RIFTED_SPATIAL_CELL_ITEM, 400);
        helper.add(AEItems.SPATIAL_CELL128, ImpenRegistry.RIFTED_SPATIAL_CELL_ITEM, 800);
    }

    private static class BuilderHelper {
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

    private static class RecipeBuilder {
        private String recipeName;
        private Ingredient input;
        private ItemStack output;
        private int fuelCost = -1;

        public RecipeResult build() {
            if (recipeName == null) {
                throw new RuntimeException("Recipe name cannot be null");
            }
            if (input == null) {
                throw new RuntimeException("Input cannot be null");
            }
            if (output == null) {
                throw new RuntimeException("Output cannot be null");
            }
            if (fuelCost == -1) {
                throw new RuntimeException("Fuel cost name cannot be null");
            }
            return new RecipeResult();
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

        private class RecipeResult implements FinishedRecipe {

            @Override
            public void serializeRecipeData(final JsonObject json) {
                json.add("input", input.toJson());
                json.add("output", AE2RecipeProvider.toJson(output));
                json.addProperty("fuel_cost", fuelCost);
            }

            @Override
            public ResourceLocation getId() {
                return ImpenIdUtil.makeId(RECIPE_TYPE_NAME + "/" + recipeName);
            }

            @Override
            public RecipeSerializer<?> getType() {
                return SpatialRiftSpawnerRecipeSerializer.INSTANCE;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        }
    }
}
