package com.tycherin.impen.datagen;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipeSerializer;
import com.tycherin.impen.util.ImpenIdUtil;

import appeng.datagen.providers.recipes.AE2RecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SpatialRiftManipulatorRecipeProvider {

    public static final String RECIPE_TYPE_NAME = "spatial_rift_manipulator";

    public SpatialRiftManipulatorRecipeProvider() {
    }

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        final BuilderHelper helper = new BuilderHelper(consumer);

        // TODO Placeholder recipes for now
        helper.addNormal(ImpenRegistry.RIFT_SHARD_ORE, Items.TNT, ImpenRegistry.RIFT_SHARD_BLOCK);

        helper.addSpatial(Items.IRON_INGOT, Blocks.IRON_ORE, 5);
        helper.addSpatial(Items.IRON_BLOCK, Blocks.IRON_ORE, 25);
    }

    private static class BuilderHelper {
        private final Consumer<FinishedRecipe> consumer;

        public BuilderHelper(final Consumer<FinishedRecipe> consumer) {
            this.consumer = consumer;
        }

        public void addNormal(final ItemLike topInput, final ItemLike bottomInput, final ItemLike output) {
            final String recipeName = topInput.asItem().getRegistryName().getPath();
            final var result = new RecipeBuilder()
                    .recipeName(recipeName)
                    .topInput(topInput.asItem().getDefaultInstance())
                    .bottomInput(Ingredient.of(bottomInput))
                    .output(output.asItem().getDefaultInstance())
                    .build();
            consumer.accept(result);
        }

        public void addSpatial(final ItemLike bottomInput, final Block block, final int value) {
            final String recipeName = "spatial_" + bottomInput.asItem().getRegistryName().getPath();
            final var result = new RecipeBuilder()
                    .recipeName(recipeName)
                    .bottomInput(Ingredient.of(bottomInput))
                    .spatialEffect(block, value)
                    .build();
            consumer.accept(result);
        }
    }

    private static class RecipeBuilder {
        private String recipeName;

        private ItemStack topInput;
        private Ingredient bottomInput;
        private ItemStack output;

        private Block block;
        private int value;

        public RecipeResult build() {
            if (recipeName == null) {
                throw new RuntimeException("Recipe name cannot be null");
            }
            return new RecipeResult();
        }

        public RecipeBuilder recipeName(final String s) {
            this.recipeName = s;
            return this;
        }

        public RecipeBuilder topInput(final ItemStack is) {
            this.topInput = is;
            return this;
        }

        public RecipeBuilder bottomInput(final Ingredient ing) {
            this.bottomInput = ing;
            return this;
        }

        public RecipeBuilder output(final ItemStack output) {
            this.output = output;
            return this;
        }

        public RecipeBuilder spatialEffect(final Block block, final int value) {
            this.block = block;
            this.value = value;
            return this;
        }

        private class RecipeResult implements FinishedRecipe {

            @Override
            public void serializeRecipeData(final JsonObject json) {
                if (topInput != null) {
                    json.add("top_input", AE2RecipeProvider.toJson(topInput));
                }
                json.add("bottom_input", bottomInput.toJson());
                if (output != null) {
                    json.add("output", AE2RecipeProvider.toJson(output));
                }
                if (block != null) {
                    final JsonObject spatialJson = new JsonObject();
                    spatialJson.addProperty("block", block.getRegistryName().toString());
                    spatialJson.addProperty("value", value);
                    json.add("spatial_effect", spatialJson);
                }
            }

            @Override
            public ResourceLocation getId() {
                return ImpenIdUtil.makeId(RECIPE_TYPE_NAME + "/" + recipeName);
            }

            @Override
            public RecipeSerializer<?> getType() {
                return SpatialRiftManipulatorRecipeSerializer.INSTANCE;
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
