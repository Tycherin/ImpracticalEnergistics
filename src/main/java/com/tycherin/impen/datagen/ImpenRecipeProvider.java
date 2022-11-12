package com.tycherin.impen.datagen;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpenRegistry.BlockDefinition;
import com.tycherin.impen.ImpenRegistry.ItemDefinition;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.util.ImpenIdUtil;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
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

        // BNL
        ShapedRecipeBuilder.shaped(ImpenRegistry.BEAMED_NETWORK_LINK)
                .pattern("III")
                .pattern("EGP")
                .pattern("III")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('P', ImpenRegistry.RIFT_PRISM)
                .define('E', AEBlocks.ENERGY_CELL)
                .define('G', Blocks.GLASS)
                .unlockedBy("has_rift_prism", has(ImpenRegistry.RIFT_PRISM))
                .save(consumer);
        // TODO Advanced BNL
        // SRM
        ShapedRecipeBuilder.shaped(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR)
                .pattern("III")
                .pattern("PXP")
                .pattern("IRI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('P', ImpenRegistry.RIFT_PRISM)
                .define('X', AEBlocks.SPATIAL_IO_PORT)
                .define('R', Blocks.REDSTONE_BLOCK)
                .unlockedBy("has_rift_prism", has(ImpenRegistry.RIFT_PRISM))
                .save(consumer);
        // SRS
        ShapedRecipeBuilder.shaped(ImpenRegistry.SPATIAL_RIFT_STABILIZER)
                .pattern("III")
                .pattern("PXP")
                .pattern("IRI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('P', ImpenRegistry.RIFT_PRISM)
                .define('X', AEBlocks.SPATIAL_PYLON)
                .define('R', Items.REDSTONE)
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
        // Toaster Drive
        ShapedRecipeBuilder.shaped(ImpenRegistry.EJECTION_DRIVE)
                .pattern("BCB")
                .define('B', Items.BREAD)
                .define('C', AEBlocks.CHEST)
                .unlockedBy("has_me_chest", has(AEBlocks.CHEST))
                .save(consumer);
        // Possibility Disintegrator
        ShapedRecipeBuilder.shaped(ImpenRegistry.POSSIBILITY_DISINTEGRATOR)
                .pattern("IPI")
                .pattern("IRI")
                .pattern("IRI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('P', ImpenRegistry.STABILIZED_RIFT_PRISM)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_stabilized_rift_prism", has(ImpenRegistry.STABILIZED_RIFT_PRISM))
                .save(consumer);
        // Capture Plane (imitating AE2 recipes for Annihilation & Formation Planes)
        ShapedRecipeBuilder.shaped(ImpenRegistry.CAPTURE_PLANE_ITEM)
                .pattern("AB ")
                .pattern("CB ")
                .pattern("AB ")
                .define('A', Tags.Items.INGOTS_IRON)
                .define('C', ImpenRegistry.RIFT_PRISM)
                .define('B', ConventionTags.ALL_FLUIX)
                .unlockedBy("has_rift_prism", has(ImpenRegistry.RIFT_PRISM))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.CAPTURE_PLANE_ITEM, "vertical"));
        ShapedRecipeBuilder.shaped(ImpenRegistry.CAPTURE_PLANE_ITEM)
                .pattern("AAA")
                .pattern("BCB")
                .define('A', Tags.Items.INGOTS_IRON)
                .define('C', ImpenRegistry.RIFT_PRISM)
                .define('B', ConventionTags.ALL_FLUIX)
                .unlockedBy("has_rift_prism", has(ImpenRegistry.RIFT_PRISM))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.CAPTURE_PLANE_ITEM, "horizontal"));

        // === Materials ===

        // Riftstone Brick
        ShapedRecipeBuilder.shaped(ImpenRegistry.RIFTSTONE_BRICKS, 4)
                .pattern("##")
                .pattern("##")
                .define('#', ImpenRegistry.SMOOTH_RIFTSTONE)
                .unlockedBy("has_smooth_riftstone", has(ImpenRegistry.SMOOTH_RIFTSTONE))
                .save(consumer);
        // Rift Glass
        ShapedRecipeBuilder.shaped(ImpenRegistry.RIFT_GLASS, 4)
                .pattern(" G ")
                .pattern("GRG")
                .pattern(" G ")
                .define('G', Blocks.GLASS)
                .define('R', ImpenRegistry.SMOOTH_RIFTSTONE)
                .unlockedBy("has_smooth_riftstone", has(ImpenRegistry.SMOOTH_RIFTSTONE))
                .save(consumer);

        // Lunchbox Cell
        ShapedRecipeBuilder.shaped(ImpenRegistry.LUNCHBOX_CELL_ITEM)
                .pattern("CCC")
                .pattern("ASB")
                .define('C', ImpenRegistry.AEROCRYSTAL)
                .define('A', Items.APPLE)
                .define('B', Items.BREAD)
                .define('S', AEItems.ITEM_CELL_1K)
                .unlockedBy("has_smooth_riftstone", has(ImpenRegistry.SMOOTH_RIFTSTONE))
                .save(consumer);
        // Rift Prism
        ShapedRecipeBuilder.shaped(ImpenRegistry.RIFT_PRISM)
                .pattern(" # ")
                .pattern("#$#")
                .pattern(" # ")
                .define('#', ImpenRegistry.AEROCRYSTAL)
                .define('$', Blocks.GLASS)
                .unlockedBy("has_aerocrystal", has(ImpenRegistry.AEROCRYSTAL))
                .save(consumer);
        // Stabilized Rift Prism
        ShapedRecipeBuilder.shaped(ImpenRegistry.STABILIZED_RIFT_PRISM)
                .pattern("###")
                .pattern("APF")
                .pattern("###")
                .define('#', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('P', ImpenRegistry.RIFT_PRISM)
                .define('A', AEItems.ANNIHILATION_CORE)
                .define('F', AEItems.FORMATION_CORE)
                .unlockedBy("has_blazing_aerocrystal", has(ImpenRegistry.BLAZING_AEROCRYSTAL))
                .save(consumer);

        // === Rift Catalysts ===

        // T1 Rift Catalyst Recipes
        ShapedRecipeBuilder.shaped(ImpenRegistry.BASIC_RIFT_CATALYST)
                .pattern("A A")
                .pattern(" C ")
                .pattern("A A")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('C', AEItems.CERTUS_QUARTZ_CRYSTAL)
                .unlockedBy("has_aerocrystal", has(ImpenRegistry.AEROCRYSTAL))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.BASIC_RIFT_CATALYST, "1"));
        ShapedRecipeBuilder.shaped(ImpenRegistry.BASIC_RIFT_CATALYST, 4)
                .pattern("A B")
                .pattern(" C ")
                .pattern("B A")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('C', AEItems.CERTUS_QUARTZ_CRYSTAL)
                .unlockedBy("has_blazing_aerocrystal", has(ImpenRegistry.BLAZING_AEROCRYSTAL))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.BASIC_RIFT_CATALYST, "4"));
        ShapedRecipeBuilder.shaped(ImpenRegistry.BASIC_RIFT_CATALYST, 16)
                .pattern("AEB")
                .pattern(" C ")
                .pattern("BEA")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('E', ImpenRegistry.EXOTIC_AEROCRYSTAL)
                .define('C', AEItems.CERTUS_QUARTZ_CRYSTAL)
                .unlockedBy("has_exotic_aerocrystal", has(ImpenRegistry.EXOTIC_AEROCRYSTAL))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.BASIC_RIFT_CATALYST, "16"));
        // T2 Rift Catalyst Recipes
        ShapedRecipeBuilder.shaped(ImpenRegistry.ADVANCED_RIFT_CATALYST, 4)
                .pattern("A B")
                .pattern(" R ")
                .pattern("B A")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('R', ImpenRegistry.RIFT_SHARD)
                .unlockedBy("has_rift_shard", has(ImpenRegistry.RIFT_SHARD))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.ADVANCED_RIFT_CATALYST, "4"));
        ShapedRecipeBuilder.shaped(ImpenRegistry.ADVANCED_RIFT_CATALYST, 16)
                .pattern("AEB")
                .pattern(" R ")
                .pattern("BEA")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('E', ImpenRegistry.EXOTIC_AEROCRYSTAL)
                .define('R', ImpenRegistry.RIFT_SHARD)
                .unlockedBy("has_exotic_aerocrystal", has(ImpenRegistry.EXOTIC_AEROCRYSTAL))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.ADVANCED_RIFT_CATALYST, "16"));
        // T3 Rift Catalyst Recipes
        ShapedRecipeBuilder.shaped(ImpenRegistry.PRISTINE_RIFT_CATALYST, 16)
                .pattern("AEB")
                .pattern("RPR")
                .pattern("BEA")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('B', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('E', ImpenRegistry.EXOTIC_AEROCRYSTAL)
                .define('P', ImpenRegistry.STABILIZED_RIFT_PRISM)
                .define('R', ImpenRegistry.RIFT_SHARD)
                .unlockedBy("has_exotic_aerocrystal", has(ImpenRegistry.EXOTIC_AEROCRYSTAL))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.PRISTINE_RIFT_CATALYST, "16"));
        // Overworld Ore
        ShapedRecipeBuilder.shaped(ImpenRegistry.OVERWORLD_ORE_CATALYST)
                .pattern("ABC")
                .pattern(" # ")
                .pattern("D$D")
                .define('#', ImpenRegistry.BASIC_RIFT_CATALYST)
                .define('$', ImpenRegistry.AEROCRYSTAL)
                .define('A', Tags.Items.INGOTS_IRON)
                .define('B', Items.COAL)
                .define('C', Blocks.STONE)
                .define('D', Tags.Items.INGOTS_COPPER)
                .unlockedBy("has_t1_catalyst_base", has(ImpenRegistry.BASIC_RIFT_CATALYST))
                .save(consumer);
        // Deepslate Ore
        ShapedRecipeBuilder.shaped(ImpenRegistry.DEEPSLATE_ORE_CATALYST)
                .pattern("ABC")
                .pattern(" # ")
                .pattern("D$D")
                .define('#', ImpenRegistry.BASIC_RIFT_CATALYST)
                .define('$', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('A', Items.REDSTONE)
                .define('B', Items.LAPIS_LAZULI)
                .define('C', AEItems.CERTUS_QUARTZ_CRYSTAL)
                .define('D', Blocks.DEEPSLATE)
                .unlockedBy("has_t1_catalyst_base", has(ImpenRegistry.BASIC_RIFT_CATALYST))
                .save(consumer);
        // Nether Ore
        ShapedRecipeBuilder.shaped(ImpenRegistry.NETHER_ORE_CATALYST)
                .pattern("ABC")
                .pattern(" # ")
                .pattern("D$D")
                .define('#', ImpenRegistry.BASIC_RIFT_CATALYST)
                .define('$', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('A', Tags.Items.INGOTS_GOLD)
                .define('B', Items.GLOWSTONE_DUST)
                .define('C', Items.QUARTZ)
                .define('D', Blocks.NETHERRACK)
                .unlockedBy("has_t1_catalyst_base", has(ImpenRegistry.BASIC_RIFT_CATALYST))
                .save(consumer);
        // Deepslate Gem
        ShapedRecipeBuilder.shaped(ImpenRegistry.DEEPSLATE_GEM_CATALYST)
                .pattern("A C")
                .pattern(" # ")
                .pattern("D$D")
                .define('#', ImpenRegistry.ADVANCED_RIFT_CATALYST)
                .define('$', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('A', Items.DIAMOND)
                .define('C', Items.EMERALD)
                .define('D', Blocks.DEEPSLATE)
                .unlockedBy("has_t2_catalyst_base", has(ImpenRegistry.ADVANCED_RIFT_CATALYST))
                .save(consumer);
        // Nether Secret
        ShapedRecipeBuilder.shaped(ImpenRegistry.NETHER_SECRET_CATALYST, 2)
                .pattern("ABA")
                .pattern(" # ")
                .pattern("D$D")
                .define('#', ImpenRegistry.ADVANCED_RIFT_CATALYST)
                .define('$', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('A', Blocks.MAGMA_BLOCK)
                .define('B', Items.NETHERITE_SCRAP)
                .define('D', Blocks.NETHERRACK)
                .unlockedBy("has_t2_catalyst_base", has(ImpenRegistry.ADVANCED_RIFT_CATALYST))
                .save(consumer);
        // Rift Ore
        ShapedRecipeBuilder.shaped(ImpenRegistry.RIFT_SPACE_CATALYST)
                .pattern("A A")
                .pattern(" # ")
                .pattern("D$D")
                .define('#', ImpenRegistry.ADVANCED_RIFT_CATALYST)
                .define('$', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('A', ImpenRegistry.RIFT_SHARD)
                .define('D', ImpenRegistry.RIFTSTONE)
                .unlockedBy("has_t2_catalyst_base", has(ImpenRegistry.ADVANCED_RIFT_CATALYST))
                .save(consumer);
        // End Secret
        ShapedRecipeBuilder.shaped(ImpenRegistry.END_SECRET_CATALYST)
                .pattern("A C")
                .pattern(" # ")
                .pattern("D$D")
                .define('#', ImpenRegistry.PRISTINE_RIFT_CATALYST)
                .define('$', ImpenRegistry.EXOTIC_AEROCRYSTAL)
                .define('A', Items.AMETHYST_SHARD)
                .define('C', Items.PRISMARINE_SHARD)
                .define('D', Blocks.END_STONE)
                .unlockedBy("has_t3_catalyst_base", has(ImpenRegistry.PRISTINE_RIFT_CATALYST))
                .save(consumer);
        // Overworld Secret
        ShapedRecipeBuilder.shaped(ImpenRegistry.OVERWORLD_SECRET_CATALYST, 2)
                .pattern("ABC")
                .pattern(" # ")
                .pattern("D$D")
                .define('#', ImpenRegistry.PRISTINE_RIFT_CATALYST)
                .define('$', ImpenRegistry.AEROCRYSTAL)
                .define('A', Items.SLIME_BALL)
                .define('B', Items.BONE_MEAL)
                .define('C', Items.CLAY_BALL)
                .define('D', Blocks.STONE)
                .unlockedBy("has_t3_catalyst_base", has(ImpenRegistry.PRISTINE_RIFT_CATALYST))
                .save(consumer);
        // Blackstone Secret
        ShapedRecipeBuilder.shaped(ImpenRegistry.BLACKSTONE_SECRET_CATALYST, 2)
                .pattern("A C")
                .pattern(" # ")
                .pattern("D$D")
                .define('#', ImpenRegistry.PRISTINE_RIFT_CATALYST)
                .define('$', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('A', Blocks.OBSIDIAN)
                .define('C', AEBlocks.SKY_STONE_BLOCK)
                .define('D', Blocks.BLACKSTONE)
                .unlockedBy("has_t3_catalyst_base", has(ImpenRegistry.PRISTINE_RIFT_CATALYST))
                .save(consumer);

        // === Disintegrator Capsules ===

        // Empty Capsule
        ShapedRecipeBuilder.shaped(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY)
                .pattern(" A ")
                .pattern("P P")
                .pattern(" P ")
                .define('A', ImpenRegistry.AEROCRYSTAL)
                .define('P', Blocks.GLASS_PANE)
                .unlockedBy("has_aerocrystal", has(ImpenRegistry.AEROCRYSTAL))
                .save(consumer);
        // Luck Capsule
        ShapedRecipeBuilder.shaped(ImpenRegistry.DISINTEGRATOR_CAPSULE_LUCK, 8)
                .pattern(" A ")
                .pattern(" C ")
                .pattern(" # ")
                .define('A', ImpenRegistry.EXOTIC_AEROCRYSTAL)
                .define('C', ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY)
                .define('#', Items.RABBIT_FOOT)
                .unlockedBy("has_empty_disintegrator_capsule", has(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.DISINTEGRATOR_CAPSULE_LUCK, "rabbit_foot"));
        ShapedRecipeBuilder.shaped(ImpenRegistry.DISINTEGRATOR_CAPSULE_LUCK, 4)
                .pattern(" A ")
                .pattern(" C ")
                .pattern(" # ")
                .define('A', ImpenRegistry.EXOTIC_AEROCRYSTAL)
                .define('C', ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY)
                .define('#', Items.AMETHYST_SHARD)
                .unlockedBy("has_empty_disintegrator_capsule", has(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.DISINTEGRATOR_CAPSULE_LUCK, "amethyst"));
        ShapedRecipeBuilder.shaped(ImpenRegistry.DISINTEGRATOR_CAPSULE_LUCK, 16)
                .pattern(" A ")
                .pattern(" C ")
                .pattern(" # ")
                .define('A', ImpenRegistry.EXOTIC_AEROCRYSTAL)
                .define('C', ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY)
                .define('#', Items.EMERALD)
                .unlockedBy("has_empty_disintegrator_capsule", has(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.DISINTEGRATOR_CAPSULE_LUCK, "emerald"));
        // Loot Capsule
        ShapedRecipeBuilder.shaped(ImpenRegistry.DISINTEGRATOR_CAPSULE_LOOT, 8)
                .pattern(" A ")
                .pattern(" C ")
                .pattern(" # ")
                .define('A', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('C', ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY)
                .define('#', Items.LAPIS_LAZULI)
                .unlockedBy("has_empty_disintegrator_capsule", has(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.DISINTEGRATOR_CAPSULE_LOOT, "lapis"));
        ShapedRecipeBuilder.shaped(ImpenRegistry.DISINTEGRATOR_CAPSULE_LOOT, 8)
                .pattern(" A ")
                .pattern(" C ")
                .pattern("DEF")
                .define('A', ImpenRegistry.BLAZING_AEROCRYSTAL)
                .define('C', ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY)
                .define('D', Items.BONE)
                .define('E', Items.GUNPOWDER)
                .define('F', Items.ROTTEN_FLESH)
                .unlockedBy("has_empty_disintegrator_capsule", has(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.DISINTEGRATOR_CAPSULE_LOOT, "mob_drops"));
        // Egg Capsule
        ShapedRecipeBuilder.shaped(ImpenRegistry.DISINTEGRATOR_CAPSULE_EGG, 2)
                .pattern(" A ")
                .pattern(" C ")
                .pattern(" # ")
                .define('A', ImpenRegistry.RIFT_SHARD)
                .define('C', ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY)
                .define('#', Items.EGG)
                .unlockedBy("has_empty_disintegrator_capsule", has(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.DISINTEGRATOR_CAPSULE_EGG, "egg"));
        ShapedRecipeBuilder.shaped(ImpenRegistry.DISINTEGRATOR_CAPSULE_EGG, 8)
                .pattern(" A ")
                .pattern(" C ")
                .pattern("DED")
                .define('A', ImpenRegistry.RIFT_SHARD)
                .define('C', ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY)
                .define('D', Items.EGG)
                .define('E', Tags.Items.HEADS)
                .unlockedBy("has_empty_disintegrator_capsule", has(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.DISINTEGRATOR_CAPSULE_EGG, "egg_better"));
        ShapedRecipeBuilder.shaped(ImpenRegistry.DISINTEGRATOR_CAPSULE_EGG, 64)
                .pattern(" A ")
                .pattern(" C ")
                .pattern(" # ")
                .define('A', ImpenRegistry.RIFT_SHARD)
                .define('C', ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY)
                .define('#', Items.DRAGON_EGG)
                .unlockedBy("has_empty_disintegrator_capsule", has(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.DISINTEGRATOR_CAPSULE_EGG, "dragon_egg"));
        // Player Kill Capsule
        ShapedRecipeBuilder.shaped(ImpenRegistry.DISINTEGRATOR_CAPSULE_PLAYER_KILL, 4)
                .pattern(" A ")
                .pattern(" C ")
                .pattern(" # ")
                .define('A', ImpenRegistry.RIFT_SHARD)
                .define('C', ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY)
                .define('#', Items.ARMOR_STAND)
                .unlockedBy("has_empty_disintegrator_capsule", has(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.DISINTEGRATOR_CAPSULE_PLAYER_KILL, "armor_stand"));
        ShapedRecipeBuilder.shaped(ImpenRegistry.DISINTEGRATOR_CAPSULE_PLAYER_KILL, 16)
                .pattern(" A ")
                .pattern(" C ")
                .pattern("DEF")
                .define('A', ImpenRegistry.RIFT_SHARD)
                .define('C', ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY)
                .define('D', Blocks.OBSERVER)
                .define('E', Items.STONE_SWORD)
                .define('F', Items.LEVER)
                .unlockedBy("has_empty_disintegrator_capsule", has(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY))
                .save(consumer, ImpenIdUtil.makeId(ImpenRegistry.DISINTEGRATOR_CAPSULE_PLAYER_KILL, "misc"));

        // === Seeds ===

        ShapelessRecipeBuilder.shapeless(ImpenRegistry.PLANTABLE_CERTUS.item())
                .requires(AEItems.CERTUS_CRYSTAL_SEED)
                .unlockedBy("has_certus_seed", has(AEItems.CERTUS_CRYSTAL_SEED))
                .save(consumer, ImpenIdUtil.makeId("plantable_certus_seeds"));
        ShapelessRecipeBuilder.shapeless(ImpenRegistry.PLANTABLE_FLUIX.item())
                .requires(AEItems.FLUIX_CRYSTAL_SEED)
                .unlockedBy("has_fluix_seed", has(AEItems.FLUIX_CRYSTAL_SEED))
                .save(consumer, ImpenIdUtil.makeId("plantable_fluix_seeds"));

        // === Storage Blocks ===

        nineBlockStorageRecipes(consumer, ImpenRegistry.AEROCRYSTAL, ImpenRegistry.AEROCRYSTAL_BLOCK);
        nineBlockStorageRecipes(consumer, ImpenRegistry.BLAZING_AEROCRYSTAL, ImpenRegistry.BLAZING_AEROCRYSTAL_BLOCK);
        nineBlockStorageRecipes(consumer, ImpenRegistry.EXOTIC_AEROCRYSTAL, ImpenRegistry.EXOTIC_AEROCRYSTAL_BLOCK);
        nineBlockStorageRecipes(consumer, ImpenRegistry.RIFT_SHARD, ImpenRegistry.RIFT_SHARD_BLOCK);

        // ***
        // Other Recipe Types
        // ***

        // Smelting recipes
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ImpenRegistry.RIFTSTONE), ImpenRegistry.SMOOTH_RIFTSTONE, 0.1F, 200)
                .unlockedBy("has_riftstone", has(ImpenRegistry.RIFTSTONE))
                .save(consumer);
        oreSmeltingOverride(consumer, Collections.singletonList(ImpenRegistry.RIFT_SHARD_ORE), ImpenRegistry.RIFT_SHARD,
                0.7F, 200, null);
        oreBlastingOverride(consumer, Collections.singletonList(ImpenRegistry.RIFT_SHARD_ORE), ImpenRegistry.RIFT_SHARD,
                0.7F, 100, null);

        // Stonecutter recipes
        stonecutterResultFromBaseOverride(consumer, ImpenRegistry.RIFTSTONE_BRICKS, ImpenRegistry.SMOOTH_RIFTSTONE);
        
        // Rift Catalyst recipes
        new RiftCatalystRecipeProvider().addRecipes(consumer);
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
                    .save(p_176534_, ImpenIdUtil.makeId(getItemName(p_176537_) + p_176541_ + "_" + getItemName(itemlike)));
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
}
