package com.tycherin.impen.datagen.recipe;

import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe.SpecialSpatialRecipe.SpecialSpatialRecipeType;
import com.tycherin.impen.util.ImpenIdUtil;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import lombok.Builder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;

public class SpatialRiftManipulatorRecipeProvider {

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        final BuilderHelper helper = new BuilderHelper(consumer);

        consumer.accept(RecipeArgs.builder()
                .recipeName("spatial_clear_precision")
                .bottomInput(Ingredient.of(Tags.Items.GLASS))
                .specialType(SpecialSpatialRecipeType.CLEAR_INPUTS)
                .build().toRecipe());
        consumer.accept(RecipeArgs.builder()
                .recipeName("spatial_boost_precision")
                .bottomInput(Ingredient.of(ImpenRegistry.STABILIZED_RIFT_PRISM))
                .specialType(SpecialSpatialRecipeType.BOOST_PRECISION)
                .build().toRecipe());

        helper.addNormal(ImpenRegistry.RIFTSTONE, Items.IRON_PICKAXE, ImpenRegistry.RIFTSTONE_DUST);

        helper.addSpatial(Items.IRON_PICKAXE, Blocks.IRON_ORE, Blocks.STONE);
        helper.addSpatial(Items.IRON_ORE, Blocks.IRON_ORE, Blocks.STONE);
        helper.addSpatial(Items.IRON_BLOCK, Blocks.IRON_ORE, Blocks.STONE);
        helper.addSpatial(Items.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE);

        helper.addSpatial(Items.LIGHTNING_ROD, Blocks.COPPER_ORE, Blocks.STONE);
        helper.addSpatial(Items.COPPER_ORE, Blocks.COPPER_ORE, Blocks.STONE);
        helper.addSpatial(Items.COPPER_BLOCK, Blocks.COPPER_ORE, Blocks.STONE);
        helper.addSpatial(Items.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE);

        helper.addSpatial(Items.CAMPFIRE, Blocks.COAL_ORE, Blocks.STONE);
        helper.addSpatial(Items.COAL_ORE, Blocks.COAL_ORE, Blocks.STONE);
        helper.addSpatial(Items.COAL_BLOCK, Blocks.COAL_ORE, Blocks.STONE);
        helper.addSpatial(Items.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE);

        helper.addSpatial(Items.GOLDEN_PICKAXE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE);
        helper.addSpatial(Items.GOLD_ORE, Blocks.GOLD_ORE, Blocks.STONE);
        helper.addSpatial(Items.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE);
        helper.addSpatial(Items.GOLD_BLOCK, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE);
        helper.addSpatial(Items.GOLDEN_SWORD, Blocks.NETHER_GOLD_ORE, Blocks.NETHERRACK);

        helper.addSpatial(Items.DIAMOND_PICKAXE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE);
        helper.addSpatial(Items.DIAMOND_ORE, Blocks.DIAMOND_ORE, Blocks.STONE);
        helper.addSpatial(Items.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE);
        helper.addSpatial(Items.DIAMOND_BLOCK, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE);

        helper.addSpatial(Items.QUARTZ_BLOCK, Blocks.NETHER_QUARTZ_ORE, Blocks.NETHERRACK);
        helper.addSpatial(Items.DAYLIGHT_DETECTOR, Blocks.NETHER_QUARTZ_ORE, Blocks.NETHERRACK);

        helper.addSpatial(Items.COMPARATOR, Blocks.REDSTONE_ORE, Blocks.STONE);
        helper.addSpatial(Items.REDSTONE_ORE, Blocks.REDSTONE_ORE, Blocks.STONE);
        helper.addSpatial(Items.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE);

        helper.addSpatial(Items.LAPIS_BLOCK, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE);
        helper.addSpatial(Items.LAPIS_ORE, Blocks.LAPIS_ORE, Blocks.STONE);
        helper.addSpatial(Items.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE);

        helper.addSpatial(AEBlocks.QUARTZ_ORE.asItem(), AEBlocks.QUARTZ_ORE.block(), Blocks.STONE);
        helper.addSpatial(AEBlocks.DEEPSLATE_QUARTZ_ORE.asItem(), AEBlocks.DEEPSLATE_QUARTZ_ORE.block(),
                Blocks.DEEPSLATE);
        helper.addSpatial(AEItems.CERTUS_QUARTZ_WRENCH, AEBlocks.QUARTZ_ORE.block(), Blocks.STONE);

        helper.addSpatial(ImpenRegistry.RIFT_SHARD_BLOCK, ImpenRegistry.RIFT_SHARD_ORE.asBlock(),
                ImpenRegistry.RIFTSTONE.asBlock());

        helper.addSpatial(Items.OBSIDIAN, Blocks.OBSIDIAN, Blocks.BLACKSTONE);

        helper.addSpatial(Items.GLOWSTONE, ImpenRegistry.NETHER_GLOWSTONE_ORE.asBlock(), Blocks.NETHERRACK);
        helper.addSpatial(Items.NETHERITE_INGOT, ImpenRegistry.NETHER_DEBRIS_ORE.asBlock(), Blocks.NETHERRACK);
        helper.addSpatial(Items.AMETHYST_BLOCK, ImpenRegistry.END_AMETHYST_ORE.asBlock(), Blocks.END_STONE);
        helper.addSpatial(Items.MUSHROOM_STEW, ImpenRegistry.MUSHROOM_DIRT.asBlock(), Blocks.DIRT);

        // And now, a big pile of recipes for modded compatibility
        // The fact that this works is kind of impressive

        // Thermal Foundation
        final String THERMAL_MOD_ID = "thermal";

        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "apatite_ore", "apatite_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_apatite_ore", "deepslate_apatite_ore",
                Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "apatite_block", "apatite_ore", Blocks.STONE);

        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "cinnabar_ore", "cinnabar_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_cinnabar_ore", "deepslate_cinnabar_ore",
                Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "cinnabar_block", "cinnabar_ore", Blocks.STONE);

        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "niter_ore", "niter_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_niter_ore", "deepslate_niter_ore", Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "niter_block", "niter_ore", Blocks.STONE);

        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "sulfur_ore", "sulfur_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_sulfur_ore", "deepslate_sulfur_ore",
                Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "sulfur_block", "sulfur_ore", Blocks.STONE);

        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "tin_ore", "tin_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_tin_ore", "deepslate_tin_ore", Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "tin_block", "tin_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "tin_gear", "tin_ore", Blocks.STONE);

        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "lead_ore", "lead_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_lead_ore", "deepslate_lead_ore", Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "lead_block", "deepslate_lead_ore", Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "lead_gear", "deepslate_lead_ore", Blocks.DEEPSLATE);

        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "silver_ore", "silver_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_silver_ore", "deepslate_silver_ore",
                Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "silver_block", "deepslate_silver_ore", Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "silver_gear", "deepslate_silver_ore", Blocks.DEEPSLATE);

        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "nickel_ore", "nickel_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_nickel_ore", "deepslate_nickel_ore",
                Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "nickel_block", "nickel_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "nickel_gear", "nickel_ore", Blocks.STONE);

        // Mekanism
        final String MEKANISM_MOD_ID = "mekanism";
        // Mekanism Tools is a separate mod from Mekanism, so I don't feel like making the recipe generation more
        // complex right now

        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "osmium_ore", "osmium_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "deepslate_osmium_ore", "deepslate_osmium_ore",
                Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "osmium_block", "osmium_ore", Blocks.STONE);

        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "uranium_ore", "uranium_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "deepslate_uranium_ore", "deepslate_uranium_ore",
                Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "uranium_block", "deepslate_uranium_ore", Blocks.DEEPSLATE);

        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "fluorite_ore", "fluorite_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "deepslate_fluorite_ore", "deepslate_fluorite_ore",
                Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "fluorite_block", "deepslate_fluorite_ore", Blocks.DEEPSLATE);

        // Mystical Agriculture
        final String MYSTICAL_AGRICULTURE_MOD_ID = "mysticalagriculture";

        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "prosperity_ore", "prosperity_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "deepslate_prosperity_ore",
                "deepslate_prosperity_ore", Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "prosperity_block", "deepslate_prosperity_ore",
                Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "prosperity_gemstone", "deepslate_prosperity_ore",
                Blocks.DEEPSLATE);

        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "inferium_ore", "inferium_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "deepslate_inferium_ore", "deepslate_inferium_ore",
                Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "inferium_block", "inferium_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "inferium_gemstone", "inferium_ore", Blocks.STONE);

        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "soulium_ore", "soulium_ore", Blocks.NETHERRACK);
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "soulium_gemstone", "soulium_ore",
                Blocks.NETHERRACK);

        // Create
        final String CREATE_MOD_ID = "create";

        helper.addSpatialForAnotherMod(CREATE_MOD_ID, "zinc_ore", "zinc_ore", Blocks.STONE);
        helper.addSpatialForAnotherMod(CREATE_MOD_ID, "deepslate_zinc_ore", "deepslate_zinc_ore", Blocks.DEEPSLATE);
        helper.addSpatialForAnotherMod(CREATE_MOD_ID, "zinc_block", "deepslate_zinc_ore", Blocks.DEEPSLATE);
    }

    protected static class BuilderHelper {
        private final Consumer<FinishedRecipe> consumer;

        public BuilderHelper(final Consumer<FinishedRecipe> consumer) {
            this.consumer = consumer;
        }

        public void addNormal(final ItemLike topInput, final ItemLike bottomInput, final ItemLike output) {
            final String recipeName = topInput.asItem().getRegistryName().getPath();
            final FinishedRecipe result = RecipeArgs.builder()
                    .recipeName(recipeName)
                    .topInput(Ingredient.of(topInput))
                    .bottomInput(Ingredient.of(bottomInput))
                    .output(output.asItem().getDefaultInstance())
                    .build().toRecipe();
            consumer.accept(result);
        }

        public void addSpatial(final ItemLike bottomInput, final Block block, final Block baseBlock) {
            final String recipeName = "spatial_" + bottomInput.asItem().getRegistryName().getPath();
            final FinishedRecipe result = RecipeArgs.builder()
                    .recipeName(recipeName)
                    .bottomInput(Ingredient.of(bottomInput))
                    .block(block)
                    .baseBlock(baseBlock)
                    .build().toRecipe();
            consumer.accept(result);
        }

        // In this thread: hacks piled on top of hacks, but it's Officially Recommended so it's okay
        public void addSpatialForAnotherMod(final String otherModId, final String itemRegistryName,
                final String blockRegistryName, final Block baseBlock) {
            final var finishedRecipe = new CustomSpatialRecipeResult(otherModId, itemRegistryName, blockRegistryName,
                    baseBlock);
            ConditionalRecipe.builder()
                    .addCondition(new ModLoadedCondition(otherModId))
                    .addRecipe(finishedRecipe)
                    .build(consumer, ImpracticalEnergisticsMod.MOD_ID, finishedRecipe.recipeName);
        }
    }

    @Builder
    protected static class RecipeArgs {
        private String recipeName;

        private Ingredient topInput;
        private Ingredient bottomInput;
        private ItemStack output;

        private Block block;
        private Block baseBlock;
        private SpecialSpatialRecipeType specialType;

        public FinishedRecipe toRecipe() {
            final SpatialRiftManipulatorRecipe recipe;
            if (block != null) {
                recipe = new SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe(null, bottomInput, block, baseBlock);
            }
            else if (specialType != null) {
                recipe = new SpatialRiftManipulatorRecipe.SpecialSpatialRecipe(null, bottomInput, specialType);
            }
            else if (topInput != null) {
                recipe = new SpatialRiftManipulatorRecipe.GenericManipulatorRecipe(null, topInput, bottomInput, output);
            }
            else {
                throw new IllegalArgumentException("Must set at least one recipe subtype");
            }
            return new CustomRecipeResult(recipeName, recipe);
        }
    }

    protected static class CustomSpatialRecipeResult implements FinishedRecipe {
        private final String recipeName;
        private final String otherModId;
        private final String itemRegistryName;
        private final String blockRegistryName;
        private final Block baseBlock;

        public CustomSpatialRecipeResult(final String otherModId, final String itemRegistryName,
                final String blockRegistryName, final Block baseBlock) {
            this.recipeName = "spatial_" + itemRegistryName;
            this.otherModId = otherModId;
            this.itemRegistryName = itemRegistryName;
            this.blockRegistryName = blockRegistryName;
            this.baseBlock = baseBlock;
        }

        @Override
        public void serializeRecipeData(final JsonObject json) {
            // There is almost certainly a more elegant way to do this, but here we are
            final var bottomInput = new JsonObject();
            bottomInput.addProperty("item", String.format("%s:%s", otherModId, itemRegistryName));
            json.add("bottom_input", bottomInput);

            final JsonObject spatialJson = new JsonObject();
            spatialJson.addProperty("block", String.format("%s:%s", otherModId, blockRegistryName));
            spatialJson.addProperty("baseBlock", baseBlock.getRegistryName().toString());
            json.add("spatial_effect", spatialJson);
        }

        @Override
        public ResourceLocation getId() {
            return ImpenIdUtil.makeId(SpatialRiftManipulatorRecipe.RECIPE_TYPE_NAME + "/" + recipeName);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return SpatialRiftManipulatorRecipe.Serializer.INSTANCE;
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }

    }
}
