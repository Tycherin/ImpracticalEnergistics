package com.tycherin.impen.datagen;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpenRegistry.ItemDefinition;
import com.tycherin.impen.logic.rift.RiftWeight;
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
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.tags.ITag;

public class RiftCatalystRecipeProvider {

    public static final String RIFT_CATALYST_NAME = "rift_catalyst";
    public static final Map<ItemDefinition, Block> CATALYSTS_TO_BLOCKS = ImmutableMap.<ItemDefinition, Block>builder()
            .put(ImpenRegistry.RIFT_CATALYST_BLACKSTONE, Blocks.BLACKSTONE)
            .put(ImpenRegistry.RIFT_CATALYST_DEEPSLATE, Blocks.DEEPSLATE)
            .put(ImpenRegistry.RIFT_CATALYST_DIRT, Blocks.DIRT)
            .put(ImpenRegistry.RIFT_CATALYST_END_STONE, Blocks.END_STONE)
            .put(ImpenRegistry.RIFT_CATALYST_NETHERRACK, Blocks.NETHERRACK)
            .put(ImpenRegistry.RIFT_CATALYST_STONE, Blocks.STONE)
            .build();

    private static final int BASE_PROBABILITY = 5;

    public RiftCatalystRecipeProvider() {
    }

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        final BuilderHelper helper = new BuilderHelper(consumer);

        // Vanilla ores
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_STONE, Blocks.IRON_ORE, Items.IRON_INGOT);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_STONE, Blocks.COPPER_ORE, Items.COPPER_INGOT);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_STONE, Blocks.COAL_ORE, Items.COAL);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_DEEPSLATE, Blocks.DEEPSLATE_DIAMOND_ORE, Items.DIAMOND);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_DEEPSLATE, Blocks.DEEPSLATE_EMERALD_ORE, Items.EMERALD);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_DEEPSLATE, Blocks.DEEPSLATE_REDSTONE_ORE, Items.REDSTONE);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_DEEPSLATE, Blocks.DEEPSLATE_LAPIS_ORE, Items.LAPIS_LAZULI);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_NETHERRACK, Blocks.NETHER_GOLD_ORE, Items.GOLD_NUGGET);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_NETHERRACK, Blocks.NETHER_QUARTZ_ORE, Items.QUARTZ);

        // Special vanilla stuff
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_DIRT, Blocks.SLIME_BLOCK);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_DIRT, Blocks.CLAY);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_DIRT, Blocks.BROWN_MUSHROOM_BLOCK, Items.BROWN_MUSHROOM);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_DIRT, Blocks.RED_MUSHROOM_BLOCK, Items.RED_MUSHROOM);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_NETHERRACK, Blocks.GLOWSTONE, Items.GLOWSTONE_DUST);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_NETHERRACK, Blocks.MAGMA_BLOCK);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_NETHERRACK, Blocks.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_END_STONE, Blocks.AMETHYST_BLOCK, Items.AMETHYST_SHARD);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_END_STONE, Blocks.PRISMARINE, Items.PRISMARINE_SHARD);
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_BLACKSTONE, Blocks.OBSIDIAN);

        // AE2 stuff
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_DEEPSLATE, AEBlocks.DEEPSLATE_QUARTZ_ORE.block(),
                AEItems.CERTUS_QUARTZ_CRYSTAL.asItem());
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_BLACKSTONE, AEBlocks.SKY_STONE_BLOCK.block());

        // Just think: I'm in there!
        helper.standardBlock(ImpenRegistry.RIFT_CATALYST_RIFTSTONE, ImpenRegistry.RIFT_SHARD_ORE.asBlock());

        // TODO This doesn't work
        // Catchall for properly tagged ore blocks added by other mods
        // Note that the order is important - prefer more advanced stone types/catalysts over easier ones
        ForgeRegistries.BLOCKS.tags().getTag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).forEach(ore -> {
            helper.standardBlock(ImpenRegistry.RIFT_CATALYST_DEEPSLATE, ore);
        });
        ForgeRegistries.BLOCKS.tags().getTag(Tags.Blocks.ORES_IN_GROUND_NETHERRACK).forEach(ore -> {
            helper.standardBlock(ImpenRegistry.RIFT_CATALYST_NETHERRACK, ore);
        });
        ForgeRegistries.BLOCKS.tags().getTag(Tags.Blocks.ORES_IN_GROUND_STONE).forEach(ore -> {
            helper.standardBlock(ImpenRegistry.RIFT_CATALYST_STONE, ore);
        });
    }

    private static class BuilderHelper {
        private final Consumer<FinishedRecipe> consumer;
        private final ITag<Block> denseTag;
        private final ITag<Block> singularTag;
        private final ITag<Block> sparseTag;

        private final Set<String> generatedRecipes = new HashSet<>();

        public BuilderHelper(final Consumer<FinishedRecipe> consumer) {
            this.consumer = consumer;
            this.denseTag = ForgeRegistries.BLOCKS.tags().getTag(Tags.Blocks.ORE_RATES_DENSE);
            this.singularTag = ForgeRegistries.BLOCKS.tags().getTag(Tags.Blocks.ORE_RATES_SINGULAR);
            this.sparseTag = ForgeRegistries.BLOCKS.tags().getTag(Tags.Blocks.ORE_RATES_SPARSE);
        }

        public void standardBlock(final ItemDefinition catalyst, final Block block) {
            checkRecipe(block, (recipeName) -> {
                RiftCatalystRecipeBuilder.of(catalyst)
                        .consumedItems(block.asItem())
                        .weights(new RiftWeight(block, BASE_PROBABILITY))
                        .save(consumer, recipeName);
            });
        }

        public void standardBlock(final ItemDefinition catalyst, final Block block, final Item item) {
            standardBlock(catalyst, block);

            checkRecipe(item, (recipeName) -> {
                final double exchangeRate;
                if (sparseTag.contains(block)) {
                    exchangeRate = 2;
                }
                else if (singularTag.contains(block)) {
                    exchangeRate = 1;
                }
                else if (denseTag.contains(block)) {
                    exchangeRate = .25;
                }
                else {
                    // Not an ore, or not a properly tagged ore
                    exchangeRate = .25;
                }

                RiftCatalystRecipeBuilder.of(catalyst)
                        .consumedItems(item)
                        .weights(new RiftWeight(block, BASE_PROBABILITY * exchangeRate))
                        .save(consumer, recipeName);
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
        private Item catalyst;
        private List<Ingredient> consumedItems;
        private List<RiftWeight> weights;

        private RiftCatalystRecipeBuilder() {
        }

        public static RiftCatalystRecipeBuilder of(final ItemDefinition catalyst) {
            final var builder = new RiftCatalystRecipeBuilder();
            if (!CATALYSTS_TO_BLOCKS.containsKey(catalyst)) {
                throw new IllegalArgumentException("No mapping found for catalyst: " + catalyst);
            }

            builder.catalyst = catalyst.asItem();
            builder.baseBlock = CATALYSTS_TO_BLOCKS.get(catalyst);
            return builder;
        }

        public RiftCatalystRecipeBuilder baseBlock(final Block baseBlock) {
            this.baseBlock = baseBlock;
            return this;
        }

        public RiftCatalystRecipeBuilder catalyst(final Item catalyst) {
            this.catalyst = catalyst;
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

        public RiftCatalystRecipeBuilder weights(final List<RiftWeight> weights) {
            this.weights = weights;
            return this;
        }

        public RiftCatalystRecipeBuilder weights(final RiftWeight... weights) {
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

                final var catalystJson = new JsonObject();
                catalystJson.addProperty("item", catalyst.getRegistryName().toString());
                json.add("catalyst", catalystJson);

                final var consumedItemsJson = new JsonArray();
                consumedItems.forEach(ingredient -> consumedItemsJson.add(ingredient.toJson()));
                json.add("consumed_items", consumedItemsJson);

                final var weightsJson = new JsonArray();
                weights.forEach(weight -> {
                    final var weightJson = new JsonObject();
                    weightJson.addProperty("block", weight.block().getRegistryName().toString());
                    weightJson.addProperty("probability", weight.probability());
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
