package com.tycherin.impen.datagen;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe.SpecialSpatialRecipe.SpecialSpatialRecipeType;
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
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;

public class SpatialRiftManipulatorRecipeProvider {

    public static final String RECIPE_TYPE_NAME = "spatial_rift_manipulator";

    public SpatialRiftManipulatorRecipeProvider() {
    }

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        final BuilderHelper helper = new BuilderHelper(consumer);
        
        consumer.accept(new RecipeBuilder()
                .recipeName("spatial_clear_precision")
                .bottomInput(Ingredient.of(Tags.Items.GLASS))
                .hasSpecialSpatialEffect(SpecialSpatialRecipeType.BOOST_PRECISION)
                .build());
        consumer.accept(new RecipeBuilder()
                .recipeName("spatial_boost_precision")
                .bottomInput(Ingredient.of(ImpenRegistry.STABILIZED_RIFT_PRISM))
                .hasSpecialSpatialEffect(SpecialSpatialRecipeType.BOOST_PRECISION)
                .build());

        helper.addNormal(ImpenRegistry.RIFTSTONE, Items.IRON_PICKAXE, ImpenRegistry.RIFTSTONE_DUST);

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

        helper.addSpatial(Items.OBSIDIAN, Blocks.OBSIDIAN);

        helper.addSpatial(Items.GLOWSTONE, ImpenRegistry.NETHER_GLOWSTONE_ORE.asBlock());
        helper.addSpatial(Items.NETHERITE_INGOT, ImpenRegistry.NETHER_DEBRIS_ORE.asBlock());
        helper.addSpatial(Items.AMETHYST_BLOCK, ImpenRegistry.END_AMETHYST_ORE.asBlock());
        helper.addSpatial(Items.MUSHROOM_STEW, ImpenRegistry.MUSHROOM_DIRT.asBlock());

        // And now, a big pile of recipes for modded compatibility
        // The fact that this works is kind of impressive
        
        // Thermal Foundation
        final String THERMAL_MOD_ID = "thermal";
        
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "apatite_ore", "apatite_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_apatite_ore", "deepslate_apatite_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "apatite_block", "apatite_ore");
        
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "cinnabar_ore", "cinnabar_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_cinnabar_ore", "deepslate_cinnabar_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "cinnabar_block", "cinnabar_ore");
        
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "niter_ore", "niter_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_niter_ore", "deepslate_niter_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "niter_block", "niter_ore");
        
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "sulfur_ore", "sulfur_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_sulfur_ore", "deepslate_sulfur_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "sulfur_block", "sulfur_ore");
        
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "tin_ore", "tin_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_tin_ore", "deepslate_tin_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "tin_block", "tin_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "tin_gear", "tin_ore");
        
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "lead_ore", "lead_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_lead_ore", "deepslate_lead_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "lead_block", "deepslate_lead_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "lead_gear", "deepslate_lead_ore");
        
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "silver_ore", "silver_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_silver_ore", "deepslate_silver_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "silver_block", "deepslate_silver_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "silver_gear", "deepslate_silver_ore");
        
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "nickel_ore", "nickel_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "deepslate_nickel_ore", "deepslate_nickel_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "nickel_block", "nickel_ore");
        helper.addSpatialForAnotherMod(THERMAL_MOD_ID, "nickel_gear", "nickel_ore");
        
        // Mekanism
        final String MEKANISM_MOD_ID = "mekanism";
        // Mekanism Tools is a separate mod from Mekanism, so I don't feel like making the recipe generation more
        // complex right now
        
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "osmium_ore", "osmium_ore");
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "deepslate_osmium_ore", "deepslate_osmium_ore");
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "osmium_block", "osmium_ore");
        
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "uranium_ore", "uranium_ore");
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "deepslate_uranium_ore", "deepslate_uranium_ore");
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "uranium_block", "deepslate_uranium_ore");
        
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "fluorite_ore", "fluorite_ore");
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "deepslate_fluorite_ore", "deepslate_fluorite_ore");
        helper.addSpatialForAnotherMod(MEKANISM_MOD_ID, "fluorite_block", "deepslate_fluorite_ore");
        
        // Mystical Agriculture
        final String MYSTICAL_AGRICULTURE_MOD_ID = "mysticalagriculture";
        
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "prosperity_ore", "prosperity_ore");
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "deepslate_prosperity_ore", "deepslate_prosperity_ore");
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "prosperity_block", "deepslate_prosperity_ore");
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "prosperity_gemstone", "deepslate_prosperity_ore");
        
        
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "inferium_ore", "inferium_ore");
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "deepslate_inferium_ore", "deepslate_inferium_ore");
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "inferium_block", "inferium_ore");
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "inferium_gemstone", "inferium_ore");
        
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "soulium_ore", "soulium_ore");
        helper.addSpatialForAnotherMod(MYSTICAL_AGRICULTURE_MOD_ID, "soulium_gemstone", "soulium_ore");
        
        // Create
        final String CREATE_MOD_ID = "create";
        
        helper.addSpatialForAnotherMod(CREATE_MOD_ID, "zinc_ore", "zinc_ore");
        helper.addSpatialForAnotherMod(CREATE_MOD_ID, "deepslate_zinc_ore", "deepslate_zinc_ore");
        helper.addSpatialForAnotherMod(CREATE_MOD_ID, "zinc_block", "deepslate_zinc_ore");
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

        // In this thread: hacks piled on top of hacks, but it's Officially Recommended so it's okay
        public void addSpatialForAnotherMod(final String otherModId, final String itemRegistryName,
                final String blockRegistryName) {
            final var finishedRecipe = new CustomSpatialRecipeResult(otherModId, itemRegistryName, blockRegistryName);
            ConditionalRecipe.builder()
                    .addCondition(new ModLoadedCondition(otherModId))
                    .addRecipe(finishedRecipe)
                    .build(consumer, ImpracticalEnergisticsMod.MOD_ID, finishedRecipe.recipeName);
        }
    }

    private static class RecipeBuilder {
        private String recipeName;

        private ItemStack topInput;
        private Ingredient bottomInput;
        private ItemStack output;

        private Block block;
        private SpecialSpatialRecipeType specialType;

        public RecipeResult build() {
            if (recipeName == null) {
                throw new RuntimeException("Recipe name cannot be null");
            }
            return new RecipeResult();
        }

        public RecipeBuilder hasSpecialSpatialEffect(final SpecialSpatialRecipeType specialType) {
            this.specialType = specialType;
            return this;
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
                else if (specialType != null) {
                    final JsonObject spatialJson = new JsonObject();
                    spatialJson.addProperty("special_effect", specialType.toString());
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
    
    private static class CustomSpatialRecipeResult implements FinishedRecipe {
        private final String recipeName;
        private final String otherModId;
        private final String itemRegistryName;
        private final String blockRegistryName;
        
        public CustomSpatialRecipeResult(final String otherModId,
                final String itemRegistryName, final String blockRegistryName) {
            this.recipeName = "spatial_" + itemRegistryName;
            this.otherModId = otherModId;
            this.itemRegistryName = itemRegistryName;
            this.blockRegistryName = blockRegistryName;
        }

        @Override
        public void serializeRecipeData(final JsonObject json) {
            // There is almost certainly a more elegant way to do this, but here we are
            final var bottomInput = new JsonObject();
            bottomInput.addProperty("item", String.format("%s:%s", otherModId, itemRegistryName));
            json.add("bottom_input", bottomInput);

            final JsonObject spatialJson = new JsonObject();
            spatialJson.addProperty("block", String.format("%s:%s", otherModId, blockRegistryName));
            json.add("spatial_effect", spatialJson);
        }

        @Override
        public ResourceLocation getId() {
            return ImpenIdUtil.makeId(RECIPE_TYPE_NAME + "/" + recipeName);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return SpatialRiftManipulatorRecipeSerializer.INSTANCE;
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
