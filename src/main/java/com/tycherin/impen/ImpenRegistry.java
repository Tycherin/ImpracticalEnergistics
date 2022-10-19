package com.tycherin.impen;

import com.tycherin.impen.block.BeamedNetworkLinkBlock;
import com.tycherin.impen.block.ImaginarySpaceManipulatorBlock;
import com.tycherin.impen.block.ImaginarySpaceStabilizerBlock;
import com.tycherin.impen.block.PlantableCertusBlock;
import com.tycherin.impen.block.PlantableFluixBlock;
import com.tycherin.impen.block.PossibilityDisintegratorBlock;
import com.tycherin.impen.block.SpatialCrystallizerBlock;
import com.tycherin.impen.block.ToasterDriveBlock;
import com.tycherin.impen.blockentity.BeamedNetworkLinkBlockEntity;
import com.tycherin.impen.blockentity.ImaginarySpaceManipulatorBlockEntity;
import com.tycherin.impen.blockentity.ImaginarySpaceStabilizerBlockEntity;
import com.tycherin.impen.blockentity.PossibilityDisintegratorBlockEntity;
import com.tycherin.impen.blockentity.SpatialCrystallizerBlockEntity;
import com.tycherin.impen.blockentity.ToasterDriveBlockEntity;
import com.tycherin.impen.entity.FluixCatalystEntity;
import com.tycherin.impen.entity.SpatialToolCatalystEntity;
import com.tycherin.impen.item.LunchboxCellItem;
import com.tycherin.impen.item.SpatialAxeItem;
import com.tycherin.impen.item.SpatialHoeItem;
import com.tycherin.impen.item.SpatialPickaxeItem;
import com.tycherin.impen.item.SpatialSpadeItem;
import com.tycherin.impen.item.SpatialSwordItem;
import com.tycherin.impen.part.CapturePlanePart;
import com.tycherin.impen.recipe.IsmCatalystRecipe;
import com.tycherin.impen.recipe.IsmCatalystRecipeSerializer;
import com.tycherin.impen.recipe.SpatialCrystallizerRecipe;
import com.tycherin.impen.recipe.SpatialCrystallizerRecipeSerializer;

import appeng.api.upgrades.Upgrades;
import appeng.block.AEBaseBlockItem;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.items.materials.CustomEntityItem;
import appeng.items.parts.PartItem;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ImpenRegistry {

    // Define all the registries we care about
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            ImpracticalEnergisticsMod.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            ImpracticalEnergisticsMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITIES, ImpracticalEnergisticsMod.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES,
            ImpracticalEnergisticsMod.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister
            .create(Registry.RECIPE_TYPE_REGISTRY, ImpracticalEnergisticsMod.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, ImpracticalEnergisticsMod.MOD_ID);

    // Beamed Network Link
    public static final RegistryObject<BeamedNetworkLinkBlock> BEAMED_NETWORK_LINK_BLOCK = BLOCKS
            .register("beamed_network_link", () -> {
                return new BeamedNetworkLinkBlock(BlockBehaviour.Properties.of(Material.METAL));
            });
    public static final RegistryObject<BlockEntityType<BeamedNetworkLinkBlockEntity>> BEAMED_NETWORK_LINK_BE = BLOCK_ENTITIES
            .register("beamed_network_link_be", () -> {
                return BlockEntityType.Builder.of(BeamedNetworkLinkBlockEntity::new, BEAMED_NETWORK_LINK_BLOCK.get())
                        .build(null);
            });
    public static final RegistryObject<Item> BEAMED_NETWORK_LINK_ITEM = ITEMS.register(
            BEAMED_NETWORK_LINK_BLOCK.getId().getPath(),
            // Use AEBaseBlockItem instead of BlockItem in order to inherit the orient-on-place behavior
            () -> new AEBaseBlockItem(BEAMED_NETWORK_LINK_BLOCK.get(),
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<LunchboxCellItem> LUNCHBOX_CELL_ITEM = ITEMS.register("lunchbox_cell",
            () -> new LunchboxCellItem());

    // Imaginary Space Manipulator
    public static final RegistryObject<ImaginarySpaceManipulatorBlock> IMAGINARY_SPACE_MANIPULATOR_BLOCK = BLOCKS
            .register("imaginary_space_manipulator", () -> {
                return new ImaginarySpaceManipulatorBlock(BlockBehaviour.Properties.of(Material.METAL));
            });
    public static final RegistryObject<BlockEntityType<ImaginarySpaceManipulatorBlockEntity>> IMAGINARY_SPACE_MANIPULATOR_BE = BLOCK_ENTITIES
            .register("imaginary_space_manipulator_be", () -> {
                return BlockEntityType.Builder
                        .of(ImaginarySpaceManipulatorBlockEntity::new, IMAGINARY_SPACE_MANIPULATOR_BLOCK.get())
                        .build(null);
            });
    public static final RegistryObject<Item> IMAGINARY_SPACE_MANIPULATOR_ITEM = ITEMS.register(
            IMAGINARY_SPACE_MANIPULATOR_BLOCK.getId().getPath(),
            () -> new BlockItem(IMAGINARY_SPACE_MANIPULATOR_BLOCK.get(),
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // Imaginary Space Stabilizer
    public static final RegistryObject<ImaginarySpaceStabilizerBlock> IMAGINARY_SPACE_STABILIZER_BLOCK = BLOCKS
            .register("imaginary_space_stabilizer", () -> {
                return new ImaginarySpaceStabilizerBlock(BlockBehaviour.Properties.of(Material.METAL));
            });
    public static final RegistryObject<BlockEntityType<ImaginarySpaceStabilizerBlockEntity>> IMAGINARY_SPACE_STABILIZER_BE = BLOCK_ENTITIES
            .register("imaginary_space_stabilizer_be", () -> {
                return BlockEntityType.Builder
                        .of(ImaginarySpaceStabilizerBlockEntity::new, IMAGINARY_SPACE_STABILIZER_BLOCK.get())
                        .build(null);
            });
    public static final RegistryObject<Item> IMAGINARY_SPACE_STABILIZER_ITEM = ITEMS.register(
            IMAGINARY_SPACE_STABILIZER_BLOCK.getId().getPath(),
            () -> new BlockItem(IMAGINARY_SPACE_STABILIZER_BLOCK.get(),
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // Spatial Crystallizer
    public static final RegistryObject<SpatialCrystallizerBlock> SPATIAL_CRYSTALLIZER_BLOCK = BLOCKS
            .register("spatial_crystallizer", () -> {
                return new SpatialCrystallizerBlock(BlockBehaviour.Properties.of(Material.METAL));
            });
    public static final RegistryObject<BlockEntityType<SpatialCrystallizerBlockEntity>> SPATIAL_CRYSTALLIZER_BE = BLOCK_ENTITIES
            .register("spatial_crystallizer_be", () -> {
                return BlockEntityType.Builder.of(SpatialCrystallizerBlockEntity::new, SPATIAL_CRYSTALLIZER_BLOCK.get())
                        .build(null);
            });
    public static final RegistryObject<Item> SPATIAL_CRYSTALLIZER_ITEM = ITEMS.register(
            SPATIAL_CRYSTALLIZER_BLOCK.getId().getPath(),
            () -> new AEBaseBlockItem(SPATIAL_CRYSTALLIZER_BLOCK.get(),
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // Possibility Disintegrator
    public static final RegistryObject<PossibilityDisintegratorBlock> POSSIBILITY_DISINTEGRATOR_BLOCK = BLOCKS
            .register("possibility_disintegrator", () -> {
                return new PossibilityDisintegratorBlock(BlockBehaviour.Properties.of(Material.METAL));
            });
    public static final RegistryObject<BlockEntityType<PossibilityDisintegratorBlockEntity>> POSSIBILITY_DISINTEGRATOR_BE = BLOCK_ENTITIES
            .register("possibility_disintegrator_be", () -> {
                return BlockEntityType.Builder
                        .of(PossibilityDisintegratorBlockEntity::new, POSSIBILITY_DISINTEGRATOR_BLOCK.get())
                        .build(null);
            });
    public static final RegistryObject<Item> POSSIBILITY_DISINTEGRATOR_ITEM = ITEMS.register(
            POSSIBILITY_DISINTEGRATOR_BLOCK.getId().getPath(),
            () -> new BlockItem(POSSIBILITY_DISINTEGRATOR_BLOCK.get(),
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // Toaster Drive
    public static final RegistryObject<ToasterDriveBlock> TOASTER_DRIVE_BLOCK = BLOCKS
            .register("toaster_drive", () -> new ToasterDriveBlock());
    public static final RegistryObject<BlockEntityType<ToasterDriveBlockEntity>> TOASTER_DRIVE_BE = BLOCK_ENTITIES
            .register("toaster_drive_be", () -> BlockEntityType.Builder.of(ToasterDriveBlockEntity::new, TOASTER_DRIVE_BLOCK.get())
                        .build(null));
    public static final RegistryObject<Item> TOASTER_DRIVE_ITEM = ITEMS.register(
            TOASTER_DRIVE_BLOCK.getId().getPath(),
            () -> new AEBaseBlockItem(TOASTER_DRIVE_BLOCK.get(),
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    
    // Capture Plane
    public static final RegistryObject<Item> CAPTURE_PLANE_ITEM = ITEMS.register(
            "capture_plane", () -> new PartItem<>(new Item.Properties().tab(CreativeModeTab.TAB_MISC),
                    CapturePlanePart.class, (part) -> new CapturePlanePart(part)));

    // Plantable seeds
    public static final RegistryObject<PlantableCertusBlock> PLANTABLE_CERTUS_BLOCK = BLOCKS.register(
            "plantable_certus", () -> new PlantableCertusBlock(BlockBehaviour.Properties.of(Material.PLANT)
                    .noCollission().randomTicks().instabreak().sound(SoundType.CROP)));
    public static final RegistryObject<Item> PLANTABLE_CERTUS_SEEDS_ITEM = ITEMS.register(
            "plantable_certus_seeds",
            () -> new ItemNameBlockItem(PLANTABLE_CERTUS_BLOCK.get(),
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<PlantableFluixBlock> PLANTABLE_FLUIX_BLOCK = BLOCKS.register(
            "plantable_fluix", () -> new PlantableFluixBlock(BlockBehaviour.Properties.of(Material.PLANT)
                    .noCollission().randomTicks().instabreak().sound(SoundType.CROP)));
    public static final RegistryObject<Item> PLANTABLE_FLUIX_SEEDS_ITEM = ITEMS.register(
            "plantable_fluix_seeds",
            () -> new ItemNameBlockItem(PLANTABLE_FLUIX_BLOCK.get(),
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    
    // Fluix Catalyst
    public static final RegistryObject<Item> FLUIX_CATALYST_ITEM = ITEMS.register("fluix_catalyst",
            () -> new CustomEntityItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC), FluixCatalystEntity::new));
    public static final RegistryObject<EntityType<FluixCatalystEntity>> FLUIX_CATALYST_ENTITY = ENTITIES
            .register(FLUIX_CATALYST_ITEM.getId().getPath(), () -> EntityType.Builder
                    .<FluixCatalystEntity>of(FluixCatalystEntity::new, MobCategory.MISC).sized(0.25F, 0.25F)
                    .clientTrackingRange(6).updateInterval(20).build(ImpracticalEnergisticsMod.MOD_ID));

    // Tools
    public static final RegistryObject<Item> SPATIAL_AXE_ITEM = ITEMS.register("spatial_axe",
            () -> new SpatialAxeItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> SPATIAL_HOE_ITEM = ITEMS.register("spatial_hoe",
            () -> new SpatialHoeItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> SPATIAL_PICKAXE_ITEM = ITEMS.register("spatial_pickaxe",
            () -> new SpatialPickaxeItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> SPATIAL_SPADE_ITEM = ITEMS.register("spatial_spade",
            () -> new SpatialSpadeItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> SPATIAL_SWORD_ITEM = ITEMS.register("spatial_sword",
            () -> new SpatialSwordItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> SPATIAL_TOOL_CATALYST_ITEM = ITEMS.register("spatial_tool_catalyst",
            () -> new CustomEntityItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC), SpatialToolCatalystEntity::new));
    public static final RegistryObject<EntityType<SpatialToolCatalystEntity>> SPATIAL_TOOL_CATALYST_ENTITY = ENTITIES
            .register(SPATIAL_TOOL_CATALYST_ITEM.getId().getPath(), () -> EntityType.Builder
                    .<SpatialToolCatalystEntity>of(SpatialToolCatalystEntity::new, MobCategory.MISC).sized(0.25F, 0.25F)
                    .clientTrackingRange(6).updateInterval(20).build(ImpracticalEnergisticsMod.MOD_ID));

    // Custom recipe types
    public static final RegistryObject<RecipeType<IsmCatalystRecipe>> ISM_CATALYST_RECIPE_TYPE =
            ImpenRegistry.<IsmCatalystRecipe>makeRecipeType("ism_catalyst");
    public static final RegistryObject<RecipeSerializer<?>> ISM_CATALYST_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
            .register("ism_catalyst", () -> IsmCatalystRecipeSerializer.INSTANCE);
    public static final RegistryObject<RecipeType<SpatialCrystallizerRecipe>> SPATIAL_CRYSTALLIZER_RECIPE_TYPE =
            ImpenRegistry.<SpatialCrystallizerRecipe>makeRecipeType("spatial_crystallizer");
    public static final RegistryObject<RecipeSerializer<?>> SPATIAL_CRYSTALLIZER_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
            .register("spatial_crystallizer", () -> SpatialCrystallizerRecipeSerializer.INSTANCE);
    
    public static void register(final IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ENTITIES.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }
    
    public static void commonSetup(final FMLCommonSetupEvent event) {
        BEAMED_NETWORK_LINK_BLOCK.get().setBlockEntity(BeamedNetworkLinkBlockEntity.class, BEAMED_NETWORK_LINK_BE.get(),
                null, (level, pos, state, be) -> ((ServerTickingBlockEntity) be).serverTick());
        IMAGINARY_SPACE_MANIPULATOR_BLOCK.get().setBlockEntity(ImaginarySpaceManipulatorBlockEntity.class,
                IMAGINARY_SPACE_MANIPULATOR_BE.get(), null, null);
        IMAGINARY_SPACE_STABILIZER_BLOCK.get().setBlockEntity(ImaginarySpaceStabilizerBlockEntity.class,
                IMAGINARY_SPACE_STABILIZER_BE.get(), null, null);
        SPATIAL_CRYSTALLIZER_BLOCK.get().setBlockEntity(SpatialCrystallizerBlockEntity.class,
                SPATIAL_CRYSTALLIZER_BE.get(), null, null);
        POSSIBILITY_DISINTEGRATOR_BLOCK.get().setBlockEntity(PossibilityDisintegratorBlockEntity.class,
                POSSIBILITY_DISINTEGRATOR_BE.get(), null, null);
        TOASTER_DRIVE_BLOCK.get().setBlockEntity(ToasterDriveBlockEntity.class, TOASTER_DRIVE_BE.get(), null,
                (level, pos, state, be) -> ((ServerTickingBlockEntity) be).serverTick());

        // AE2 upgrades need to be registered after normal registry events
        Upgrades.add(AEItems.SPEED_CARD, SPATIAL_CRYSTALLIZER_ITEM.get(), 3);
        Upgrades.add(AEItems.SPEED_CARD, POSSIBILITY_DISINTEGRATOR_ITEM.get(), 2);
        Upgrades.add(AEItems.CAPACITY_CARD, POSSIBILITY_DISINTEGRATOR_ITEM.get(), 2);
        Upgrades.add(AEItems.SPEED_CARD, IMAGINARY_SPACE_MANIPULATOR_ITEM.get(), 3);
    }
    
    private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> makeRecipeType(final String key) {
        return RECIPE_TYPES.register(key, () -> new RecipeType<T>() {
            @Override
            public String toString() {
                return key;
            }
        });
    }
}
