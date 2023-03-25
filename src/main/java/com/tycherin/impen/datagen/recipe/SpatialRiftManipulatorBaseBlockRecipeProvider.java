package com.tycherin.impen.datagen.recipe;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.SpatialRiftManipulatorBaseBlockRecipe;
import com.tycherin.impen.util.GsonUtil.MockBlock;

import appeng.core.definitions.AEBlocks;
import lombok.Builder;
import lombok.NonNull;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.RegistryObject;

public class SpatialRiftManipulatorBaseBlockRecipeProvider {

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        List.of(
                SpatialRiftBaseBlockData.builder()
                        .baseBlock(Blocks.STONE)
                        .ingredient(createStack(Items.STONE, 16))
                        .baseWeights(new MapBuilder()
                                // Vanilla
                                .put(Blocks.COAL_ORE, 100)
                                .put(Blocks.COPPER_ORE, 80)
                                .put(Blocks.IRON_ORE, 50)
                                .put(Blocks.LAPIS_ORE, 12)
                                .put(Blocks.GOLD_ORE, 6)
                                .put(Blocks.DIAMOND_ORE, 1)
                                // AE2
                                .put(AEBlocks.QUARTZ_ORE, 20)
                                // Create
                                .put("create:zinc_ore", 30)
                                // Mekanism
                                .put("mekanism:osmium_ore", 30)
                                .put("mekanism:fluorite_ore", 4)
                                // Mystical Agriculture
                                .put("mysticalagriculture:inferium_ore", 40)
                                .put("mysticalagriculture:prosperity_ore", 12)
                                // Thermal
                                .put("thermal:tin_ore", 30)
                                .put("thermal:apatite_ore", 16)
                                .put("thermal:niter_ore", 10)
                                .put("thermal:nickel_ore", 10)
                                .put("thermal:silver_ore", 6)
                                .put("thermal:sulfur_ore", 4)
                                .put("thermal:cinnabar_ore", 1)
                                .build())
                        .build(),

                SpatialRiftBaseBlockData.builder()
                        .baseBlock(Blocks.DEEPSLATE)
                        .ingredient(createStack(Items.DEEPSLATE, 16))
                        .baseWeights(new MapBuilder()
                                // Vanilla
                                .put(Blocks.DEEPSLATE_REDSTONE_ORE, 34)
                                .put(Blocks.DEEPSLATE_GOLD_ORE, 30)
                                .put(Blocks.DEEPSLATE_IRON_ORE, 28)
                                .put(Blocks.DEEPSLATE_LAPIS_ORE, 15)
                                .put(Blocks.DEEPSLATE_DIAMOND_ORE, 15)
                                // AE2
                                .put(AEBlocks.DEEPSLATE_QUARTZ_ORE, 65)
                                // Create
                                .put("create:deepslate_zinc_ore", 50)
                                // Mekanism
                                .put("mekanism:deepslate_fluorite_ore", 50)
                                .put("mekanism:deepslate_uranium_ore", 45)
                                .put("mekanism:deepslate_osmium_ore", 15)
                                // Mystical Agriculture
                                .put("mysticalargiculture:deepslate_prosperity_ore", 50)
                                .put("mysticalargiculture:deepslate_inferium_ore", 30)
                                // Thermal
                                .put("thermal:deepslate_lead_ore", 18)
                                .put("thermal:deepslate_silver_ore", 15)
                                .put("thermal:deepslate_nickel_ore", 4)
                                .put("thermal:deepslate_tin_ore", 3)
                                .put("thermal:deepslate_sulfur_ore", 3)
                                .put("thermal:deepslate_niter_ore", 3)
                                .put("thermal:deepslate_apatite_ore", 2)
                                .put("thermal:deepslate_cinnabar_ore", 1)
                                .build())
                        .build(),

                SpatialRiftBaseBlockData.builder()
                        .baseBlock(Blocks.NETHERRACK)
                        .ingredient(createStack(Items.NETHERRACK, 16))
                        .baseWeights(new MapBuilder()
                                // Vanilla
                                .put(Blocks.NETHER_QUARTZ_ORE, 100)
                                .put(Blocks.NETHER_GOLD_ORE, 30)
                                .put(Blocks.ANCIENT_DEBRIS, 2)
                                // Mystical Agriculture
                                .put("mysticalagriculture:soulium_ore", 90)
                                .build())
                        .build(),

                SpatialRiftBaseBlockData.builder()
                        .baseBlock(ImpenRegistry.UNSTABLE_RIFTSTONE.asBlock())
                        .ingredient(createStack(ImpenRegistry.RIFTSTONE_DUST, 16))
                        .baseWeights(new MapBuilder()
                                // Impractical Energistics
                                .put(ImpenRegistry.RIFT_SHARD_ORE, 10)
                                .put(ImpenRegistry.RIFTSTONE, 200)
                                .build())
                        .build(),

                SpatialRiftBaseBlockData.builder()
                        .baseBlock(ImpenRegistry.RIFTSTONE.asBlock())
                        .ingredient(createStack(ImpenRegistry.RIFTSTONE, 16))
                        .baseWeights(new MapBuilder()
                                // Impractical Energistics
                                .put(ImpenRegistry.RIFT_SHARD_ORE, 40)
                                .put(ImpenRegistry.END_AMETHYST_ORE, 40)
                                .put(ImpenRegistry.UNSTABLE_RIFTSTONE, 1000)
                                .build())
                        .build())
                .forEach(data -> {
                    final String recipeName = data.baseBlock.getRegistryName().getPath();
                    consumer.accept(new RecipeTemplate(recipeName, data));
                });
    }

    private static class MapBuilder {
        private final ImmutableMap.Builder<MockBlock, Integer> builder = ImmutableMap.<MockBlock, Integer>builder();

        public MapBuilder put(final appeng.core.definitions.BlockDefinition<?> blockDef, final Integer value) {
            return this.put(blockDef.block(), value);
        }

        public MapBuilder put(final com.tycherin.impen.ImpenRegistry.BlockDefinition blockDef, final Integer value) {
            return this.put(blockDef.block(), value);
        }

        public MapBuilder put(final Block block, final Integer value) {
            return this.put(block.getRegistryName().toString(), value);
        }

        public MapBuilder put(final String blockId, final Integer value) {
            builder.put(new MockBlock(blockId), value);
            return this;
        }

        public Map<MockBlock, Integer> build() {
            return Map.copyOf(builder.build());
        }
    }

    private ItemStack createStack(final ItemLike item, final int count) {
        final var stack = item.asItem().getDefaultInstance().copy();
        stack.setCount(count);
        return stack;
    }

    @Builder
    public static record SpatialRiftBaseBlockData(
            Block baseBlock,
            ItemStack ingredient,
            Map<MockBlock, Integer> baseWeights) {
    }

    private static class RecipeTemplate extends CustomRecipeResult<SpatialRiftBaseBlockData> {

        public RecipeTemplate(@NonNull final String recipeName, @NonNull final SpatialRiftBaseBlockData data) {
            super(recipeName, data);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return SpatialRiftManipulatorBaseBlockRecipe.Serializer.INSTANCE;
        }

        @Override
        protected RegistryObject<?> getRecipeHolder() {
            return ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_BASE_BLOCK_RECIPE_TYPE;
        }
    }
}
