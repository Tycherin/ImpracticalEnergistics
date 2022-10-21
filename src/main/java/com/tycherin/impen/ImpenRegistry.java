package com.tycherin.impen;

import java.util.function.Function;

import org.lwjgl.system.CallbackI.B;

import com.tycherin.impen.block.AtmosphericCrystallizerBlock;
import com.tycherin.impen.block.BeamedNetworkLinkBlock;
import com.tycherin.impen.block.PlantableCertusBlock;
import com.tycherin.impen.block.PlantableFluixBlock;
import com.tycherin.impen.block.PossibilityDisintegratorBlock;
import com.tycherin.impen.block.SpatialRiftManipulatorBlock;
import com.tycherin.impen.block.SpatialRiftStabilizerBlock;
import com.tycherin.impen.block.ToasterDriveBlock;
import com.tycherin.impen.blockentity.AtmosphericCrystallizerBlockEntity;
import com.tycherin.impen.blockentity.BeamedNetworkLinkBlockEntity;
import com.tycherin.impen.blockentity.PossibilityDisintegratorBlockEntity;
import com.tycherin.impen.blockentity.SpatialRiftManipulatorBlockEntity;
import com.tycherin.impen.blockentity.SpatialRiftStabilizerBlockEntity;
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
import com.tycherin.impen.recipe.AtmosphericCrystallizerRecipe;
import com.tycherin.impen.recipe.AtmosphericCrystallizerRecipeSerializer;
import com.tycherin.impen.recipe.RiftCatalystRecipe;
import com.tycherin.impen.recipe.RiftCatalystRecipeSerializer;

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
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("hiding") // Parameterized static inner classes are weird
public class ImpenRegistry {

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
        SPATIAL_RIFT_MANIPULATOR.block().setBlockEntity(SpatialRiftManipulatorBlockEntity.class,
                SPATIAL_RIFT_MANIPULATOR.blockEntity(), null, null);
        SPATIAL_RIFT_STABILIZER.block().setBlockEntity(SpatialRiftStabilizerBlockEntity.class,
                SPATIAL_RIFT_STABILIZER.blockEntity(), null, null);
        ATMOSPHERIC_CRYSTALLIZER.block().setBlockEntity(AtmosphericCrystallizerBlockEntity.class,
                ATMOSPHERIC_CRYSTALLIZER.blockEntity(), null, null);
        POSSIBILITY_DISINTEGRATOR.block().setBlockEntity(PossibilityDisintegratorBlockEntity.class,
                POSSIBILITY_DISINTEGRATOR.blockEntity(), null, null);
        TOASTER_DRIVE.block().setBlockEntity(ToasterDriveBlockEntity.class, TOASTER_DRIVE.blockEntity(), null,
                (level, pos, state, be) -> ((ServerTickingBlockEntity) be).serverTick());

        // AE2 upgrades need to be registered after normal registry events
        Upgrades.add(AEItems.SPEED_CARD, ATMOSPHERIC_CRYSTALLIZER.item(), 3);
        Upgrades.add(AEItems.SPEED_CARD, POSSIBILITY_DISINTEGRATOR.item(), 2);
        Upgrades.add(AEItems.CAPACITY_CARD, POSSIBILITY_DISINTEGRATOR.item(), 2);
        Upgrades.add(AEItems.SPEED_CARD, SPATIAL_RIFT_MANIPULATOR.item(), 3);
    }

    // ***
    // Actual registry objects
    // ***

    // Machines
    //@formatter:off
    public static final MachineDefinition<BeamedNetworkLinkBlock, BeamedNetworkLinkBlockEntity> BEAMED_NETWORK_LINK =
            makeMachine("beamed_network_link", BeamedNetworkLinkBlock::new, BeamedNetworkLinkBlockEntity::new, true);

    public static final MachineDefinition<SpatialRiftManipulatorBlock, SpatialRiftManipulatorBlockEntity> SPATIAL_RIFT_MANIPULATOR =
            makeMachine("spatial_rift_manipulator", SpatialRiftManipulatorBlock::new, SpatialRiftManipulatorBlockEntity::new, false);

    public static final MachineDefinition<SpatialRiftStabilizerBlock, SpatialRiftStabilizerBlockEntity> SPATIAL_RIFT_STABILIZER =
            makeMachine("spatial_rift_stabilizer", SpatialRiftStabilizerBlock::new, SpatialRiftStabilizerBlockEntity::new, false);

    public static final MachineDefinition<AtmosphericCrystallizerBlock, AtmosphericCrystallizerBlockEntity> ATMOSPHERIC_CRYSTALLIZER =
            makeMachine("atmospheric_crystallizer", AtmosphericCrystallizerBlock::new, AtmosphericCrystallizerBlockEntity::new, true);

    public static final MachineDefinition<PossibilityDisintegratorBlock, PossibilityDisintegratorBlockEntity> POSSIBILITY_DISINTEGRATOR =
            makeMachine("possibility_disintegrator", PossibilityDisintegratorBlock::new, PossibilityDisintegratorBlockEntity::new, false);

    public static final MachineDefinition<ToasterDriveBlock, ToasterDriveBlockEntity> TOASTER_DRIVE =
            makeMachine("toaster_drive", ToasterDriveBlock::new, ToasterDriveBlockEntity::new, true);
    //@formatter:on

    // Plantable crops
    public static final PlantDefinition<PlantableCertusBlock> PLANTABLE_CERTUS = makeCrop("plantable_certus",
            "plantable_certus_seeds", PlantableCertusBlock::new);
    public static final PlantDefinition<PlantableFluixBlock> PLANTABLE_FLUIX = makeCrop("plantable_fluix",
            "plantable_fluix_seeds", PlantableFluixBlock::new);

    // Tools
    public static final RegistryObject<Item> RIFT_AXE_ITEM = makeTool("rift_axe", RiftAxeItem::new);
    public static final RegistryObject<Item> RIFT_HOE_ITEM = makeTool("rift_hoe", RiftHoeItem::new);
    public static final RegistryObject<Item> RIFT_PICKAXE_ITEM = makeTool("rift_pickaxe", RiftPickaxeItem::new);
    public static final RegistryObject<Item> RIFT_SPADE_ITEM = makeTool("rift_spade", RiftSpadeItem::new);
    public static final RegistryObject<Item> RIFT_SWORD_ITEM = makeTool("rift_sword", RiftSwordItem::new);

    // Materials
    public static final RegistryObject<Item> AEROCRYSTAL = makeItem("aerocrystal");
    public static final RegistryObject<Item> BLAZING_AEROCRYSTAL = makeItem("blazing_aerocrystal");
    public static final RegistryObject<Item> EXOTIC_AEROCRYSTAL = makeItem("exotic_aerocrystal");

    public static final RegistryObject<Item> BASIC_RIFT_CATALYST = makeItem("catalyst_base_t1");
    public static final RegistryObject<Item> ADVANCED_RIFT_CATALYST = makeItem("catalyst_base_t2");
    public static final RegistryObject<Item> PRISTINE_RIFT_CATALYST = makeItem("catalyst_base_t3");
    public static final RegistryObject<Item> OVERWORLD_ORE_CATALYST = makeItem("catalyst_overworld_ore");
    public static final RegistryObject<Item> DEEPSLATE_ORE_CATALYST = makeItem("catalyst_deepslate_ore");
    public static final RegistryObject<Item> DEEPSLATE_GEM_CATALYST = makeItem("catalyst_deepslate_gem");
    public static final RegistryObject<Item> NETHER_ORE_CATALYST = makeItem("catalyst_nether_ore");
    public static final RegistryObject<Item> NETHER_SECRET_CATALYST = makeItem("catalyst_nether_secret");
    public static final RegistryObject<Item> END_SECRET_CATALYST = makeItem("catalyst_end_secret");
    public static final RegistryObject<Item> RIFT_SPACE_CATALYST = makeItem("catalyst_rift_ore");
    public static final RegistryObject<Item> OVERWORLD_SECRET_CATALYST = makeItem("catalyst_overworld_secret");
    public static final RegistryObject<Item> BLACKSTONE_SECRET_CATALYST = makeItem("catalyst_blackstone_secret");
    // TODO: Add catalysts integrating ores from other mods via tags

    public static final RegistryObject<Item> DISINTEGRATOR_CAPSULE_EMPTY = makeItem("disintegrator_capsule_empty");
    public static final RegistryObject<Item> DISINTEGRATOR_CAPSULE_LUCK = makeItem("disintegrator_capsule_luck");
    public static final RegistryObject<Item> DISINTEGRATOR_CAPSULE_LOOT = makeItem("disintegrator_capsule_loot");
    public static final RegistryObject<Item> DISINTEGRATOR_CAPSULE_EGG = makeItem("disintegrator_capsule_egg");
    public static final RegistryObject<Item> DISINTEGRATOR_CAPSULE_PLAYER_KILL = makeItem("disintegrator_capsule_player_kill");

    public static final RegistryObject<Item> RIFT_SHARD = makeItem("rift_shard");

    // TODO Hide this from JEI
    public static final RegistryObject<Item> FAKE_DIMENSION_PLACEHOLDER = makeItem("fake_dimension_placeholder");

    // Basic Blocks
    // TODO I have no idea if these are the right materials to use
    public static final BlockDefinition RIFTSTONE = makeBasicBlock("riftstone", Material.STONE);
    public static final BlockDefinition RIFT_SHARD_ORE;
    static {
        final var blockHolder = BLOCKS.register("rift_shard_ore", () -> new OreBlock(
                BlockBehaviour.Properties.copy(Blocks.REDSTONE_ORE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)));
        final var itemHolder = ITEMS.register("rift_shard_ore",
                () -> new ItemNameBlockItem(blockHolder.get(), getItemProps()));
        RIFT_SHARD_ORE = new BlockDefinition(blockHolder, itemHolder);
    }
    public static final BlockDefinition SMOOTH_RIFTSTONE = makeBasicBlock("smooth_riftstone", Material.STONE);
    public static final BlockDefinition RIFTSTONE_BRICK = makeBasicBlock("riftstone_brick", Material.STONE);
    public static final BlockDefinition RIFT_GLASS = makeBasicBlock("rift_glass", Material.GLASS);
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
    public static final RegistryObject<LunchboxCellItem> LUNCHBOX_CELL_ITEM = ITEMS.register("lunchbox_cell",
            () -> new LunchboxCellItem());

    public static final RegistryObject<Item> CAPTURE_PLANE_ITEM = ITEMS.register("capture_plane",
            () -> new PartItem<>(getItemProps(), CapturePlanePart.class, CapturePlanePart::new));

    // Custom recipe types
    public static final RegistryObject<RecipeType<RiftCatalystRecipe>> RIFT_CATALYST_RECIPE_TYPE = ImpenRegistry
            .<RiftCatalystRecipe>makeRecipeType("rift_catalyst");
    public static final RegistryObject<RecipeSerializer<?>> RIFT_CATALYST_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
            .register(RIFT_CATALYST_RECIPE_TYPE.getId().getPath(), () -> RiftCatalystRecipeSerializer.INSTANCE);
    public static final RegistryObject<RecipeType<AtmosphericCrystallizerRecipe>> ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE = ImpenRegistry
            .<AtmosphericCrystallizerRecipe>makeRecipeType("atmospheric_crystallizer");
    public static final RegistryObject<RecipeSerializer<?>> ATMOSPHERIC_CRYSTALLIZER_RECIPE_SERIALIZER = RECIPE_SERIALIZERS
            .register(ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE.getId().getPath(),
                    () -> AtmosphericCrystallizerRecipeSerializer.INSTANCE);

    // ***
    // Helper methods
    // ***

    private static <B extends Block, E extends BlockEntity> MachineDefinition<B, E> makeMachine(final String name,
            final Function<BlockBehaviour.Properties, B> blockSupplier,
            final BlockEntityType.BlockEntitySupplier<E> blockEntitySupplier, final boolean orientable) {
        final var blockHolder = BLOCKS
                .register(name, () -> blockSupplier.apply(BlockBehaviour.Properties.of(Material.METAL)));
        final var blockEntityHolder = BLOCK_ENTITIES
                .register(blockHolder.getId().getPath(),
                        () -> BlockEntityType.Builder.of(blockEntitySupplier, blockHolder.get()).build(null));
        final var blockItemHolder = ITEMS.register(
                blockHolder.getId().getPath(), () -> {
                    final var props = getItemProps();
                    if (orientable) {
                        return new AEBaseBlockItem(blockHolder.get(), props);
                    }
                    else {
                        return new BlockItem(blockHolder.get(), props);
                    }
                });
        return new MachineDefinition<>(blockHolder, blockEntityHolder, blockItemHolder);
    }

    public static RegistryObject<Item> makeTool(final String name,
            final Function<Item.Properties, ? extends Item> func) {
        return ITEMS.register(name, () -> func.apply(getItemProps()));
    }

    public static RegistryObject<Item> makeItem(final String name) {
        return ITEMS.register(name, () -> new Item(getItemProps()));
    }

    private static <BT extends CropBlock> PlantDefinition<BT> makeCrop(final String cropName, final String seedName,
            final Function<BlockBehaviour.Properties, BT> func) {
        final var blockHolder = BLOCKS.register(cropName, () -> func.apply(BlockBehaviour.Properties.of(Material.PLANT)
                .noCollission().randomTicks().instabreak().sound(SoundType.CROP)));
        final var itemHolder = ITEMS.register(seedName,
                () -> new ItemNameBlockItem(blockHolder.get(), getItemProps()));
        return new PlantDefinition<BT>(blockHolder, itemHolder);
    }

    private static <E extends Entity> DroppableItemDefinition<E> makeDroppableItem(final String name,
            final EntityType.EntityFactory<E> entityFunc, final CustomEntityItem.EntityFactory entityFuncAgain) {
        final var itemHolder = ITEMS.register(name,
                () -> new CustomEntityItem(getItemProps(), entityFuncAgain));
        final var entityHolder = ENTITIES
                .register(itemHolder.getId().getPath(), () -> EntityType.Builder
                        .<E>of(entityFunc, MobCategory.MISC).sized(0.25F, 0.25F)
                        .clientTrackingRange(6).updateInterval(20).build(ImpracticalEnergisticsMod.MOD_ID));
        return new DroppableItemDefinition<E>(itemHolder, entityHolder);
    }

    private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> makeRecipeType(final String key) {
        return RECIPE_TYPES.register(key, () -> new RecipeType<T>() {
            @Override
            public String toString() {
                return key;
            }
        });
    }

    private static BlockDefinition makeBasicBlock(final String name, final Material mat) {
        final var blockHolder = BLOCKS.register(name, () -> new Block(BlockBehaviour.Properties.of(mat)));
        final var itemHolder = ITEMS.register(name, () -> new ItemNameBlockItem(blockHolder.get(), getItemProps()));
        return new BlockDefinition(blockHolder, itemHolder);
    }

    private static Item.Properties getItemProps() {
        // TODO Use real creative tab
        return new Item.Properties().tab(CreativeModeTab.TAB_MISC);
    }

    // ***
    // Many static inner classes
    // ***

    public static class MachineDefinition<B extends Block, E extends BlockEntity> {
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
    }

    public static class PlantDefinition<B extends CropBlock> {
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
    }

    public static class DroppableItemDefinition<E extends Entity> {
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
    }

    public static class BlockDefinition {
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
    }
}
