package com.tycherin.impen;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
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
import com.tycherin.impen.client.gui.ImaginarySpaceManipulatorMenu;
import com.tycherin.impen.client.gui.ImaginarySpaceStabilizerMenu;
import com.tycherin.impen.client.gui.SpatialCrystallizerMenu;
import com.tycherin.impen.entity.FluixCatalystEntity;
import com.tycherin.impen.item.LunchboxCellItem;
import com.tycherin.impen.logic.ism.IsmService;
import com.tycherin.impen.part.CapturePlanePart;
import com.tycherin.impen.recipe.IsmCatalystRecipe;
import com.tycherin.impen.recipe.IsmCatalystRecipeSerializer;
import com.tycherin.impen.recipe.SpatialCrystallizerRecipe;
import com.tycherin.impen.recipe.SpatialCrystallizerRecipeSerializer;

import appeng.api.parts.PartModels;
import appeng.block.AEBaseBlockItem;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.storage.ChestBlockEntity;
import appeng.items.materials.CustomEntityItem;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ImpracticalEnergisticsMod.MOD_ID)
public class ImpracticalEnergisticsMod {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "impracticalenergistics";

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
                    .clientTrackingRange(6).updateInterval(20).build(MOD_ID));

    public static final RegistryObject<RecipeType<IsmCatalystRecipe>> ISM_CATALYST_RECIPE_TYPE = RECIPE_TYPES
            .register("ism_catalyst", () -> IsmCatalystRecipe.TYPE);
    public static final RegistryObject<RecipeSerializer<?>> ISM_CATALYST_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
            .register("ism_catalyst", () -> IsmCatalystRecipeSerializer.INSTANCE);
    public static final RegistryObject<RecipeType<SpatialCrystallizerRecipe>> SPATIAL_CRYSTALLIZER_RECIPE_TYPE = RECIPE_TYPES
            .register("spatial_crystallizer", () -> SpatialCrystallizerRecipe.TYPE);
    public static final RegistryObject<RecipeSerializer<?>> SPATIAL_CRYSTALLIZER_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
            .register("spatial_crystallizer", () -> SpatialCrystallizerRecipeSerializer.INSTANCE);

    public ImpracticalEnergisticsMod() {
        MinecraftForge.EVENT_BUS.register(this);

        final var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ENTITIES.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);

        IsmService.init();

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ImpracticalEnergisticsClientSetup::init);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addGenericListener(MenuType.class, this::registerMenus);

        MinecraftForge.EVENT_BUS.addListener(CapturePlanePart::handleProjectileEvent);

        PartModels.registerModels(PartModelsHelper.createModels(CapturePlanePart.class));
    }

    public void registerMenus(RegistryEvent.Register<MenuType<?>> event) {
        event.getRegistry().registerAll(
                ImaginarySpaceManipulatorMenu.TYPE,
                ImaginarySpaceStabilizerMenu.TYPE,
                SpatialCrystallizerMenu.TYPE);
    }

    private static RegistryObject<Item> createBlockItem(final RegistryObject<? extends Block> block) {
        return ITEMS.register(block.getId().getPath(),
                () -> new BlockItem(block.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    }

    public void commonSetup(final FMLCommonSetupEvent event) {
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
    }
}
