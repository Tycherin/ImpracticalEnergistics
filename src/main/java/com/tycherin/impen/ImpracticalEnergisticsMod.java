package com.tycherin.impen;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.block.BeamedNetworkLinkBlock;
import com.tycherin.impen.block.ImaginarySpaceManipulatorBlock;
import com.tycherin.impen.block.ImaginarySpaceStabilizerBlock;
import com.tycherin.impen.block.SpatialCrystallizerBlock;
import com.tycherin.impen.blockentity.BeamedNetworkLinkBlockEntity;
import com.tycherin.impen.blockentity.ImaginarySpaceManipulatorBlockEntity;
import com.tycherin.impen.blockentity.ImaginarySpaceStabilizerBlockEntity;
import com.tycherin.impen.blockentity.SpatialCrystallizerBlockEntity;
import com.tycherin.impen.client.gui.ImaginarySpaceManipulatorMenu;
import com.tycherin.impen.client.gui.ImaginarySpaceStabilizerMenu;
import com.tycherin.impen.item.LunchboxCellItem;
import com.tycherin.impen.logic.ism.IsmService;
import com.tycherin.impen.recipe.IsmCatalystRecipe;
import com.tycherin.impen.recipe.IsmCatalystRecipeSerializer;
import com.tycherin.impen.recipe.SpatialCrystallizerRecipe;
import com.tycherin.impen.recipe.SpatialCrystallizerRecipeSerializer;

import appeng.block.AEBaseBlockItem;
import appeng.blockentity.ServerTickingBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
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
                return BlockEntityType.Builder.of(ImaginarySpaceManipulatorBlockEntity::new, IMAGINARY_SPACE_MANIPULATOR_BLOCK.get())
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
                return BlockEntityType.Builder.of(ImaginarySpaceStabilizerBlockEntity::new, IMAGINARY_SPACE_STABILIZER_BLOCK.get())
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
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        
        IsmService.init();

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ImpracticalEnergisticsClientSetup::init);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addGenericListener(MenuType.class, this::registerMenus);
    }
    
    public void registerMenus(RegistryEvent.Register<MenuType<?>> event) {
        event.getRegistry().registerAll(
                ImaginarySpaceManipulatorMenu.TYPE,
                ImaginarySpaceStabilizerMenu.TYPE);
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
    }
}
