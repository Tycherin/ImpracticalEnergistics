package com.tycherin.impen.datagen.recipe;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpenRegistry.BlockDefinition;
import com.tycherin.impen.ImpenRegistry.ItemDefinition;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.util.ImpenIdUtil;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipeSerializer;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

public class ImpenRecipeProvider extends RecipeProvider {

    public ImpenRecipeProvider(final DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void buildCraftingRecipes(final Consumer<FinishedRecipe> consumer) {
        // ***
        // Crafting recipes
        // ***

        // === Machines ===

        // Beam Network
        ShapedRecipeBuilder.shaped(ImpenRegistry.BEAM_NETWORK_EMITTER)
                .pattern("III")
                .pattern("EP ")
                .pattern("III")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('P', ImpenRegistry.RIFT_PRISM)
                .define('E', AEBlocks.ENERGY_CELL)
                .unlockedBy("has_rift_prism", has(ImpenRegistry.RIFT_PRISM))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.BEAM_NETWORK_RECEIVER)
                .pattern("III")
                .pattern("AP ")
                .pattern("III")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('P', ImpenRegistry.RIFT_PRISM)
                .define('A', AEBlocks.ENERGY_ACCEPTOR)
                .unlockedBy("has_rift_prism", has(ImpenRegistry.RIFT_PRISM))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.BEAM_NETWORK_AMPLIFIER)
                .pattern("III")
                .pattern("ACA")
                .pattern("III")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('A', ImpenRegistry.AEROCRYSTAL_PRISM)
                .define('C', AEBlocks.ENERGY_CELL)
                .unlockedBy("has_rift_prism", has(ImpenRegistry.RIFT_PRISM))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.BEAM_NETWORK_MIRROR)
                .pattern("FFF")
                .pattern("FAG")
                .pattern("FG ")
                .define('F', Items.IRON_BARS)
                .define('A', ImpenRegistry.AEROCRYSTAL_PRISM)
                .define('G', Items.GLASS)
                .unlockedBy("has_rift_prism", has(ImpenRegistry.RIFT_PRISM))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.BEAM_NETWORK_SPLITTER)
                .pattern("FFF")
                .pattern("GAG")
                .pattern("FGF")
                .define('F', Items.IRON_BARS)
                .define('A', ImpenRegistry.AEROCRYSTAL_PRISM)
                .define('G', Items.GLASS)
                .unlockedBy("has_rift_prism", has(ImpenRegistry.RIFT_PRISM))
                .save(consumer);
        // Atm. Crystallizer
        ShapedRecipeBuilder.shaped(ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER)
                .pattern(" I ")
                .pattern("CFC")
                .pattern("ICI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('F', AEItems.FLUIX_CRYSTAL)
                .unlockedBy("has_fluix_crystal", has(AEItems.FLUIX_CRYSTAL))
                .save(consumer);
        // Spatial Rift Spawner
        ShapedRecipeBuilder.shaped(ImpenRegistry.SPATIAL_RIFT_SPAWNER)
                .pattern("IRI")
                .pattern("PXP")
                .pattern("ICI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('P', ImpenRegistry.PROCESSOR_QUANTIZED)
                .define('R', Items.REDSTONE_BLOCK)
                .define('X', ImpenRegistry.SPATIAL_MACHINE_CORE)
                .unlockedBy("has_aerocrystal", has(ImpenRegistry.AEROCRYSTAL))
                .save(consumer);
        // Spatial Rift Manipulator
        ShapedRecipeBuilder.shaped(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR)
                .pattern("IRI")
                .pattern("QXQ")
                .pattern("ICI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('R', ImpenRegistry.RIFT_SHARD)
                .define('Q', ImpenRegistry.PROCESSOR_EQUALIZED)
                .define('X', ImpenRegistry.SPATIAL_MACHINE_CORE)
                .unlockedBy("has_rift_shard", has(ImpenRegistry.RIFT_SHARD))
                .save(consumer);
        // Spatial Rift Collapser
        ShapedRecipeBuilder.shaped(ImpenRegistry.SPATIAL_RIFT_COLLAPSER)
                .pattern("IPI")
                .pattern("RXR")
                .pattern("ICI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('R', ImpenRegistry.PROCESSOR_MAGNIFIED)
                .define('P', ImpenRegistry.RIFT_PRISM)
                .define('X', ImpenRegistry.SPATIAL_MACHINE_CORE)
                .unlockedBy("has_rift_prism", has(ImpenRegistry.RIFT_PRISM))
                .save(consumer);
        // Ejection Drive
        ShapedRecipeBuilder.shaped(ImpenRegistry.EJECTION_DRIVE)
                .pattern("PCD")
                .define('P', Items.PISTON)
                .define('C', AEBlocks.CHEST)
                .define('D', Items.DROPPER)
                .unlockedBy("has_me_chest", has(AEBlocks.CHEST))
                .save(consumer);
        // Phase Field Controller
        ShapedRecipeBuilder.shaped(ImpenRegistry.PHASE_FIELD_CONTROLLER)
                .pattern("IPI")
                .pattern("IRI")
                .pattern("IQI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('P', ImpenRegistry.STABILIZED_RIFT_PRISM)
                .define('R', ImpenRegistry.PROCESSOR_MAGNIFIED)
                .define('Q', ImpenRegistry.PROCESSOR_EQUALIZED)
                .unlockedBy("has_stabilized_rift_prism", has(ImpenRegistry.STABILIZED_RIFT_PRISM))
                .save(consumer);
        // Capture Plane (imitating AE2 recipes for Annihilation & Formation Planes)
        ShapedRecipeBuilder.shaped(ImpenRegistry.CAPTURE_PLANE_ITEM)
                .pattern("AB ")
                .pattern("CB ")
                .pattern("AB ")
                .define('A', Tags.Items.INGOTS_IRON)
                .define('C', ImpenRegistry.AEROCRYSTAL_PRISM)
                .define('B', ConventionTags.ALL_FLUIX)
                .unlockedBy("has_aerocrystal_prism", has(ImpenRegistry.AEROCRYSTAL_PRISM))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.CAPTURE_PLANE_ITEM, "vertical"));
        ShapedRecipeBuilder.shaped(ImpenRegistry.CAPTURE_PLANE_ITEM)
                .pattern("BBB")
                .pattern("ACA")
                .define('A', Tags.Items.INGOTS_IRON)
                .define('C', ImpenRegistry.AEROCRYSTAL_PRISM)
                .define('B', ConventionTags.ALL_FLUIX)
                .unlockedBy("has_aerocrystal_prism", has(ImpenRegistry.AEROCRYSTAL_PRISM))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.CAPTURE_PLANE_ITEM, "horizontal"));

        // === Materials ===

        // Riftstone Brick
        ShapedRecipeBuilder.shaped(ImpenRegistry.RIFTSTONE_BRICK, 4)
                .pattern("##")
                .pattern("##")
                .define('#', ImpenRegistry.RIFTSTONE)
                .unlockedBy("has_riftstone", has(ImpenRegistry.RIFTSTONE))
                .save(consumer);
        // Lunchbox Cell
        ShapedRecipeBuilder.shaped(ImpenRegistry.LUNCHBOX_CELL_ITEM)
                .pattern("IPI")
                .pattern("ASB")
                .define('P', ImpenRegistry.PROCESSOR_QUANTIZED)
                .define('I', Items.IRON_INGOT)
                .define('A', Items.APPLE)
                .define('B', Items.BREAD)
                .define('S', AEItems.ITEM_CELL_1K)
                .unlockedBy("has_aerocrystal", has(ImpenRegistry.AEROCRYSTAL))
                .save(consumer);
        // Aerocrystal Prism
        ShapedRecipeBuilder.shaped(ImpenRegistry.AEROCRYSTAL_PRISM)
                .pattern(" # ")
                .pattern("#$#")
                .pattern(" # ")
                .define('#', ImpenRegistry.AEROCRYSTAL)
                .define('$', Blocks.GLASS)
                .unlockedBy("has_aerocrystal", has(ImpenRegistry.AEROCRYSTAL))
                .save(consumer);
        // Aerocrystal Assembly
        ShapedRecipeBuilder.shaped(ImpenRegistry.AEROCRYSTAL_ASSEMBLY)
                .pattern("DAA")
                .pattern("DXB")
                .pattern("CCB")
                .define('X', Items.IRON_BARS)
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('C', AEItems.CERTUS_QUARTZ_CRYSTAL)
                .define('D', ImpenRegistry.EXOTIC_AEROCRYSTAL)
                .unlockedBy("has_exotic_aerocrystal", has(ImpenRegistry.EXOTIC_AEROCRYSTAL))
                .save(consumer);
        // Spatial Machine Core
        ShapedRecipeBuilder.shaped(ImpenRegistry.SPATIAL_MACHINE_CORE)
                .pattern("IGI")
                .pattern("OAO")
                .pattern("IGI")
                .define('I', Items.IRON_INGOT)
                .define('O', Items.OBSIDIAN)
                .define('G', Items.GOLD_INGOT)
                .define('A', ImpenRegistry.AEROCRYSTAL_ASSEMBLY)
                .unlockedBy("has_aerocrystal_assembly", has(ImpenRegistry.AEROCRYSTAL_ASSEMBLY))
                .save(consumer);

        stairBuilder(ImpenRegistry.RIFTSTONE_STAIRS, Ingredient.of(ImpenRegistry.RIFTSTONE))
                .unlockedBy("has_riftstone", has(ImpenRegistry.RIFTSTONE))
                .save(consumer);
        stairBuilder(ImpenRegistry.RIFTSTONE_BRICK_STAIRS, Ingredient.of(ImpenRegistry.RIFTSTONE_BRICK))
                .unlockedBy("has_riftstone_brick", has(ImpenRegistry.RIFTSTONE_BRICK))
                .save(consumer);
        stairBuilder(ImpenRegistry.SMOOTH_RIFTSTONE_STAIRS, Ingredient.of(ImpenRegistry.SMOOTH_RIFTSTONE))
                .unlockedBy("has_smooth_riftstone", has(ImpenRegistry.SMOOTH_RIFTSTONE))
                .save(consumer);

        slabBuilder(ImpenRegistry.RIFTSTONE_SLAB, Ingredient.of(ImpenRegistry.RIFTSTONE))
                .unlockedBy("has_riftstone", has(ImpenRegistry.RIFTSTONE))
                .save(consumer);
        slabBuilder(ImpenRegistry.RIFTSTONE_BRICK_SLAB, Ingredient.of(ImpenRegistry.RIFTSTONE_BRICK))
                .unlockedBy("has_riftstone_brick", has(ImpenRegistry.RIFTSTONE_BRICK))
                .save(consumer);
        slabBuilder(ImpenRegistry.SMOOTH_RIFTSTONE_SLAB, Ingredient.of(ImpenRegistry.SMOOTH_RIFTSTONE))
                .unlockedBy("has_smooth_riftstone", has(ImpenRegistry.SMOOTH_RIFTSTONE))
                .save(consumer);

        // === Phasic Capsules ===

        ShapedRecipeBuilder.shaped(ImpenRegistry.PHASIC_CAPSULE_EMPTY)
                .pattern(" A ")
                .pattern("P P")
                .pattern(" P ")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('P', Blocks.GLASS_PANE)
                .unlockedBy("has_aerocrystal", has(ImpenRegistry.AEROCRYSTAL))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.PHASIC_CAPSULE_RED)
                .pattern(" B ")
                .pattern(" A ")
                .pattern(" # ")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', Items.STONE_SWORD)
                .define('#', ImpenRegistry.PHASIC_CAPSULE_EMPTY)
                .unlockedBy("has_phasic_capsule", has(ImpenRegistry.PHASIC_CAPSULE_EMPTY))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.PHASIC_CAPSULE_ORANGE)
                .pattern(" B ")
                .pattern(" A ")
                .pattern(" # ")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', Items.OBSERVER)
                .define('#', ImpenRegistry.PHASIC_CAPSULE_EMPTY)
                .unlockedBy("has_phasic_capsule", has(ImpenRegistry.PHASIC_CAPSULE_EMPTY))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.PHASIC_CAPSULE_YELLOW)
                .pattern(" B ")
                .pattern(" A ")
                .pattern(" # ")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', Items.EGG)
                .define('#', ImpenRegistry.PHASIC_CAPSULE_EMPTY)
                .unlockedBy("has_phasic_capsule", has(ImpenRegistry.PHASIC_CAPSULE_EMPTY))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.PHASIC_CAPSULE_LIME)
                .pattern(" B ")
                .pattern(" A ")
                .pattern(" # ")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', Items.APPLE)
                .define('#', ImpenRegistry.PHASIC_CAPSULE_EMPTY)
                .unlockedBy("has_phasic_capsule", has(ImpenRegistry.PHASIC_CAPSULE_EMPTY))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.PHASIC_CAPSULE_WHITE)
                .pattern(" B ")
                .pattern(" A ")
                .pattern(" # ")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', Items.RABBIT_FOOT)
                .define('#', ImpenRegistry.PHASIC_CAPSULE_EMPTY)
                .unlockedBy("has_phasic_capsule", has(ImpenRegistry.PHASIC_CAPSULE_EMPTY))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.PHASIC_CAPSULE_GREEN)
                .pattern(" B ")
                .pattern(" A ")
                .pattern(" # ")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', Items.WHEAT)
                .define('#', ImpenRegistry.PHASIC_CAPSULE_EMPTY)
                .unlockedBy("has_phasic_capsule", has(ImpenRegistry.PHASIC_CAPSULE_EMPTY))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.PHASIC_CAPSULE_BLUE)
                .pattern(" B ")
                .pattern(" A ")
                .pattern(" # ")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', Items.LAPIS_LAZULI)
                .define('#', ImpenRegistry.PHASIC_CAPSULE_EMPTY)
                .unlockedBy("has_phasic_capsule", has(ImpenRegistry.PHASIC_CAPSULE_EMPTY))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.PHASIC_CAPSULE_GRAY)
                .pattern(" B ")
                .pattern(" A ")
                .pattern(" # ")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', Items.GLASS)
                .define('#', ImpenRegistry.PHASIC_CAPSULE_EMPTY)
                .unlockedBy("has_phasic_capsule", has(ImpenRegistry.PHASIC_CAPSULE_EMPTY))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.PHASIC_CAPSULE_PALE)
                .pattern(" B ")
                .pattern(" A ")
                .pattern(" # ")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', Items.BOW)
                .define('#', ImpenRegistry.PHASIC_CAPSULE_EMPTY)
                .unlockedBy("has_phasic_capsule", has(ImpenRegistry.PHASIC_CAPSULE_EMPTY))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.PHASIC_CAPSULE_SLIMY)
                .pattern(" B ")
                .pattern(" A ")
                .pattern(" # ")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', Items.SLIME_BALL)
                .define('#', ImpenRegistry.PHASIC_CAPSULE_EMPTY)
                .unlockedBy("has_phasic_capsule", has(ImpenRegistry.PHASIC_CAPSULE_EMPTY))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ImpenRegistry.PHASIC_CAPSULE_FLORAL)
                .pattern(" B ")
                .pattern(" A ")
                .pattern(" # ")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', Items.BONE_MEAL)
                .define('#', ImpenRegistry.PHASIC_CAPSULE_EMPTY)
                .unlockedBy("has_phasic_capsule", has(ImpenRegistry.PHASIC_CAPSULE_EMPTY))
                .save(consumer);

        // === Storage Blocks ===

        nineBlockStorageRecipes(consumer, ImpenRegistry.AEROCRYSTAL, ImpenRegistry.AEROCRYSTAL_BLOCK);
        nineBlockStorageRecipes(consumer, ImpenRegistry.BLAZING_AEROCRYSTAL, ImpenRegistry.BLAZING_AEROCRYSTAL_BLOCK);
        nineBlockStorageRecipes(consumer, ImpenRegistry.EXOTIC_AEROCRYSTAL, ImpenRegistry.EXOTIC_AEROCRYSTAL_BLOCK);
        nineBlockStorageRecipes(consumer, ImpenRegistry.RIFT_SHARD, ImpenRegistry.RIFT_SHARD_BLOCK);
        nineBlockStorageRecipes(consumer, ImpenRegistry.RIFT_ALLOY_INGOT, ImpenRegistry.RIFT_ALLOY_BLOCK);

        // ***
        // Other Recipe Types
        // ***

        // Smelting recipes
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ImpenRegistry.RIFTSTONE), ImpenRegistry.SMOOTH_RIFTSTONE, 0.1F, 200)
                .unlockedBy("has_riftstone", has(ImpenRegistry.RIFTSTONE))
                .save(consumer);
        // TODO Change this to a conditional recipe & add conditional alternatives for Thermal & Mek instead
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ImpenRegistry.RIFT_SHARD_BLOCK), ImpenRegistry.RIFTSTONE_DUST, 0.1F, 200)
                .unlockedBy("has_rift_shard", has(ImpenRegistry.RIFT_SHARD))
                .save(consumer);

        addOreRecipes(consumer, ImpenRegistry.RIFT_SHARD_ORE, ImpenRegistry.RIFT_SHARD);
        addOreRecipes(consumer, ImpenRegistry.NETHER_GLOWSTONE_ORE, Items.GLOWSTONE);
        addOreRecipes(consumer, ImpenRegistry.NETHER_DEBRIS_ORE, Items.NETHERITE_SCRAP);
        addOreRecipes(consumer, ImpenRegistry.END_AMETHYST_ORE, Items.AMETHYST_SHARD);
        addOreRecipes(consumer, ImpenRegistry.STABILIZED_RIFT_PRISM, ImpenRegistry.RIFT_SHARD);

        // Stonecutter recipes
        stonecutterResultFromBaseOverride(consumer, ImpenRegistry.RIFTSTONE_BRICK, ImpenRegistry.SMOOTH_RIFTSTONE);

        // Inscriber recipes
        InscriberRecipeBuilder.forOutput(ImpenRegistry.CIRCUIT_QUANTIZED)
                .setTop(Ingredient.of(AEItems.ENGINEERING_PROCESSOR_PRESS))
                .setMiddle(Ingredient.of(ImpenRegistry.AEROCRYSTAL))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(consumer);
        InscriberRecipeBuilder.forOutput(ImpenRegistry.CIRCUIT_MAGNIFIED)
                .setTop(Ingredient.of(AEItems.LOGIC_PROCESSOR_PRESS))
                .setMiddle(Ingredient.of(ImpenRegistry.BLAZING_AEROCRYSTAL))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(consumer);
        InscriberRecipeBuilder.forOutput(ImpenRegistry.CIRCUIT_EQUALIZED)
                .setTop(Ingredient.of(AEItems.CALCULATION_PROCESSOR_PRESS))
                .setMiddle(Ingredient.of(ImpenRegistry.EXOTIC_AEROCRYSTAL))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(consumer);

        InscriberRecipeBuilder.forOutput(ImpenRegistry.PROCESSOR_QUANTIZED)
                .setTop(Ingredient.of(ImpenRegistry.CIRCUIT_QUANTIZED))
                .setMiddle(Ingredient.of(Items.REDSTONE))
                .setBottom(Ingredient.of(AEItems.SILICON_PRINT))
                .setMode(InscriberProcessType.PRESS)
                .save(consumer);
        InscriberRecipeBuilder.forOutput(ImpenRegistry.PROCESSOR_MAGNIFIED)
                .setTop(Ingredient.of(ImpenRegistry.CIRCUIT_MAGNIFIED))
                .setMiddle(Ingredient.of(Items.REDSTONE))
                .setBottom(Ingredient.of(AEItems.SILICON_PRINT))
                .setMode(InscriberProcessType.PRESS)
                .save(consumer);
        InscriberRecipeBuilder.forOutput(ImpenRegistry.PROCESSOR_EQUALIZED)
                .setTop(Ingredient.of(ImpenRegistry.CIRCUIT_EQUALIZED))
                .setMiddle(Ingredient.of(Items.REDSTONE))
                .setBottom(Ingredient.of(AEItems.SILICON_PRINT))
                .setMode(InscriberProcessType.PRESS)
                .save(consumer);

        // Custom recipe categories
        new SpatialRiftSpawnerRecipeProvider().addRecipes(consumer);
        new SpatialRiftCollapserRecipeProvider().addRecipes(consumer);
        new SpatialRiftManipulatorBaseBlockRecipeProvider().addRecipes(consumer);
        new SpatialRiftManipulatorBlockWeightRecipeProvider().addRecipes(consumer);
        new SpatialRiftManipulatorCraftingRecipeProvider().addRecipes(consumer);
        new SpatialRiftManipulatorSpecialRecipeProvider().addRecipes(consumer);

        // TODO Add compatibility recipes with other mods (particularly Rift Alloy)
    }

    // These methods shadow or copy vanilla methods because that code assumes the vanilla namespace, and we want it to
    // use our mod's namespace instead

    private void nineBlockStorageRecipes(final Consumer<FinishedRecipe> consumer, final ItemDefinition item,
            final BlockDefinition block) {
        // By default, this method will try to namespace the recipe into the vanilla namespace, so we need to force it
        // to use our namespace instead
        nineBlockStorageRecipes(consumer, item, block, ImpracticalEnergisticsMod.MOD_ID + ":" + item.getKey(), null,
                ImpracticalEnergisticsMod.MOD_ID + ":" + block.getKey(), null);
    }

    private void addOreRecipes(final Consumer<FinishedRecipe> consumer, final ItemLike oreBlock,
            final ItemLike product) {
        oreSmeltingOverride(consumer, Collections.singletonList(oreBlock), product, 0.7f, 200, null);
        oreBlastingOverride(consumer, Collections.singletonList(oreBlock), product, 0.7f, 100, null);
    }

    private void oreSmeltingOverride(Consumer<FinishedRecipe> p_176592_, List<ItemLike> p_176593_, ItemLike p_176594_,
            float p_176595_, int p_176596_, String p_176597_) {
        oreCookingOverride(p_176592_, RecipeSerializer.SMELTING_RECIPE, p_176593_, p_176594_, p_176595_, p_176596_,
                p_176597_, "_from_smelting");
    }

    private void oreBlastingOverride(Consumer<FinishedRecipe> p_176626_, List<ItemLike> p_176627_, ItemLike p_176628_,
            float p_176629_, int p_176630_, String p_176631_) {
        oreCookingOverride(p_176626_, RecipeSerializer.BLASTING_RECIPE, p_176627_, p_176628_, p_176629_, p_176630_,
                p_176631_, "_from_blasting");
    }

    private void oreCookingOverride(Consumer<FinishedRecipe> p_176534_, SimpleCookingSerializer<?> p_176535_,
            List<ItemLike> p_176536_, ItemLike p_176537_, float p_176538_, int p_176539_, String p_176540_,
            String p_176541_) {
        for (ItemLike itemlike : p_176536_) {
            SimpleCookingRecipeBuilder.cooking(Ingredient.of(itemlike), p_176537_, p_176538_, p_176539_, p_176535_)
                    .group(p_176540_).unlockedBy(getHasName(itemlike), has(itemlike))
                    // This is the bit we actually need to override
                    .save(p_176534_,
                            ImpenIdUtil.makeId(getItemName(p_176537_) + p_176541_ + "_" + getItemName(itemlike)));
        }
    }

    private void stonecutterResultFromBaseOverride(Consumer<FinishedRecipe> p_176736_, ItemLike p_176737_,
            ItemLike p_176738_) {
        stonecutterResultFromBaseOverride(p_176736_, p_176737_, p_176738_, 1);
    }

    private void stonecutterResultFromBaseOverride(Consumer<FinishedRecipe> p_176547_, ItemLike p_176548_,
            ItemLike p_176549_, int p_176550_) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(p_176549_), p_176548_, p_176550_)
                .unlockedBy(getHasName(p_176549_), has(p_176549_))
                // This is the bit we actually need to override
                .save(p_176547_, ImpenIdUtil.makeId(getConversionRecipeName(p_176548_, p_176549_) + "_stonecutting"));
    }

    // The equivalent class in AE2 isn't visible, so here's this nonsense
    protected static class InscriberRecipeBuilder {
        private final String recipeName;
        private Ingredient topInput;
        private Ingredient middleInput;
        private Ingredient bottomInput;
        private ItemStack output;
        private InscriberProcessType mode = InscriberProcessType.INSCRIBE;

        public static InscriberRecipeBuilder forOutput(final ItemLike item) {
            return InscriberRecipeBuilder.forOutput(item.asItem().getDefaultInstance());
        }

        public static InscriberRecipeBuilder forOutput(final ItemStack output) {
            final InscriberRecipeBuilder builder = new InscriberRecipeBuilder(
                    output.getItem().getRegistryName().getPath());
            builder.setOutput(output);
            return builder;
        }

        public InscriberRecipeBuilder(final String recipeName) {
            this.recipeName = recipeName;
        }

        public InscriberRecipeBuilder setTop(final Ingredient topInput) {
            this.topInput = topInput;
            return this;
        }

        public InscriberRecipeBuilder setMiddle(final Ingredient middleInput) {
            this.middleInput = middleInput;
            return this;
        }

        public InscriberRecipeBuilder setBottom(final Ingredient bottomInput) {
            this.bottomInput = bottomInput;
            return this;
        }

        public InscriberRecipeBuilder setOutput(final ItemStack output) {
            this.output = output;
            return this;
        }

        public InscriberRecipeBuilder setMode(final InscriberProcessType processType) {
            this.mode = processType;
            return this;
        }

        public void save(final Consumer<FinishedRecipe> consumer) {
            consumer.accept(new RecipeResult());
        }

        private class RecipeResult implements FinishedRecipe {

            @Override
            public void serializeRecipeData(JsonObject json) {
                json.addProperty("mode", mode.name().toLowerCase(Locale.ROOT));
                json.add("result", AE2RecipeProvider.toJson(output));
                var ingredients = new JsonObject();
                ingredients.add("middle", middleInput.toJson());
                if (topInput != null) {
                    ingredients.add("top", topInput.toJson());
                }
                if (bottomInput != null) {
                    ingredients.add("bottom", bottomInput.toJson());
                }
                json.add("ingredients", ingredients);
            }

            @Override
            public ResourceLocation getId() {
                return ImpenIdUtil.makeId("inscriber/" + recipeName);
            }

            @Override
            public RecipeSerializer<?> getType() {
                return InscriberRecipeSerializer.INSTANCE;
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
