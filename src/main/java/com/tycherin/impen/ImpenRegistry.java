package com.tycherin.impen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.tycherin.impen.block.AtmosphericCrystallizerBlock;
import com.tycherin.impen.block.EjectionDriveBlock;
import com.tycherin.impen.block.OreBlock;
import com.tycherin.impen.block.PhaseFieldControllerBlock;
import com.tycherin.impen.block.beam.BeamNetworkAmplifierBlock;
import com.tycherin.impen.block.beam.BeamNetworkEmitterBlock;
import com.tycherin.impen.block.beam.BeamNetworkMirrorBlock;
import com.tycherin.impen.block.beam.BeamNetworkReceiverBlock;
import com.tycherin.impen.block.beam.BeamNetworkSplitterBlock;
import com.tycherin.impen.block.rift.SpatialRiftCollapserBlock;
import com.tycherin.impen.block.rift.SpatialRiftManipulatorBlock;
import com.tycherin.impen.block.rift.SpatialRiftSpawnerBlock;
import com.tycherin.impen.blockentity.AtmosphericCrystallizerBlockEntity;
import com.tycherin.impen.blockentity.EjectionDriveBlockEntity;
import com.tycherin.impen.blockentity.PhaseFieldControllerBlockEntity;
import com.tycherin.impen.blockentity.beam.BeamNetworkAmplifierBlockEntity;
import com.tycherin.impen.blockentity.beam.BeamNetworkEmitterBlockEntity;
import com.tycherin.impen.blockentity.beam.BeamNetworkMirrorBlockEntity;
import com.tycherin.impen.blockentity.beam.BeamNetworkReceiverBlockEntity;
import com.tycherin.impen.blockentity.beam.BeamNetworkSplitterBlockEntity;
import com.tycherin.impen.blockentity.rift.SpatialRiftCollapserBlockEntity;
import com.tycherin.impen.blockentity.rift.SpatialRiftManipulatorBlockEntity;
import com.tycherin.impen.blockentity.rift.SpatialRiftSpawnerBlockEntity;
import com.tycherin.impen.client.particle.DisintegratorDamageParticle;
import com.tycherin.impen.client.particle.DisintegratorLockParticle;
import com.tycherin.impen.item.LunchboxCellItem;
import com.tycherin.impen.item.SpatialRiftCellItem;
import com.tycherin.impen.item.StaticTooltipItem;
import com.tycherin.impen.part.CapturePlanePart;
import com.tycherin.impen.part.PhaseFieldEmitterPart;
import com.tycherin.impen.recipe.AtmosphericCrystallizerRecipe;
import com.tycherin.impen.recipe.SpatialRiftCollapserRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorBaseBlockRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorBlockWeightRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorCraftingRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorSpecialRecipe;
import com.tycherin.impen.recipe.SpatialRiftSpawnerRecipe;

import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.api.upgrades.Upgrades;
import appeng.block.AEBaseBlockItem;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.parts.AEBasePart;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
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
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister
            .create(ForgeRegistries.PARTICLE_TYPES, ImpracticalEnergisticsMod.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister
            .create(Registry.RECIPE_TYPE_REGISTRY, ImpracticalEnergisticsMod.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, ImpracticalEnergisticsMod.MOD_ID);

    public static void register(final IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ENTITIES.register(modEventBus);
        PARTICLES.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }

    public static void commonSetup(final FMLCommonSetupEvent event) {
        SPATIAL_RIFT_SPAWNER.block().setBlockEntity(SpatialRiftSpawnerBlockEntity.class,
                SPATIAL_RIFT_SPAWNER.blockEntity(), null, null);
        SPATIAL_RIFT_COLLAPSER.block().setBlockEntity(SpatialRiftCollapserBlockEntity.class,
                SPATIAL_RIFT_COLLAPSER.blockEntity(), null, null);
        SPATIAL_RIFT_MANIPULATOR.block().setBlockEntity(SpatialRiftManipulatorBlockEntity.class,
                SPATIAL_RIFT_MANIPULATOR.blockEntity(), null, null);
        ATMOSPHERIC_CRYSTALLIZER.block().setBlockEntity(AtmosphericCrystallizerBlockEntity.class,
                ATMOSPHERIC_CRYSTALLIZER.blockEntity(), null, null);
        PHASE_FIELD_CONTROLLER.block().setBlockEntity(PhaseFieldControllerBlockEntity.class,
                PHASE_FIELD_CONTROLLER.blockEntity(), null, null);
        BEAM_NETWORK_AMPLIFIER.block().setBlockEntity(BeamNetworkAmplifierBlockEntity.class,
                BEAM_NETWORK_AMPLIFIER.blockEntity(), null, null);
        BEAM_NETWORK_EMITTER.block().setBlockEntity(BeamNetworkEmitterBlockEntity.class,
                BEAM_NETWORK_EMITTER.blockEntity(), null, null);
        BEAM_NETWORK_MIRROR.block().setBlockEntity(BeamNetworkMirrorBlockEntity.class,
                BEAM_NETWORK_MIRROR.blockEntity(), null, null);
        BEAM_NETWORK_RECEIVER.block().setBlockEntity(BeamNetworkReceiverBlockEntity.class,
                BEAM_NETWORK_RECEIVER.blockEntity(), null, null);
        BEAM_NETWORK_SPLITTER.block().setBlockEntity(BeamNetworkSplitterBlockEntity.class,
                BEAM_NETWORK_SPLITTER.blockEntity(), null, null);
        EJECTION_DRIVE.block().setBlockEntity(EjectionDriveBlockEntity.class, EJECTION_DRIVE.blockEntity(), null,
                (level, pos, state, be) -> ((ServerTickingBlockEntity)be).serverTick());

        // AE2 upgrades need to be registered after normal registry events
        Upgrades.add(AEItems.SPEED_CARD, ATMOSPHERIC_CRYSTALLIZER.item(), 3);
    }

    private static final BlockBehaviour.Properties MACHINE_BLOCK_PROPS = BlockBehaviour.Properties.of(Material.METAL)
            .strength(3f, 10f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops();
    private static final BlockBehaviour.Properties ORE_BLOCK_PROPS = BlockBehaviour.Properties.of(Material.STONE)
            .strength(3f, 5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops();
    private static final BlockBehaviour.Properties RIFTSTONE_BLOCK_PROPS = BlockBehaviour.Properties.of(Material.STONE)
            .strength(6f, 30f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops();

    // ***
    // Actual registry objects
    // ***

    // Machines
    //@formatter:off
    public static final MachineDefinition<AtmosphericCrystallizerBlock, AtmosphericCrystallizerBlockEntity> ATMOSPHERIC_CRYSTALLIZER =
            makeMachine("atmospheric_crystallizer", AtmosphericCrystallizerBlock::new, AtmosphericCrystallizerBlockEntity::new, true);

    public static final MachineDefinition<PhaseFieldControllerBlock, PhaseFieldControllerBlockEntity> PHASE_FIELD_CONTROLLER =
            makeMachine("phase_field_controller", PhaseFieldControllerBlock::new, PhaseFieldControllerBlockEntity::new, false);

    public static final MachineDefinition<EjectionDriveBlock, EjectionDriveBlockEntity> EJECTION_DRIVE =
            makeMachine("ejection_drive", EjectionDriveBlock::new, EjectionDriveBlockEntity::new, true);

    public static final MachineDefinition<SpatialRiftSpawnerBlock, SpatialRiftSpawnerBlockEntity> SPATIAL_RIFT_SPAWNER =
            makeMachine("spatial_rift_spawner", SpatialRiftSpawnerBlock::new, SpatialRiftSpawnerBlockEntity::new, false);

    public static final MachineDefinition<SpatialRiftCollapserBlock, SpatialRiftCollapserBlockEntity> SPATIAL_RIFT_COLLAPSER =
            makeMachine("spatial_rift_collapser", SpatialRiftCollapserBlock::new, SpatialRiftCollapserBlockEntity::new, false);

    public static final MachineDefinition<SpatialRiftManipulatorBlock, SpatialRiftManipulatorBlockEntity> SPATIAL_RIFT_MANIPULATOR =
            makeMachine("spatial_rift_manipulator", SpatialRiftManipulatorBlock::new, SpatialRiftManipulatorBlockEntity::new, false);
    
    public static final MachineDefinition<BeamNetworkAmplifierBlock, BeamNetworkAmplifierBlockEntity> BEAM_NETWORK_AMPLIFIER =
            makeMachine("beam_network_amplifier", BeamNetworkAmplifierBlock::new, BeamNetworkAmplifierBlockEntity::new, true,
                    props -> props.noOcclusion());
    public static final MachineDefinition<BeamNetworkEmitterBlock, BeamNetworkEmitterBlockEntity> BEAM_NETWORK_EMITTER =
            makeMachine("beam_network_emitter", BeamNetworkEmitterBlock::new, BeamNetworkEmitterBlockEntity::new, true,
                    props -> props.noOcclusion());
    public static final MachineDefinition<BeamNetworkReceiverBlock, BeamNetworkReceiverBlockEntity> BEAM_NETWORK_RECEIVER =
            makeMachine("beam_network_receiver", BeamNetworkReceiverBlock::new, BeamNetworkReceiverBlockEntity::new, true);
    public static final MachineDefinition<BeamNetworkMirrorBlock, BeamNetworkMirrorBlockEntity> BEAM_NETWORK_MIRROR =
            makeMachine("beam_network_mirror", BeamNetworkMirrorBlock::new, BeamNetworkMirrorBlockEntity::new, true,
                    props -> props.noOcclusion());
    public static final MachineDefinition<BeamNetworkSplitterBlock, BeamNetworkSplitterBlockEntity> BEAM_NETWORK_SPLITTER =
            makeMachine("beam_network_splitter", BeamNetworkSplitterBlock::new, BeamNetworkSplitterBlockEntity::new, true,
                    props -> props.noOcclusion());
    
    //@formatter:on

    // Basic items
    public static final ItemDefinition AEROCRYSTAL = makeItem("aerocrystal");
    public static final ItemDefinition BLAZING_AEROCRYSTAL = makeItem("blazing_aerocrystal");
    public static final ItemDefinition EXOTIC_AEROCRYSTAL = makeItem("exotic_aerocrystal");
    public static final ItemDefinition RIFT_SHARD = makeItem("rift_shard");
    public static final ItemDefinition RIFT_PRISM = makeItem("rift_prism");
    public static final ItemDefinition STABILIZED_RIFT_PRISM = makeItem("stabilized_rift_prism");
    public static final ItemDefinition AEROCRYSTAL_ASSEMBLY = makeItem("aerocrystal_assembly");
    public static final ItemDefinition AEROCRYSTAL_PRISM = makeItem("aerocrystal_prism");
    public static final ItemDefinition SPATIAL_MACHINE_CORE = makeItem("spatial_machine_core");
    public static final ItemDefinition RIFTSTONE_DUST = makeItem("riftstone_dust");
    public static final ItemDefinition RIFT_ALLOY_INGOT = makeItem("rift_alloy_ingot");

    public static final ItemDefinition PHASIC_CAPSULE_EMPTY = makeItem("phasic_capsule_empty");
    public static final ItemDefinition PHASIC_CAPSULE_RED = makeItemWithTooltip("phasic_capsule_red");
    public static final ItemDefinition PHASIC_CAPSULE_ORANGE = makeItemWithTooltip("phasic_capsule_orange");
    public static final ItemDefinition PHASIC_CAPSULE_YELLOW = makeItemWithTooltip("phasic_capsule_yellow");
    public static final ItemDefinition PHASIC_CAPSULE_LIME = makeItemWithTooltip("phasic_capsule_lime");
    public static final ItemDefinition PHASIC_CAPSULE_GREEN = makeItemWithTooltip("phasic_capsule_green");
    public static final ItemDefinition PHASIC_CAPSULE_WHITE = makeItemWithTooltip("phasic_capsule_white");
    public static final ItemDefinition PHASIC_CAPSULE_BLUE = makeItemWithTooltip("phasic_capsule_blue");
    public static final ItemDefinition PHASIC_CAPSULE_GRAY = makeItemWithTooltip("phasic_capsule_gray");
    public static final ItemDefinition PHASIC_CAPSULE_PALE = makeItemWithTooltip("phasic_capsule_pale");
    public static final ItemDefinition PHASIC_CAPSULE_SLIMY = makeItemWithTooltip("phasic_capsule_slimy");
    public static final ItemDefinition PHASIC_CAPSULE_FLORAL = makeItemWithTooltip("phasic_capsule_floral");

    public static final ItemDefinition CIRCUIT_QUANTIZED = makeItem("circuit_quantized");
    public static final ItemDefinition CIRCUIT_MAGNIFIED = makeItem("circuit_magnified");
    public static final ItemDefinition CIRCUIT_EQUALIZED = makeItem("circuit_equalized");
    public static final ItemDefinition PROCESSOR_QUANTIZED = makeItem("processor_quantized");
    public static final ItemDefinition PROCESSOR_MAGNIFIED = makeItem("processor_magnified");
    public static final ItemDefinition PROCESSOR_EQUALIZED = makeItem("processor_equalized");

    // Fake item that is hidden in JEI
    public static final ItemDefinition FAKE_DIMENSION_PLACEHOLDER = makeItem("fake_dimension_placeholder");

    // Basic Blocks
    public static final BlockDefinition RIFTSTONE = makeBasicBlock("riftstone", RIFTSTONE_BLOCK_PROPS);
    public static final BlockDefinition RIFTSTONE_STAIRS = makeCustomBlock("riftstone_stairs",
            () -> new StairBlock(RIFTSTONE.block()::defaultBlockState, RIFTSTONE_BLOCK_PROPS));
    public static final BlockDefinition RIFTSTONE_SLAB = makeCustomBlock("riftstone_slab",
            () -> new SlabBlock(RIFTSTONE_BLOCK_PROPS));

    public static final BlockDefinition SMOOTH_RIFTSTONE = makeBasicBlock("smooth_riftstone", RIFTSTONE_BLOCK_PROPS);
    public static final BlockDefinition SMOOTH_RIFTSTONE_STAIRS = makeCustomBlock("smooth_riftstone_stairs",
            () -> new StairBlock(SMOOTH_RIFTSTONE.block()::defaultBlockState, RIFTSTONE_BLOCK_PROPS));
    public static final BlockDefinition SMOOTH_RIFTSTONE_SLAB = makeCustomBlock("smooth_riftstone_slab",
            () -> new SlabBlock(RIFTSTONE_BLOCK_PROPS));

    public static final BlockDefinition RIFTSTONE_BRICK = makeBasicBlock("riftstone_brick", RIFTSTONE_BLOCK_PROPS);
    public static final BlockDefinition RIFTSTONE_BRICK_STAIRS = makeCustomBlock("riftstone_brick_stairs",
            () -> new StairBlock(RIFTSTONE_BRICK.block()::defaultBlockState, RIFTSTONE_BLOCK_PROPS));
    public static final BlockDefinition RIFTSTONE_BRICK_SLAB = makeCustomBlock("riftstone_brick_slab",
            () -> new SlabBlock(RIFTSTONE_BLOCK_PROPS));

    public static final BlockDefinition AEROCRYSTAL_BLOCK = makeBasicBlock("aerocrystal_block", Material.AMETHYST);
    public static final BlockDefinition BLAZING_AEROCRYSTAL_BLOCK = makeBasicBlock("blazing_aerocrystal_block",
            Material.AMETHYST);
    public static final BlockDefinition EXOTIC_AEROCRYSTAL_BLOCK = makeBasicBlock("exotic_aerocrystal_block",
            Material.AMETHYST);
    public static final BlockDefinition RIFT_SHARD_BLOCK = makeBasicBlock("rift_shard_block", Material.AMETHYST);
    public static final BlockDefinition RIFT_ALLOY_BLOCK = makeBasicBlock("rift_alloy_block", Material.METAL);

    public static final BlockDefinition UNSTABLE_RIFTSTONE = makeBasicBlock("unstable_riftstone",
            RIFTSTONE_BLOCK_PROPS);
    public static final BlockDefinition RIFT_SHARD_ORE = makeOreBlock("rift_shard_ore");
    public static final BlockDefinition NETHER_GLOWSTONE_ORE = makeOreBlock("nether_glowstone_ore");
    public static final BlockDefinition NETHER_DEBRIS_ORE = makeOreBlock("nether_debris_ore");
    public static final BlockDefinition END_AMETHYST_ORE = makeOreBlock("end_amethyst_ore");

    // Special items
    public static final ItemDefinition LUNCHBOX_CELL_ITEM = makeItem("lunchbox_cell", LunchboxCellItem::new);

    public static final ItemDefinition CAPTURE_PLANE_ITEM = makePart("capture_plane", CapturePlanePart.class,
            CapturePlanePart::new);

    public static final ItemDefinition PHASE_FIELD_EMITTER_ITEM = makePart("phase_field_emitter",
            PhaseFieldEmitterPart.class, PhaseFieldEmitterPart::new);

    public static final ItemDefinition SPATIAL_RIFT_CELL_2_ITEM = makeRiftCellItem("spatial_rift_cell_2",
            AEItems.SPATIAL_CELL2);
    public static final ItemDefinition SPATIAL_RIFT_CELL_16_ITEM = makeRiftCellItem("spatial_rift_cell_16",
            AEItems.SPATIAL_CELL16);
    public static final ItemDefinition SPATIAL_RIFT_CELL_128_ITEM = makeRiftCellItem("spatial_rift_cell_128",
            AEItems.SPATIAL_CELL128);

    // Custom recipe types
    //@formatter:off
    public static final RegistryObject<RecipeType<AtmosphericCrystallizerRecipe>> ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE = ImpenRegistry
            .makeRecipeType("atmospheric_crystallizer", AtmosphericCrystallizerRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeType<SpatialRiftSpawnerRecipe>> SPATIAL_RIFT_SPAWNER_RECIPE_TYPE = ImpenRegistry
            .makeRecipeType("spatial_rift_spawner", SpatialRiftSpawnerRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeType<SpatialRiftCollapserRecipe>> SPATIAL_RIFT_COLLAPSER_RECIPE_TYPE = ImpenRegistry
            .makeRecipeType("spatial_rift_collapser", SpatialRiftCollapserRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeType<SpatialRiftManipulatorBaseBlockRecipe>> SPATIAL_RIFT_MANIPULATOR_BASE_BLOCK_RECIPE_TYPE = ImpenRegistry
            .makeRecipeType("spatial_rift_manipulator_base", SpatialRiftManipulatorBaseBlockRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeType<SpatialRiftManipulatorBlockWeightRecipe>> SPATIAL_RIFT_MANIPULATOR_BLOCK_WEIGHT_RECIPE_TYPE = ImpenRegistry
            .makeRecipeType("spatial_rift_manipulator_weight", SpatialRiftManipulatorBlockWeightRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeType<SpatialRiftManipulatorCraftingRecipe>> SPATIAL_RIFT_MANIPULATOR_CRAFTING_RECIPE_TYPE = ImpenRegistry
            .makeRecipeType("spatial_rift_manipulator_crafting", SpatialRiftManipulatorCraftingRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeType<SpatialRiftManipulatorSpecialRecipe>> SPATIAL_RIFT_MANIPULATOR_SPECIAL_RECIPE_TYPE = ImpenRegistry
            .makeRecipeType("spatial_rift_manipulator_special", SpatialRiftManipulatorSpecialRecipe.Serializer.INSTANCE);
    //@formatter:on

    // Particles
    public static final RegistryObject<ParticleType<SimpleParticleType>> DISINTEGRATOR_DAMAGE_PARTICLE = PARTICLES
            .register("disintegrator_damage_particle", () -> DisintegratorDamageParticle.TYPE);
    public static final RegistryObject<ParticleType<SimpleParticleType>> DISINTEGRATOR_LOCK_PARTICLE = PARTICLES
            .register("disintegrator_lock_particle", () -> DisintegratorLockParticle.TYPE);

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
        return ImpenRegistry.makeMachine(name, blockSupplier, blockEntitySupplier, orientable,
                propsModifier.apply(MACHINE_BLOCK_PROPS));
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

    public static ItemDefinition makeItemWithTooltip(final String name) {
        return makeItem(name, StaticTooltipItem::new);
    }

    public static ItemDefinition makeItem(final String name, final Supplier<Item> sup) {
        final var def = new ItemDefinition(ITEMS.register(name, sup));
        ImpenRegistry.ITEMS_LIST.add(def);
        return def;
    }

    public static ItemDefinition makeRiftCellItem(final String name, final ItemLike originalItem) {
        return makeItem(name, () -> new SpatialRiftCellItem(getItemProps(), originalItem));
    }

    private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> makeRecipeType(final String key,
            final RecipeSerializer<T> serializer) {
        final RegistryObject<RecipeType<T>> recipeType = RECIPE_TYPES.register(key, () -> new RecipeType<T>() {
            @Override
            public String toString() {
                return key;
            }
        });
        RECIPE_SERIALIZERS.register(recipeType.getId().getPath(), () -> serializer);
        return recipeType;
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
        return ImpenRegistry.makeBasicBlock(name, BlockBehaviour.Properties.of(mat)
                .strength(1f, 2f));
    }

    private static BlockDefinition makeBasicBlock(final String name, final BlockBehaviour.Properties props) {
        return ImpenRegistry.makeCustomBlock(name, () -> new Block(props));
    }

    private static BlockDefinition makeOreBlock(final String name) {
        return ImpenRegistry.makeCustomBlock(name, () -> new OreBlock(ORE_BLOCK_PROPS));
    }

    private static Item.Properties getItemProps() {
        return new Item.Properties().tab(ImpenCreativeModeTab.TAB);
    }
    
    public static <T extends AEBasePart> ItemDefinition makePart(final String name, final Class<T> partClass, final Function<IPartItem<T>, T> partCtor) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return makeItem(name, () -> new PartItem<>(getItemProps(), partClass, partCtor));
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
