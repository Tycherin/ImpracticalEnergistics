package com.tycherin.impen.datagen;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipeSerializer;
import com.tycherin.impen.util.ImpenIdUtil;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
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

        // TODO Add in special spatial recipes:
        // Clear precision
        // Boost precision

        // TODO Placeholder recipes for now
        helper.addNormal(ImpenRegistry.RIFT_SHARD_ORE, Items.TNT, ImpenRegistry.RIFT_SHARD_BLOCK);

        helper.addSpatial(Items.IRON_PICKAXE, Blocks.IRON_ORE);
        helper.addSpatial(Items.IRON_ORE, Blocks.IRON_ORE);
        helper.addSpatial(Items.IRON_BLOCK, Blocks.IRON_ORE);
        helper.addSpatial(Items.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);

        helper.addSpatial(Items.LIGHTNING_ROD, Blocks.COPPER_ORE);
        helper.addSpatial(Items.COPPER_ORE, Blocks.COPPER_ORE);
        helper.addSpatial(Items.COPPER_BLOCK, Blocks.COPPER_ORE);
        helper.addSpatial(Items.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);

        helper.addSpatial(Items.CAMPFIRE, Blocks.COAL_ORE);
        helper.addSpatial(Items.COAL_ORE, Blocks.COAL_ORE);
        helper.addSpatial(Items.COAL_BLOCK, Blocks.COAL_ORE);
        helper.addSpatial(Items.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);

        helper.addSpatial(Items.GOLDEN_PICKAXE, Blocks.DEEPSLATE_GOLD_ORE);
        helper.addSpatial(Items.GOLD_ORE, Blocks.GOLD_ORE);
        helper.addSpatial(Items.GOLD_BLOCK, Blocks.DEEPSLATE_GOLD_ORE);
        helper.addSpatial(Items.GOLDEN_SWORD, Blocks.NETHER_GOLD_ORE);

        helper.addSpatial(Items.DIAMOND_PICKAXE, Blocks.DEEPSLATE_DIAMOND_ORE);
        helper.addSpatial(Items.DIAMOND_ORE, Blocks.DIAMOND_ORE);
        helper.addSpatial(Items.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
        helper.addSpatial(Items.DIAMOND_BLOCK, Blocks.DEEPSLATE_DIAMOND_ORE);

        helper.addSpatial(Items.QUARTZ_BLOCK, Blocks.NETHER_QUARTZ_ORE);
        helper.addSpatial(Items.DAYLIGHT_DETECTOR, Blocks.NETHER_QUARTZ_ORE);

        helper.addSpatial(Items.COMPARATOR, Blocks.REDSTONE_ORE);
        helper.addSpatial(Items.REDSTONE_ORE, Blocks.REDSTONE_ORE);
        helper.addSpatial(Items.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);

        helper.addSpatial(Items.LAPIS_BLOCK, Blocks.DEEPSLATE_LAPIS_ORE);
        helper.addSpatial(Items.LAPIS_ORE, Blocks.LAPIS_ORE);
        helper.addSpatial(Items.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);

        helper.addSpatial(AEBlocks.QUARTZ_ORE.asItem(), AEBlocks.QUARTZ_ORE.block());
        helper.addSpatial(AEBlocks.DEEPSLATE_QUARTZ_ORE.asItem(), AEBlocks.DEEPSLATE_QUARTZ_ORE.block());
        helper.addSpatial(AEItems.CERTUS_QUARTZ_WRENCH, AEBlocks.QUARTZ_ORE.block());

        helper.addSpatial(ImpenRegistry.RIFT_SHARD_BLOCK, ImpenRegistry.RIFT_SHARD_ORE.asBlock());
        helper.addSpatial(ImpenRegistry.RIFT_GLASS, ImpenRegistry.RIFT_SHARD_ORE.asBlock());

        helper.addSpatial(Items.OBSIDIAN, Blocks.OBSIDIAN);

        helper.addSpatial(Items.GLOWSTONE, ImpenRegistry.NETHER_GLOWSTONE_ORE.asBlock());
        helper.addSpatial(Items.NETHERITE_INGOT, ImpenRegistry.NETHER_DEBRIS_ORE.asBlock());
        helper.addSpatial(Items.AMETHYST_BLOCK, ImpenRegistry.END_AMETHYST_ORE.asBlock());
        helper.addSpatial(Items.MUSHROOM_STEW, ImpenRegistry.MUSHROOM_DIRT.asBlock());

        // TODO Figure out how to generate recipes for modded ores
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

        public void addSpatial(final ItemLike bottomInput, final Block block) {
            final String recipeName = "spatial_" + bottomInput.asItem().getRegistryName().getPath();
            final var result = new RecipeBuilder()
                    .recipeName(recipeName)
                    .bottomInput(Ingredient.of(bottomInput))
                    .spatialEffect(block)
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

        public RecipeBuilder spatialEffect(final Block block) {
            this.block = block;
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
