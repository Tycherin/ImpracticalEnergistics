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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class SpatialRiftCollapserRecipeProvider {

    public static final String RECIPE_TYPE_NAME = "spatial_rift_collapser";

    public SpatialRiftCollapserRecipeProvider() {
    }

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        final BuilderHelper helper = new BuilderHelper(consumer);

        // TODO These are just here for testing, get rid of them later
        helper.add(Items.GOLDEN_APPLE, Items.AMETHYST_SHARD);
        helper.add(ImpenRegistry.STABILIZED_RIFT_PRISM, ImpenRegistry.RIFT_PRISM);

        // TODO Yeah, I want separate items here
        helper.add(ImpenRegistry.RIFTED_SPATIAL_CELL_ITEM, AEItems.SPATIAL_CELL2);
    }

    private static class BuilderHelper {
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

    private static class RecipeBuilder {
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
