package com.tycherin.impen.datagen;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.logic.SpatialRiftWeight;
import com.tycherin.impen.recipe.RiftCatalystRecipeSerializer;
import com.tycherin.impen.util.ImpenIdUtil;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class RiftCatalystRecipeProvider {

    public static final String RIFT_CATALYST_NAME = "rift_catalyst";

    private static final int BASE_PROBABILITY = 5;

    public RiftCatalystRecipeProvider() {
    }

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        final BuilderHelper helper = new BuilderHelper(consumer);
        
        // Vanilla ores
        helper.standardBlock(Blocks.STONE, Blocks.IRON_ORE, Items.IRON_INGOT);
        helper.standardBlock(Blocks.STONE, Blocks.COPPER_ORE, Items.COPPER_INGOT);
        helper.standardBlock(Blocks.STONE, Blocks.COAL_ORE, Items.COAL);
        helper.standardBlock(Blocks.DEEPSLATE, Blocks.DEEPSLATE_DIAMOND_ORE, Items.DIAMOND);
        helper.standardBlock(Blocks.DEEPSLATE, Blocks.DEEPSLATE_EMERALD_ORE, Items.EMERALD);
        helper.standardBlock(Blocks.DEEPSLATE, Blocks.DEEPSLATE_REDSTONE_ORE, Items.REDSTONE);
        helper.standardBlock(Blocks.DEEPSLATE, Blocks.DEEPSLATE_LAPIS_ORE, Items.LAPIS_LAZULI);
        helper.standardBlock(Blocks.NETHERRACK, Blocks.NETHER_GOLD_ORE, Items.GOLD_NUGGET);
        helper.standardBlock(Blocks.NETHERRACK, Blocks.NETHER_QUARTZ_ORE, Items.QUARTZ);

        // Special vanilla stuff
        helper.standardBlock(Blocks.DIRT, Blocks.SLIME_BLOCK);
        helper.standardBlock(Blocks.DIRT, Blocks.CLAY);
        helper.standardBlock(Blocks.DIRT, Blocks.BROWN_MUSHROOM_BLOCK, Items.BROWN_MUSHROOM);
        helper.standardBlock(Blocks.DIRT, Blocks.RED_MUSHROOM_BLOCK, Items.RED_MUSHROOM);
        helper.standardBlock(Blocks.NETHERRACK, Blocks.GLOWSTONE, Items.GLOWSTONE_DUST);
        helper.standardBlock(Blocks.NETHERRACK, Blocks.MAGMA_BLOCK);
        helper.standardBlock(Blocks.NETHERRACK, Blocks.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP);
        helper.standardBlock(Blocks.END_STONE, Blocks.AMETHYST_BLOCK, Items.AMETHYST_SHARD);
        helper.standardBlock(Blocks.END_STONE, Blocks.PRISMARINE, Items.PRISMARINE_SHARD);
        helper.standardBlock(Blocks.BLACKSTONE, Blocks.OBSIDIAN);

        // AE2 stuff
        helper.standardBlock(Blocks.DEEPSLATE, AEBlocks.DEEPSLATE_QUARTZ_ORE.block(),
                AEItems.CERTUS_QUARTZ_CRYSTAL.asItem());
        helper.standardBlock(Blocks.DEEPSLATE, AEBlocks.SKY_STONE_BLOCK.block());

        // Just think: I'm in there!
        helper.standardBlock(ImpenRegistry.RIFTSTONE.asBlock(), ImpenRegistry.RIFT_SHARD_ORE.asBlock());

        // TODO Figure out recipes from other mods
    }

    private static class BuilderHelper {
        private final Consumer<FinishedRecipe> consumer;

        private final Set<String> generatedRecipes = new HashSet<>();

        public BuilderHelper(final Consumer<FinishedRecipe> consumer) {
            this.consumer = consumer;
        }

        public void standardBlock(final Block baseBlock, final Block spawnedBlock) {
            checkRecipe(spawnedBlock, (recipeName) -> {
                RiftCatalystRecipeBuilder.of(baseBlock)
                        .consumedItems(spawnedBlock.asItem())
                        .weights(new SpatialRiftWeight(spawnedBlock, BASE_PROBABILITY))
                        .save(consumer, recipeName);
            });
        }

        public void standardBlock(final Block baseBlock, final Block spawnedBlock, final Item item) {
            standardBlock(baseBlock, spawnedBlock);

            checkRecipe(item, (recipeName) -> {
                
                RiftCatalystRecipeBuilder.of(baseBlock)
                .consumedItems(item)
                .weights(new SpatialRiftWeight(spawnedBlock, 4))
                .save(consumer, recipeName);
                
                // TODO Figure out numbers & balancing
            });
        }

        private void checkRecipe(final ForgeRegistryEntry<?> ingredient, final Consumer<String> generateRecipeFunc) {
            final String ingredientRawName = ingredient.getRegistryName().getPath();

            // We don't want to add extra recipes for ores that have multiple variants, so we strip the variant prefix
            // off to create the canonical name. This means that whichever recipe gets added first trumps the others.
            final String recipeName;
            if (ingredientRawName.startsWith("deepslate_") || ingredientRawName.startsWith("nether_")) {
                recipeName = ingredientRawName.split("_", 2)[1]; // mmm, regex shenanigans
            }
            else {
                recipeName = ingredientRawName;
            }

            if (!generatedRecipes.contains(recipeName)) {
                generateRecipeFunc.accept(recipeName);
                generatedRecipes.add(recipeName);
            }
        }
    }

    public static class RiftCatalystRecipeBuilder {
        private Block baseBlock;
        private List<Ingredient> consumedItems;
        private List<SpatialRiftWeight> weights;

        private RiftCatalystRecipeBuilder() {
        }

        public static RiftCatalystRecipeBuilder of(final Block baseBlock) {
            final var builder = new RiftCatalystRecipeBuilder();
            builder.baseBlock = baseBlock;
            return builder;
        }

        public RiftCatalystRecipeBuilder baseBlock(final Block baseBlock) {
            this.baseBlock = baseBlock;
            return this;
        }

        public RiftCatalystRecipeBuilder consumedItems(final List<Ingredient> consumedItems) {
            this.consumedItems = consumedItems;
            return this;
        }

        public RiftCatalystRecipeBuilder consumedItems(final Ingredient... consumedItems) {
            return this.consumedItems(Arrays.asList(consumedItems));
        }

        public RiftCatalystRecipeBuilder consumedItems(final Item... consumedItems) {
            return this.consumedItems(
                    Arrays.asList(consumedItems).stream().map(Ingredient::of).collect(Collectors.toList()));
        }

        public RiftCatalystRecipeBuilder weights(final List<SpatialRiftWeight> weights) {
            this.weights = weights;
            return this;
        }

        public RiftCatalystRecipeBuilder weights(final SpatialRiftWeight... weights) {
            return this.weights(Arrays.asList(weights));
        }

        public void save(final Consumer<FinishedRecipe> consumer, final String name) {
            consumer.accept(new Result(name));
        }

        public class Result implements FinishedRecipe {
            private final String name;

            public Result(final String name) {
                this.name = name;
            }

            @Override
            public void serializeRecipeData(final JsonObject json) {
                final var baseBlockJson = new JsonObject();
                baseBlockJson.addProperty("block", baseBlock.getRegistryName().toString());
                json.add("base_block", baseBlockJson);

                final var consumedItemsJson = new JsonArray();
                consumedItems.forEach(ingredient -> consumedItemsJson.add(ingredient.toJson()));
                json.add("consumed_items", consumedItemsJson);

                final var weightsJson = new JsonArray();
                weights.forEach(weight -> {
                    final var weightJson = new JsonObject();
                    weightJson.addProperty("block", weight.block().getRegistryName().toString());
                    weightJson.addProperty("value", weight.blockCount());
                    weightsJson.add(weightJson);
                });
                json.add("weights", weightsJson);
            }

            @Override
            public ResourceLocation getId() {
                return ImpenIdUtil.makeId(RIFT_CATALYST_NAME + "/" + name);
            }

            @Override
            public RecipeSerializer<?> getType() {
                return RiftCatalystRecipeSerializer.INSTANCE;
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
