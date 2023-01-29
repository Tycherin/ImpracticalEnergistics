package com.tycherin.impen.datagen;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.SpatialRiftCollapserRecipeSerializer;
import com.tycherin.impen.util.ImpenIdUtil;

import appeng.core.definitions.AEItems;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class SpatialRiftCollapserRecipeProvider {

    public static final String RECIPE_TYPE_NAME = "spatial_rift_collapser";

    public SpatialRiftCollapserRecipeProvider() {
    }

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
            final var result = new RecipeBuilder()
                    .recipeName(recipeName)
                    .input(Ingredient.of(input))
                    .output(output.asItem().getDefaultInstance())
                    .build();
            consumer.accept(result);
        }
    }

    protected static class RecipeBuilder {
        private String recipeName;
        private Ingredient input;
        private ItemStack output;

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

        private class RecipeResult implements FinishedRecipe {

            @Override
            public void serializeRecipeData(final JsonObject json) {
                json.add("input", input.toJson());
                json.add("output", AE2RecipeProvider.toJson(output));
            }

            @Override
            public ResourceLocation getId() {
                return ImpenIdUtil.makeId(RECIPE_TYPE_NAME + "/" + recipeName);
            }

            @Override
            public RecipeSerializer<?> getType() {
                return SpatialRiftCollapserRecipeSerializer.INSTANCE;
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
