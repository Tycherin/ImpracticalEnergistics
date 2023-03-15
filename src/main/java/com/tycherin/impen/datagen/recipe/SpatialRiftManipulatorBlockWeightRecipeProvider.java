package com.tycherin.impen.datagen.recipe;

import java.util.function.Consumer;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.recipe.SpatialRiftManipulatorBlockWeightRecipe;
import com.tycherin.impen.util.GsonUtil;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import lombok.Builder;
import lombok.NonNull;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.registries.RegistryObject;

public class SpatialRiftManipulatorBlockWeightRecipeProvider {

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        final BuilderHelper helper = new BuilderHelper(consumer);

        helper.addRecipe(Items.IRON_PICKAXE, Blocks.IRON_ORE);
        helper.addRecipe(Items.IRON_ORE, Blocks.IRON_ORE);
        helper.addRecipe(Items.IRON_BLOCK, Blocks.IRON_ORE);
        helper.addRecipe(Items.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);

        helper.addRecipe(Items.LIGHTNING_ROD, Blocks.COPPER_ORE);
        helper.addRecipe(Items.COPPER_ORE, Blocks.COPPER_ORE);
        helper.addRecipe(Items.COPPER_BLOCK, Blocks.COPPER_ORE);
        helper.addRecipe(Items.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);

        helper.addRecipe(Items.CAMPFIRE, Blocks.COAL_ORE);
        helper.addRecipe(Items.COAL_ORE, Blocks.COAL_ORE);
        helper.addRecipe(Items.COAL_BLOCK, Blocks.COAL_ORE);
        helper.addRecipe(Items.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);

        helper.addRecipe(Items.GOLDEN_PICKAXE, Blocks.DEEPSLATE_GOLD_ORE);
        helper.addRecipe(Items.GOLD_ORE, Blocks.GOLD_ORE);
        helper.addRecipe(Items.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE);
        helper.addRecipe(Items.GOLD_BLOCK, Blocks.DEEPSLATE_GOLD_ORE);
        helper.addRecipe(Items.GOLDEN_SWORD, Blocks.NETHER_GOLD_ORE);

        helper.addRecipe(Items.DIAMOND_PICKAXE, Blocks.DEEPSLATE_DIAMOND_ORE);
        helper.addRecipe(Items.DIAMOND_ORE, Blocks.DIAMOND_ORE);
        helper.addRecipe(Items.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
        helper.addRecipe(Items.DIAMOND_BLOCK, Blocks.DEEPSLATE_DIAMOND_ORE);

        helper.addRecipe(Items.NETHER_QUARTZ_ORE, Blocks.NETHER_QUARTZ_ORE);
        helper.addRecipe(Items.QUARTZ_BLOCK, Blocks.NETHER_QUARTZ_ORE);
        helper.addRecipe(Items.DAYLIGHT_DETECTOR, Blocks.NETHER_QUARTZ_ORE);

        helper.addRecipe(Items.COMPARATOR, Blocks.REDSTONE_ORE);
        helper.addRecipe(Items.REDSTONE_ORE, Blocks.REDSTONE_ORE);
        helper.addRecipe(Items.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);

        helper.addRecipe(Items.LAPIS_BLOCK, Blocks.DEEPSLATE_LAPIS_ORE);
        helper.addRecipe(Items.LAPIS_ORE, Blocks.LAPIS_ORE);
        helper.addRecipe(Items.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);

        helper.addRecipe(AEBlocks.QUARTZ_ORE.asItem(), AEBlocks.QUARTZ_ORE.block());
        helper.addRecipe(AEBlocks.DEEPSLATE_QUARTZ_ORE.asItem(), AEBlocks.DEEPSLATE_QUARTZ_ORE.block());
        helper.addRecipe(AEItems.CERTUS_QUARTZ_WRENCH, AEBlocks.QUARTZ_ORE.block());

        helper.addRecipe(ImpenRegistry.RIFT_SHARD_BLOCK, ImpenRegistry.RIFT_SHARD_ORE.asBlock());

        helper.addRecipe(Items.OBSIDIAN, Blocks.OBSIDIAN);

        helper.addRecipe(Items.GLOWSTONE, ImpenRegistry.NETHER_GLOWSTONE_ORE.asBlock());
        helper.addRecipe(Items.NETHERITE_INGOT, ImpenRegistry.NETHER_DEBRIS_ORE.asBlock());
        helper.addRecipe(Items.AMETHYST_BLOCK, ImpenRegistry.END_AMETHYST_ORE.asBlock());

        // And now, a big pile of recipes for modded compatibility
        // The fact that this works is kind of impressive

        // Thermal Foundation
        final String THERMAL_MOD_ID = "thermal";

        helper.addRecipe(THERMAL_MOD_ID, "apatite_ore", "apatite_ore");
        helper.addRecipe(THERMAL_MOD_ID, "deepslate_apatite_ore", "deepslate_apatite_ore");
        helper.addRecipe(THERMAL_MOD_ID, "apatite_block", "apatite_ore");

        helper.addRecipe(THERMAL_MOD_ID, "cinnabar_ore", "cinnabar_ore");
        helper.addRecipe(THERMAL_MOD_ID, "deepslate_cinnabar_ore", "deepslate_cinnabar_ore");
        helper.addRecipe(THERMAL_MOD_ID, "cinnabar_block", "cinnabar_ore");

        helper.addRecipe(THERMAL_MOD_ID, "niter_ore", "niter_ore");
        helper.addRecipe(THERMAL_MOD_ID, "deepslate_niter_ore", "deepslate_niter_ore");
        helper.addRecipe(THERMAL_MOD_ID, "niter_block", "niter_ore");

        helper.addRecipe(THERMAL_MOD_ID, "sulfur_ore", "sulfur_ore");
        helper.addRecipe(THERMAL_MOD_ID, "deepslate_sulfur_ore", "deepslate_sulfur_ore");
        helper.addRecipe(THERMAL_MOD_ID, "sulfur_block", "sulfur_ore");

        helper.addRecipe(THERMAL_MOD_ID, "tin_ore", "tin_ore");
        helper.addRecipe(THERMAL_MOD_ID, "deepslate_tin_ore", "deepslate_tin_ore");
        helper.addRecipe(THERMAL_MOD_ID, "tin_block", "tin_ore");
        helper.addRecipe(THERMAL_MOD_ID, "tin_gear", "tin_ore");

        helper.addRecipe(THERMAL_MOD_ID, "lead_ore", "lead_ore");
        helper.addRecipe(THERMAL_MOD_ID, "deepslate_lead_ore", "deepslate_lead_ore");
        helper.addRecipe(THERMAL_MOD_ID, "lead_block", "deepslate_lead_ore");
        helper.addRecipe(THERMAL_MOD_ID, "lead_gear", "deepslate_lead_ore");

        helper.addRecipe(THERMAL_MOD_ID, "silver_ore", "silver_ore");
        helper.addRecipe(THERMAL_MOD_ID, "deepslate_silver_ore", "deepslate_silver_ore");
        helper.addRecipe(THERMAL_MOD_ID, "silver_block", "deepslate_silver_ore");
        helper.addRecipe(THERMAL_MOD_ID, "silver_gear", "deepslate_silver_ore");

        helper.addRecipe(THERMAL_MOD_ID, "nickel_ore", "nickel_ore");
        helper.addRecipe(THERMAL_MOD_ID, "deepslate_nickel_ore", "deepslate_nickel_ore");
        helper.addRecipe(THERMAL_MOD_ID, "nickel_block", "nickel_ore");
        helper.addRecipe(THERMAL_MOD_ID, "nickel_gear", "nickel_ore");

        // Mekanism
        final String MEKANISM_MOD_ID = "mekanism";
        // Mekanism Tools is a separate mod from Mekanism, so I don't feel like making the recipe generation more
        // complex right now

        helper.addRecipe(MEKANISM_MOD_ID, "osmium_ore", "osmium_ore");
        helper.addRecipe(MEKANISM_MOD_ID, "deepslate_osmium_ore", "deepslate_osmium_ore");
        helper.addRecipe(MEKANISM_MOD_ID, "osmium_block", "osmium_ore");

        helper.addRecipe(MEKANISM_MOD_ID, "uranium_ore", "uranium_ore");
        helper.addRecipe(MEKANISM_MOD_ID, "deepslate_uranium_ore", "deepslate_uranium_ore");
        helper.addRecipe(MEKANISM_MOD_ID, "uranium_block", "deepslate_uranium_ore");

        helper.addRecipe(MEKANISM_MOD_ID, "fluorite_ore", "fluorite_ore");
        helper.addRecipe(MEKANISM_MOD_ID, "deepslate_fluorite_ore", "deepslate_fluorite_ore");
        helper.addRecipe(MEKANISM_MOD_ID, "fluorite_block", "deepslate_fluorite_ore");

        // Mystical Agriculture
        final String MYSTICAL_AGRICULTURE_MOD_ID = "mysticalagriculture";

        helper.addRecipe(MYSTICAL_AGRICULTURE_MOD_ID, "prosperity_ore", "prosperity_ore");
        helper.addRecipe(MYSTICAL_AGRICULTURE_MOD_ID, "deepslate_prosperity_ore", "deepslate_prosperity_ore");
        helper.addRecipe(MYSTICAL_AGRICULTURE_MOD_ID, "prosperity_block", "deepslate_prosperity_ore");
        helper.addRecipe(MYSTICAL_AGRICULTURE_MOD_ID, "prosperity_gemstone", "deepslate_prosperity_ore");

        helper.addRecipe(MYSTICAL_AGRICULTURE_MOD_ID, "inferium_ore", "inferium_ore");
        helper.addRecipe(MYSTICAL_AGRICULTURE_MOD_ID, "deepslate_inferium_ore", "deepslate_inferium_ore");
        helper.addRecipe(MYSTICAL_AGRICULTURE_MOD_ID, "inferium_block", "inferium_ore");
        helper.addRecipe(MYSTICAL_AGRICULTURE_MOD_ID, "inferium_gemstone", "inferium_ore");

        helper.addRecipe(MYSTICAL_AGRICULTURE_MOD_ID, "soulium_ore", "soulium_ore");
        helper.addRecipe(MYSTICAL_AGRICULTURE_MOD_ID, "soulium_gemstone", "soulium_ore");

        // Create
        final String CREATE_MOD_ID = "create";

        helper.addRecipe(CREATE_MOD_ID, "zinc_ore", "zinc_ore");
        helper.addRecipe(CREATE_MOD_ID, "deepslate_zinc_ore", "deepslate_zinc_ore");
        helper.addRecipe(CREATE_MOD_ID, "zinc_block", "deepslate_zinc_ore");
    }

    protected static class BuilderHelper {
        private final Consumer<FinishedRecipe> consumer;

        public BuilderHelper(final Consumer<FinishedRecipe> consumer) {
            this.consumer = consumer;
        }

        public void addRecipe(final ItemLike bottomInput, final Block block) {
            final String recipeName = "spatial_" + bottomInput.asItem().getRegistryName().getPath();
            final var recipe = new SpatialRiftManipulatorBlockWeightRecipe(null, Ingredient.of(bottomInput), block);
            consumer.accept(new RealRecipe(recipeName, recipe));
        }

        public void addRecipe(final String otherModId, final String itemRegistryName, final String blockRegistryName) {
            final String recipeName = "spatial_" + itemRegistryName;
            final var data = SpatialRiftManipulatorBlockWeightData.builder()
                    .bottomInput(new GsonUtil.MockIngredient(itemRegistryName))
                    .block(new GsonUtil.MockBlock(blockRegistryName))
                    .build();
            ConditionalRecipe.builder()
                    .addCondition(new ModLoadedCondition(otherModId))
                    .addRecipe(new FakeRecipe(recipeName, data))
                    .build(consumer, ImpracticalEnergisticsMod.MOD_ID, recipeName);
        }
    }

    private static class RealRecipe extends CustomRecipeResult<SpatialRiftManipulatorBlockWeightRecipe> {

        public RealRecipe(@NonNull String recipeName, @NonNull SpatialRiftManipulatorBlockWeightRecipe data) {
            super(recipeName, data);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return SpatialRiftManipulatorBlockWeightRecipe.Serializer.INSTANCE;
        }

        @Override
        protected RegistryObject<?> getRecipeHolder() {
            return ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_BLOCK_WEIGHT_RECIPE_TYPE;
        }
    }

    @Builder
    private static record SpatialRiftManipulatorBlockWeightData(
            GsonUtil.MockIngredient bottomInput,
            GsonUtil.MockBlock block) {
    }

    private static class FakeRecipe extends CustomRecipeResult<SpatialRiftManipulatorBlockWeightData> {

        public FakeRecipe(@NonNull String recipeName, @NonNull SpatialRiftManipulatorBlockWeightData data) {
            super(recipeName, data);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return SpatialRiftManipulatorBlockWeightRecipe.Serializer.INSTANCE;
        }

        @Override
        protected RegistryObject<?> getRecipeHolder() {
            return ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_BLOCK_WEIGHT_RECIPE_TYPE;
        }
    }
}
