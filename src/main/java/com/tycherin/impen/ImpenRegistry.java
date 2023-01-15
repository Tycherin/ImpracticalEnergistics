package com.tycherin.impen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.tycherin.impen.block.AtmosphericCrystallizerBlock;
import com.tycherin.impen.block.BeamedNetworkLinkBlock;
import com.tycherin.impen.block.EjectionDriveBlock;
import com.tycherin.impen.block.PossibilityDisintegratorBlock;
import com.tycherin.impen.block.rift.SpatialRiftCollapserBlock;
import com.tycherin.impen.block.rift.SpatialRiftManipulatorBlock;
import com.tycherin.impen.block.rift.SpatialRiftSpawnerBlock;
import com.tycherin.impen.blockentity.AtmosphericCrystallizerBlockEntity;
import com.tycherin.impen.blockentity.BeamedNetworkLinkBlockEntity;
import com.tycherin.impen.blockentity.EjectionDriveBlockEntity;
import com.tycherin.impen.blockentity.PossibilityDisintegratorBlockEntity;
import com.tycherin.impen.blockentity.rift.SpatialRiftCollapserBlockEntity;
import com.tycherin.impen.blockentity.rift.SpatialRiftManipulatorBlockEntity;
import com.tycherin.impen.blockentity.rift.SpatialRiftSpawnerBlockEntity;
import com.tycherin.impen.entity.RiftPrismEntity;
import com.tycherin.impen.entity.StabilizedRiftPrismEntity;
import com.tycherin.impen.item.LunchboxCellItem;
import com.tycherin.impen.item.RiftedSpatialCellItem;
import com.tycherin.impen.part.CapturePlanePart;
import com.tycherin.impen.recipe.AtmosphericCrystallizerRecipe;
import com.tycherin.impen.recipe.AtmosphericCrystallizerRecipeSerializer;
import com.tycherin.impen.recipe.SpatialRiftCollapserRecipe;
import com.tycherin.impen.recipe.SpatialRiftCollapserRecipeSerializer;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipeSerializer;
import com.tycherin.impen.recipe.SpatialRiftSpawnerRecipe;
import com.tycherin.impen.recipe.SpatialRiftSpawnerRecipeSerializer;

import appeng.api.upgrades.Upgrades;
import appeng.block.AEBaseBlockItem;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.items.materials.CustomEntityItem;
import appeng.items.parts.PartItem;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ImpenRegistry {
    
    private static final List<ItemLike> ITEMS_LIST = new ArrayList<>();
    private static final List<BlockLike> BLOCKS_LIST = new ArrayList<>();

    // ***
    // Mostly boilerplate registry stuff
    // ***

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

    public static void register(final IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ENTITIES.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }

    public static void commonSetup(final FMLCommonSetupEvent event) {
        BEAMED_NETWORK_LINK.block().setBlockEntity(BeamedNetworkLinkBlockEntity.class,
                BEAMED_NETWORK_LINK.blockEntity(),
                null, (level, pos, state, be) -> ((ServerTickingBlockEntity) be).serverTick());
        SPATIAL_RIFT_SPAWNER.block().setBlockEntity(SpatialRiftSpawnerBlockEntity.class,
                SPATIAL_RIFT_SPAWNER.blockEntity(), null, null);
        SPATIAL_RIFT_COLLAPSER.block().setBlockEntity(SpatialRiftCollapserBlockEntity.class,
                SPATIAL_RIFT_COLLAPSER.blockEntity(), null, null);
        SPATIAL_RIFT_MANIPULATOR.block().setBlockEntity(SpatialRiftManipulatorBlockEntity.class,
                SPATIAL_RIFT_MANIPULATOR.blockEntity(), null, null);
        ATMOSPHERIC_CRYSTALLIZER.block().setBlockEntity(AtmosphericCrystallizerBlockEntity.class,
                ATMOSPHERIC_CRYSTALLIZER.blockEntity(), null, null);
        POSSIBILITY_DISINTEGRATOR.block().setBlockEntity(PossibilityDisintegratorBlockEntity.class,
                POSSIBILITY_DISINTEGRATOR.blockEntity(), null, null);
        EJECTION_DRIVE.block().setBlockEntity(EjectionDriveBlockEntity.class, EJECTION_DRIVE.blockEntity(), null,
                (level, pos, state, be) -> ((ServerTickingBlockEntity) be).serverTick());

        // AE2 upgrades need to be registered after normal registry events
        Upgrades.add(AEItems.SPEED_CARD, ATMOSPHERIC_CRYSTALLIZER.item(), 3);
        Upgrades.add(AEItems.SPEED_CARD, POSSIBILITY_DISINTEGRATOR.item(), 2);
        Upgrades.add(AEItems.CAPACITY_CARD, POSSIBILITY_DISINTEGRATOR.item(), 2);
    }

    // ***
    // Actual registry objects
    // ***

    // Machines
    //@formatter:off
    public static final MachineDefinition<BeamedNetworkLinkBlock, BeamedNetworkLinkBlockEntity> BEAMED_NETWORK_LINK =
            makeMachine("beamed_network_link", BeamedNetworkLinkBlock::new, BeamedNetworkLinkBlockEntity::new, true,
                    props -> props.noOcclusion());

    public static final MachineDefinition<AtmosphericCrystallizerBlock, AtmosphericCrystallizerBlockEntity> ATMOSPHERIC_CRYSTALLIZER =
            makeMachine("atmospheric_crystallizer", AtmosphericCrystallizerBlock::new, AtmosphericCrystallizerBlockEntity::new, true);

    public static final MachineDefinition<PossibilityDisintegratorBlock, PossibilityDisintegratorBlockEntity> POSSIBILITY_DISINTEGRATOR =
            makeMachine("possibility_disintegrator", PossibilityDisintegratorBlock::new, PossibilityDisintegratorBlockEntity::new, false);

    public static final MachineDefinition<EjectionDriveBlock, EjectionDriveBlockEntity> EJECTION_DRIVE =
            makeMachine("ejection_drive", EjectionDriveBlock::new, EjectionDriveBlockEntity::new, true);

    public static final MachineDefinition<SpatialRiftSpawnerBlock, SpatialRiftSpawnerBlockEntity> SPATIAL_RIFT_SPAWNER =
            makeMachine("spatial_rift_spawner", SpatialRiftSpawnerBlock::new, SpatialRiftSpawnerBlockEntity::new, false);

    public static final MachineDefinition<SpatialRiftCollapserBlock, SpatialRiftCollapserBlockEntity> SPATIAL_RIFT_COLLAPSER =
            makeMachine("spatial_rift_collapser", SpatialRiftCollapserBlock::new, SpatialRiftCollapserBlockEntity::new, false);

    public static final MachineDefinition<SpatialRiftManipulatorBlock, SpatialRiftManipulatorBlockEntity> SPATIAL_RIFT_MANIPULATOR =
            makeMachine("spatial_rift_manipulator", SpatialRiftManipulatorBlock::new, SpatialRiftManipulatorBlockEntity::new, false);
    //@formatter:on

    // Materials
    public static final ItemDefinition AEROCRYSTAL = makeItem("aerocrystal");
    public static final ItemDefinition BLAZING_AEROCRYSTAL = makeItem("blazing_aerocrystal");
    public static final ItemDefinition EXOTIC_AEROCRYSTAL = makeItem("exotic_aerocrystal");

    public static final ItemDefinition DISINTEGRATOR_CAPSULE_EMPTY = makeItem("disintegrator_capsule_empty");
    public static final ItemDefinition DISINTEGRATOR_CAPSULE_LUCK = makeItem("disintegrator_capsule_luck");
    public static final ItemDefinition DISINTEGRATOR_CAPSULE_LOOT = makeItem("disintegrator_capsule_loot");
    public static final ItemDefinition DISINTEGRATOR_CAPSULE_EGG = makeItem("disintegrator_capsule_egg");
    public static final ItemDefinition DISINTEGRATOR_CAPSULE_PLAYER_KILL = makeItem("disintegrator_capsule_player_kill");

    public static final ItemDefinition RIFT_SHARD = makeItem("rift_shard");

    // Fake item that is hidden in JEI
    public static final ItemDefinition FAKE_DIMENSION_PLACEHOLDER = makeItem("fake_dimension_placeholder");

    // Basic Blocks
    // TODO I have no idea if these are the right materials to use
    public static final BlockDefinition RIFTSTONE = makeBasicBlock("riftstone", Material.STONE);
    public static final BlockDefinition RIFT_SHARD_ORE = makeOreBlock("rift_shard_ore", Material.STONE);
    public static final BlockDefinition SMOOTH_RIFTSTONE = makeBasicBlock("smooth_riftstone", Material.STONE);
    public static final BlockDefinition RIFTSTONE_BRICKS = makeBasicBlock("riftstone_bricks", Material.STONE);
    public static final BlockDefinition RIFT_GLASS = makeCustomBlock("rift_glass",
            () -> new GlassBlock(BlockBehaviour.Properties.copy(Blocks.BLACK_STAINED_GLASS)));
    public static final BlockDefinition AEROCRYSTAL_BLOCK = makeBasicBlock("aerocrystal_block", Material.AMETHYST);
    public static final BlockDefinition BLAZING_AEROCRYSTAL_BLOCK = makeBasicBlock("blazing_aerocrystal_block",
            Material.AMETHYST);
    public static final BlockDefinition EXOTIC_AEROCRYSTAL_BLOCK = makeBasicBlock("exotic_aerocrystal_block",
            Material.AMETHYST);
    public static final BlockDefinition RIFT_SHARD_BLOCK = makeBasicBlock("rift_shard_block", Material.AMETHYST);

    // Droppable items
    public static final DroppableItemDefinition<RiftPrismEntity> RIFT_PRISM = makeDroppableItem(
            "rift_prism", RiftPrismEntity::new, RiftPrismEntity::new);
    public static final DroppableItemDefinition<StabilizedRiftPrismEntity> STABILIZED_RIFT_PRISM = makeDroppableItem(
            "stabilized_rift_prism", StabilizedRiftPrismEntity::new, StabilizedRiftPrismEntity::new);

    // Misc items
    public static final ItemDefinition LUNCHBOX_CELL_ITEM = makeItem("lunchbox_cell", LunchboxCellItem::new);

    public static final ItemDefinition CAPTURE_PLANE_ITEM = makeItem("capture_plane",
            () -> new PartItem<>(getItemProps(), CapturePlanePart.class, CapturePlanePart::new));

    public static final ItemDefinition RIFTED_SPATIAL_CELL_ITEM = makeItem("rifted_spatial_cell",
            RiftedSpatialCellItem::new);

    // Custom recipe types
    public static final RegistryObject<RecipeType<AtmosphericCrystallizerRecipe>> ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE = ImpenRegistry
            .<AtmosphericCrystallizerRecipe>makeRecipeType("atmospheric_crystallizer");
    public static final RegistryObject<RecipeSerializer<?>> ATMOSPHERIC_CRYSTALLIZER_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
            .register(ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE.getId().getPath(),
                    () -> AtmosphericCrystallizerRecipeSerializer.INSTANCE);

    public static final RegistryObject<RecipeType<SpatialRiftSpawnerRecipe>> SPATIAL_RIFT_SPAWNER_RECIPE_TYPE = ImpenRegistry
            .<SpatialRiftSpawnerRecipe>makeRecipeType("spatial_rift_spawner");
    public static final RegistryObject<RecipeSerializer<?>> SPATIAL_RIFT_SPAWNER_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
            .register(SPATIAL_RIFT_SPAWNER_RECIPE_TYPE.getId().getPath(),
                    () -> SpatialRiftSpawnerRecipeSerializer.INSTANCE);
    
    public static final RegistryObject<RecipeType<SpatialRiftCollapserRecipe>> SPATIAL_RIFT_COLLAPSER_RECIPE_TYPE = ImpenRegistry
            .<SpatialRiftCollapserRecipe>makeRecipeType("spatial_rift_collapser");
    public static final RegistryObject<RecipeSerializer<?>> SPATIAL_RIFT_COLLAPSER_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
            .register(SPATIAL_RIFT_COLLAPSER_RECIPE_TYPE.getId().getPath(),
                    () -> SpatialRiftCollapserRecipeSerializer.INSTANCE);

    public static final RegistryObject<RecipeType<SpatialRiftManipulatorRecipe>> SPATIAL_RIFT_MANIPULATOR_RECIPE_TYPE = ImpenRegistry
            .<SpatialRiftManipulatorRecipe>makeRecipeType("spatial_rift_manipulator");
    public static final RegistryObject<RecipeSerializer<?>> SPATIAL_RIFT_MANIPULATOR_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
            .register(SPATIAL_RIFT_MANIPULATOR_RECIPE_TYPE.getId().getPath(),
                    () -> SpatialRiftManipulatorRecipeSerializer.INSTANCE);

    // ***
    // Helper methods
    // ***

    private static <B extends Block, E extends BlockEntity> MachineDefinition<B, E> makeMachine(final String name,
            final Function<BlockBehaviour.Properties, B> blockSupplier,
            final BlockEntityType.BlockEntitySupplier<E> blockEntitySupplier, final boolean orientable) {
        return ImpenRegistry.makeMachine(name, blockSupplier, blockEntitySupplier, orientable, Function.identity());
    }

    private static <B extends Block, E extends BlockEntity> MachineDefinition<B, E> makeMachine(final String name,
            final Function<BlockBehaviour.Properties, B> blockSupplier,
            final BlockEntityType.BlockEntitySupplier<E> blockEntitySupplier, final boolean orientable,
            final Function<BlockBehaviour.Properties, BlockBehaviour.Properties> propsModifier) {
        final var blockProps = BlockBehaviour.Properties.of(Material.METAL);
        return ImpenRegistry.makeMachine(name, blockSupplier, blockEntitySupplier, orientable,
                propsModifier.apply(blockProps));
    }

    private static <B extends Block, E extends BlockEntity> MachineDefinition<B, E> makeMachine(final String name,
            final Function<BlockBehaviour.Properties, B> blockSupplier,
            final BlockEntityType.BlockEntitySupplier<E> blockEntitySupplier, final boolean orientable,
            final BlockBehaviour.Properties blockProps) {
        final var blockHolder = BLOCKS
                .register(name, () -> blockSupplier.apply(blockProps));
        final var blockEntityHolder = BLOCK_ENTITIES
                .register(blockHolder.getId().getPath(),
                        () -> BlockEntityType.Builder.of(blockEntitySupplier, blockHolder.get()).build(null));
        final var blockItemHolder = ITEMS.register(
                blockHolder.getId().getPath(), () -> {
                    final var itemProps = getItemProps();
                    if (orientable) {
                        return new AEBaseBlockItem(blockHolder.get(), itemProps);
                    }
                    else {
                        return new BlockItem(blockHolder.get(), itemProps);
                    }
                });
        final var def = new MachineDefinition<>(blockHolder, blockEntityHolder, blockItemHolder);
        ImpenRegistry.BLOCKS_LIST.add(def);
        ImpenRegistry.ITEMS_LIST.add(def);
        return def;
    }

    public static ItemDefinition makeItem(final String name, final Function<Item.Properties, ? extends Item> func) {
        return makeItem(name, () -> func.apply(getItemProps()));
    }

    public static ItemDefinition makeItem(final String name) {
        return makeItem(name, Item::new);
    }

    public static ItemDefinition makeItem(final String name, final Supplier<Item> sup) {
        final var def = new ItemDefinition(ITEMS.register(name, sup));
        ImpenRegistry.ITEMS_LIST.add(def);
        return def;
    }

    private static <E extends Entity> DroppableItemDefinition<E> makeDroppableItem(final String name,
            final EntityType.EntityFactory<E> entityFunc, final CustomEntityItem.EntityFactory entityFuncAgain) {
        final var itemHolder = ITEMS.register(name,
                () -> new CustomEntityItem(getItemProps(), entityFuncAgain));
        final var entityHolder = ENTITIES
                .register(itemHolder.getId().getPath(), () -> EntityType.Builder
                        .<E>of(entityFunc, MobCategory.MISC).sized(0.25F, 0.25F)
                        .clientTrackingRange(6).updateInterval(20).build(ImpracticalEnergisticsMod.MOD_ID));
        final var def = new DroppableItemDefinition<E>(itemHolder, entityHolder);
        ImpenRegistry.ITEMS_LIST.add(def);
        return def;
    }

    private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> makeRecipeType(final String key) {
        return RECIPE_TYPES.register(key, () -> new RecipeType<T>() {
            @Override
            public String toString() {
                return key;
            }
        });
    }

    private static BlockDefinition makeCustomBlock(final String name, final Supplier<Block> sup) {
        final var blockHolder = BLOCKS.register(name, sup);
        final var itemHolder = ITEMS.register(blockHolder.getId().getPath(),
                () -> new BlockItem(blockHolder.get(), getItemProps()));
        final var def = new BlockDefinition(blockHolder, itemHolder);
        ImpenRegistry.BLOCKS_LIST.add(def);
        ImpenRegistry.ITEMS_LIST.add(def);
        return def;
    }

    private static BlockDefinition makeBasicBlock(final String name, final Material mat) {
        return ImpenRegistry.makeBasicBlock(name, BlockBehaviour.Properties.of(mat));
    }
    
    private static BlockDefinition makeBasicBlock(final String name, final BlockBehaviour.Properties props) {
        return ImpenRegistry.makeCustomBlock(name, () -> new Block(props));
    }

    private static BlockDefinition makeOreBlock(final String name, final Material mat) {
        return ImpenRegistry.makeCustomBlock(name, () -> new OreBlock(BlockBehaviour.Properties.copy(Blocks.IRON_ORE)));
    }

    private static Item.Properties getItemProps() {
        return new Item.Properties().tab(ImpenCreativeModeTab.TAB);
    }
    
    // ***
    // Convenience methods for scanning the registry
    // ***

    public static List<ItemLike> getRegisteredItems() {
        return ITEMS_LIST;
    }

    public static List<BlockLike> getRegisteredBlocks() {
        return BLOCKS_LIST;
    }
    
    // ***
    // Many static inner classes
    // ***

    public static interface BlockLike {
        Block asBlock();
    }

    public static class MachineDefinition<B extends Block, E extends BlockEntity>
            implements ItemLike, BlockLike, RegistryIdProvider {
        private final RegistryObject<B> blockHolder;
        private final RegistryObject<BlockEntityType<E>> blockEntityHolder;
        private final RegistryObject<BlockItem> blockItemHolder;

        private MachineDefinition(final RegistryObject<B> blockHolder,
                final RegistryObject<BlockEntityType<E>> blockEntityHolder,
                final RegistryObject<BlockItem> blockItemHolder) {
            this.blockHolder = blockHolder;
            this.blockEntityHolder = blockEntityHolder;
            this.blockItemHolder = blockItemHolder;
        }

        public B block() {
            return blockHolder.get();
        }

        public BlockEntityType<E> blockEntity() {
            return blockEntityHolder.get();
        }

        public BlockItem item() {
            return blockItemHolder.get();
        }

        @Override
        public Item asItem() {
            return this.item();
        }

        @Override
        public Block asBlock() {
            return this.block();
        }

        @Override
        public String getKey() {
            return blockHolder.getId().getPath();
        }
    }

    public static class PlantDefinition<B extends CropBlock> implements ItemLike, BlockLike, RegistryIdProvider {
        private final RegistryObject<B> blockHolder;
        private final RegistryObject<? extends Item> itemHolder;

        private PlantDefinition(final RegistryObject<B> blockHolder, final RegistryObject<? extends Item> itemHolder) {
            this.blockHolder = blockHolder;
            this.itemHolder = itemHolder;
        }

        public B block() {
            return blockHolder.get();
        }

        public Item item() {
            return itemHolder.get();
        }

        @Override
        public Block asBlock() {
            return block();
        }

        @Override
        public Item asItem() {
            return item();
        }

        @Override
        public String getKey() {
            return blockHolder.getId().getPath();
        }
    }

    public static class DroppableItemDefinition<E extends Entity> implements ItemLike, RegistryIdProvider {
        private final RegistryObject<? extends Item> itemHolder;
        private final RegistryObject<EntityType<E>> entityHolder;

        private DroppableItemDefinition(final RegistryObject<? extends Item> itemHolder,
                final RegistryObject<EntityType<E>> entityHolder) {
            this.itemHolder = itemHolder;
            this.entityHolder = entityHolder;
        }

        public Item item() {
            return itemHolder.get();
        }

        public EntityType<E> entity() {
            return entityHolder.get();
        }

        @Override
        public Item asItem() {
            return this.item();
        }

        @Override
        public String getKey() {
            return itemHolder.getId().getPath();
        }
    }

    public static class BlockDefinition implements ItemLike, BlockLike, RegistryIdProvider {
        private final RegistryObject<? extends Block> blockHolder;
        private final RegistryObject<? extends Item> itemHolder;

        private BlockDefinition(final RegistryObject<? extends Block> blockHolder,
                final RegistryObject<? extends Item> itemHolder) {
            this.blockHolder = blockHolder;
            this.itemHolder = itemHolder;
        }

        public Block block() {
            return blockHolder.get();
        }

        public Item item() {
            return itemHolder.get();
        }

        @Override
        public Item asItem() {
            return this.item();
        }

        @Override
        public Block asBlock() {
            return this.block();
        }

        @Override
        public String getKey() {
            return itemHolder.getId().getPath();
        }
    }
    
    public static class ItemDefinition implements ItemLike, RegistryIdProvider {
        private final RegistryObject<? extends Item> itemHolder;

        private ItemDefinition(final RegistryObject<? extends Item> itemHolder) {
            this.itemHolder = itemHolder;
        }

        @Override
        public Item asItem() {
            return this.itemHolder.get();
        }

        @Override
        public String getKey() {
            return itemHolder.getId().getPath();
        }
    }
    
    public static interface RegistryIdProvider {
        String getKey();
    }
}
