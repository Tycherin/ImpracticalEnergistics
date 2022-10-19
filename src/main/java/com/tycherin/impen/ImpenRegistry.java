package com.tycherin.impen;

import com.tycherin.impen.block.BeamedNetworkLinkBlock;
import com.tycherin.impen.block.SpatialRiftManipulatorBlock;
import com.tycherin.impen.block.SpatialRiftStabilizerBlock;
import com.tycherin.impen.block.PlantableCertusBlock;
import com.tycherin.impen.block.PlantableFluixBlock;
import com.tycherin.impen.block.PossibilityDisintegratorBlock;
import com.tycherin.impen.block.AtmosphericCrystallizerBlock;
import com.tycherin.impen.block.ToasterDriveBlock;
import com.tycherin.impen.blockentity.BeamedNetworkLinkBlockEntity;
import com.tycherin.impen.blockentity.SpatialRiftManipulatorBlockEntity;
import com.tycherin.impen.blockentity.SpatialRiftStabilizerBlockEntity;
import com.tycherin.impen.blockentity.PossibilityDisintegratorBlockEntity;
import com.tycherin.impen.blockentity.AtmosphericCrystallizerBlockEntity;
import com.tycherin.impen.blockentity.ToasterDriveBlockEntity;
import com.tycherin.impen.entity.RiftPrismEntity;
import com.tycherin.impen.entity.StabilizedRiftPrismEntity;
import com.tycherin.impen.item.LunchboxCellItem;
import com.tycherin.impen.item.RiftAxeItem;
import com.tycherin.impen.item.RiftHoeItem;
import com.tycherin.impen.item.RiftPickaxeItem;
import com.tycherin.impen.item.RiftSpadeItem;
import com.tycherin.impen.item.RiftSwordItem;
import com.tycherin.impen.part.CapturePlanePart;
import com.tycherin.impen.recipe.RiftCatalystRecipe;
import com.tycherin.impen.recipe.RiftCatalystRecipeSerializer;
import com.tycherin.impen.recipe.AtmosphericCrystallizerRecipe;
import com.tycherin.impen.recipe.AtmosphericCrystallizerRecipeSerializer;

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
            .register(BEAMED_NETWORK_LINK_BLOCK.getId().getPath(), () -> {
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

    // Spatial Rift Manipulator
    public static final RegistryObject<SpatialRiftManipulatorBlock> SPATIAL_RIFT_MANIPULATOR_BLOCK = BLOCKS
            .register("spatial_rift_manipulator", () -> {
                return new SpatialRiftManipulatorBlock(BlockBehaviour.Properties.of(Material.METAL));
            });
    public static final RegistryObject<BlockEntityType<SpatialRiftManipulatorBlockEntity>> SPATIAL_RIFT_MANIPULATOR_BE = BLOCK_ENTITIES
            .register(SPATIAL_RIFT_MANIPULATOR_BLOCK.getId().getPath(), () -> {
                return BlockEntityType.Builder
                        .of(SpatialRiftManipulatorBlockEntity::new, SPATIAL_RIFT_MANIPULATOR_BLOCK.get())
                        .build(null);
            });
    public static final RegistryObject<Item> SPATIAL_RIFT_MANIPULATOR_ITEM = ITEMS.register(
            SPATIAL_RIFT_MANIPULATOR_BLOCK.getId().getPath(),
            () -> new BlockItem(SPATIAL_RIFT_MANIPULATOR_BLOCK.get(),
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // Spatial Rift Stabilizer
    public static final RegistryObject<SpatialRiftStabilizerBlock> SPATIAL_RIFT_STABILIZER_BLOCK = BLOCKS
            .register("spatial_rift_stabilizer", () -> {
                return new SpatialRiftStabilizerBlock(BlockBehaviour.Properties.of(Material.METAL));
            });
    public static final RegistryObject<BlockEntityType<SpatialRiftStabilizerBlockEntity>> SPATIAL_RIFT_STABILIZER_BE = BLOCK_ENTITIES
            .register(SPATIAL_RIFT_STABILIZER_BLOCK.getId().getPath(), () -> {
                return BlockEntityType.Builder
                        .of(SpatialRiftStabilizerBlockEntity::new, SPATIAL_RIFT_STABILIZER_BLOCK.get())
                        .build(null);
            });
    public static final RegistryObject<Item> SPATIAL_RIFT_STABILIZER_ITEM = ITEMS.register(
            SPATIAL_RIFT_STABILIZER_BLOCK.getId().getPath(),
            () -> new BlockItem(SPATIAL_RIFT_STABILIZER_BLOCK.get(),
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // Atmospheric Crystallizer
    public static final RegistryObject<AtmosphericCrystallizerBlock> ATMOSPHERIC_CRYSTALLIZER_BLOCK = BLOCKS
            .register("atmospheric_crystallizer", () -> {
                return new AtmosphericCrystallizerBlock(BlockBehaviour.Properties.of(Material.METAL));
            });
    public static final RegistryObject<BlockEntityType<AtmosphericCrystallizerBlockEntity>> ATMOSPHERIC_CRYSTALLIZER_BE = BLOCK_ENTITIES
            .register(ATMOSPHERIC_CRYSTALLIZER_BLOCK.getId().getPath(), () -> {
                return BlockEntityType.Builder.of(AtmosphericCrystallizerBlockEntity::new, ATMOSPHERIC_CRYSTALLIZER_BLOCK.get())
                        .build(null);
            });
    public static final RegistryObject<Item> SPATIAL_CRYSTALLIZER_ITEM = ITEMS.register(
            ATMOSPHERIC_CRYSTALLIZER_BLOCK.getId().getPath(),
            () -> new AEBaseBlockItem(ATMOSPHERIC_CRYSTALLIZER_BLOCK.get(),
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // Possibility Disintegrator
    public static final RegistryObject<PossibilityDisintegratorBlock> POSSIBILITY_DISINTEGRATOR_BLOCK = BLOCKS
            .register("possibility_disintegrator", () -> {
                return new PossibilityDisintegratorBlock(BlockBehaviour.Properties.of(Material.METAL));
            });
    public static final RegistryObject<BlockEntityType<PossibilityDisintegratorBlockEntity>> POSSIBILITY_DISINTEGRATOR_BE = BLOCK_ENTITIES
            .register(POSSIBILITY_DISINTEGRATOR_BLOCK.getId().getPath(), () -> {
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
            .register(TOASTER_DRIVE_BLOCK.getId().getPath(),
                    () -> BlockEntityType.Builder.of(ToasterDriveBlockEntity::new, TOASTER_DRIVE_BLOCK.get()).build(null));
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
    
    // Rift Prism
    public static final RegistryObject<Item> RIFT_PRISM_ITEM = ITEMS.register("rift_prism",
            () -> new CustomEntityItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC), RiftPrismEntity::new));
    public static final RegistryObject<EntityType<RiftPrismEntity>> RIFT_PRISM_ENTITY = ENTITIES
            .register(RIFT_PRISM_ITEM.getId().getPath(), () -> EntityType.Builder
                    .<RiftPrismEntity>of(RiftPrismEntity::new, MobCategory.MISC).sized(0.25F, 0.25F)
                    .clientTrackingRange(6).updateInterval(20).build(ImpracticalEnergisticsMod.MOD_ID));

    // Tools
    public static final RegistryObject<Item> RIFT_AXE_ITEM = ITEMS.register("rift_axe",
            () -> new RiftAxeItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> RIFT_HOE_ITEM = ITEMS.register("rift_hoe",
            () -> new RiftHoeItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> RIFT_PICKAXE_ITEM = ITEMS.register("rift_pickaxe",
            () -> new RiftPickaxeItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> RIFT_SPADE_ITEM = ITEMS.register("rift_spade",
            () -> new RiftSpadeItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> RIFT_SWORD_ITEM = ITEMS.register("rift_sword",
            () -> new RiftSwordItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> STABILIZED_RIFT_PRISM_ITEM = ITEMS.register("stabilized_rift_prism",
            () -> new CustomEntityItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC), StabilizedRiftPrismEntity::new));
    public static final RegistryObject<EntityType<StabilizedRiftPrismEntity>> STABILIZED_RIFT_PRISM_ENTITY = ENTITIES
            .register(STABILIZED_RIFT_PRISM_ITEM.getId().getPath(), () -> EntityType.Builder
                    .<StabilizedRiftPrismEntity>of(StabilizedRiftPrismEntity::new, MobCategory.MISC).sized(0.25F, 0.25F)
                    .clientTrackingRange(6).updateInterval(20).build(ImpracticalEnergisticsMod.MOD_ID));

    // Custom recipe types
    public static final RegistryObject<RecipeType<RiftCatalystRecipe>> RIFT_CATALYST_RECIPE_TYPE =
            ImpenRegistry.<RiftCatalystRecipe>makeRecipeType("rift_catalyst");
    public static final RegistryObject<RecipeSerializer<?>> RIFT_CATALYST_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
            .register(RIFT_CATALYST_RECIPE_TYPE.getId().getPath(), () -> RiftCatalystRecipeSerializer.INSTANCE);
    public static final RegistryObject<RecipeType<AtmosphericCrystallizerRecipe>> ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE =
            ImpenRegistry.<AtmosphericCrystallizerRecipe>makeRecipeType("atmospheric_crystallizer");
    public static final RegistryObject<RecipeSerializer<?>> ATMOSPHERIC_CRYSTALLIZER_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
            .register(ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE.getId().getPath(),
                    () -> AtmosphericCrystallizerRecipeSerializer.INSTANCE);
    
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
        SPATIAL_RIFT_MANIPULATOR_BLOCK.get().setBlockEntity(SpatialRiftManipulatorBlockEntity.class,
                SPATIAL_RIFT_MANIPULATOR_BE.get(), null, null);
        SPATIAL_RIFT_STABILIZER_BLOCK.get().setBlockEntity(SpatialRiftStabilizerBlockEntity.class,
                SPATIAL_RIFT_STABILIZER_BE.get(), null, null);
        ATMOSPHERIC_CRYSTALLIZER_BLOCK.get().setBlockEntity(AtmosphericCrystallizerBlockEntity.class,
                ATMOSPHERIC_CRYSTALLIZER_BE.get(), null, null);
        POSSIBILITY_DISINTEGRATOR_BLOCK.get().setBlockEntity(PossibilityDisintegratorBlockEntity.class,
                POSSIBILITY_DISINTEGRATOR_BE.get(), null, null);
        TOASTER_DRIVE_BLOCK.get().setBlockEntity(ToasterDriveBlockEntity.class, TOASTER_DRIVE_BE.get(), null,
                (level, pos, state, be) -> ((ServerTickingBlockEntity) be).serverTick());

        // AE2 upgrades need to be registered after normal registry events
        Upgrades.add(AEItems.SPEED_CARD, SPATIAL_CRYSTALLIZER_ITEM.get(), 3);
        Upgrades.add(AEItems.SPEED_CARD, POSSIBILITY_DISINTEGRATOR_ITEM.get(), 2);
        Upgrades.add(AEItems.CAPACITY_CARD, POSSIBILITY_DISINTEGRATOR_ITEM.get(), 2);
        Upgrades.add(AEItems.SPEED_CARD, SPATIAL_RIFT_MANIPULATOR_ITEM.get(), 3);
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
