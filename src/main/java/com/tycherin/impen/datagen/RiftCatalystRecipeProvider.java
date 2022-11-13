package com.tycherin.impen.datagen;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

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

    public RiftCatalystRecipeProvider() {
    }
    
    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        RiftCatalystRecipeBuilder.of(ImpenRegistry.RIFT_CATALYST_STONE)
            .consumedItems(Items.APPLE)
            .weights(new RiftWeight(Blocks.IRON_ORE, 5))
            .save(consumer, "stone_apple");
        
        // TODO Generate remaining recipes
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
