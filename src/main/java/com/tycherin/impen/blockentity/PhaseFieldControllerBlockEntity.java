package com.tycherin.impen.blockentity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.part.PhaseFieldEmitterPart;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.me.helpers.MachineSource;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class PhaseFieldControllerBlockEntity extends AENetworkBlockEntity
        implements IGridTickable {

    private static final int TICKS_PER_OPERATION = 20;

    private static final Ingredient USABLE_CAPSULES = Ingredient.of(
            ImpenRegistry.DISINTEGRATOR_CAPSULE_EGG,
            ImpenRegistry.DISINTEGRATOR_CAPSULE_LOOT,
            ImpenRegistry.DISINTEGRATOR_CAPSULE_LUCK,
            ImpenRegistry.DISINTEGRATOR_CAPSULE_PLAYER_KILL);

    @Getter
    private final InternalInventory capsuleConfigInv = new CapsuleConfigInventory(3);
    private Set<PhaseFieldEmitterPart> emitters = Collections.emptySet();
    private int tickCount = TICKS_PER_OPERATION;

    public PhaseFieldControllerBlockEntity(final BlockPos pos,
            final BlockState blockState) {
        super(ImpenRegistry.PHASE_FIELD_CONTROLLER.blockEntity(), pos, blockState);

        this.getMainNode()
                .setExposedOnSides(EnumSet.noneOf(Direction.class))
                .addService(IGridTickable.class, this)
                .setFlags();
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(1, 1, !this.shouldBeActive(), false);
    }

    private boolean shouldBeActive() {
        return !this.emitters.isEmpty();
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        // TODO This might should be up, depending on how the model turns out
        this.getMainNode().setExposedOnSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getMainNode().setExposedOnSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (!this.shouldBeActive()) {
            return TickRateModulation.SLEEP;
        }

        this.tickCount--;
        if (this.tickCount <= 0) {
            this.tickCount = TICKS_PER_OPERATION;
            this.doOperation();
        }

        return TickRateModulation.SAME;
    }

    private void doOperation() {
        final Set<Mob> affectedEntities = new HashSet<>();
        this.emitters.forEach(emitter -> {
            final BlockPos affectedPos = emitter.getBlockEntity().getBlockPos().relative(emitter.getSide());
            // TODO Consider caching the AABBs here, since they should change relatively infrequently
            affectedEntities.addAll(this.level.getEntitiesOfClass(Mob.class, new AABB(affectedPos)));
        });

        // Temporary code
        // TODO Replace with proper capsule configuration
        // TODO Replace with proper inventory management
        final MEStorage storage = this.getGridNode().getGrid().getStorageService().getInventory();
        final AtomicBoolean gotIngredients = new AtomicBoolean();
        gotIngredients.set(true);
        capsuleConfigInv.forEach(configuredInput -> {
            if (configuredInput.isEmpty()) {
                return;
            }

            if (storage.extract(AEItemKey.of(configuredInput), 1, Actionable.SIMULATE, new MachineSource(this)) != 1) {
                gotIngredients.set(false);
            }
        });

        if (gotIngredients.get()) {
            affectedEntities.forEach(Mob::kill);
        }
    }

    public void setEmitters(final Set<PhaseFieldEmitterPart> emitters) {
        if (!emitters.isEmpty()) {
            // Wake the node up if it was asleep
            this.getMainNode().ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        }
        this.emitters = emitters;
    }

    public static final class CapsuleConfigInventory implements InternalInventory {

        private final ItemStack[] items;

        public CapsuleConfigInventory(final int size) {
            this.items = new ItemStack[size];
            for (int i = 0; i < this.items.length; i++) {
                this.items[i] = ItemStack.EMPTY;
            }
        }

        @Override
        public int size() {
            return items.length;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(final int slotIndex) {
            return items[slotIndex];
        }

        @Override
        public boolean isItemValid(final int slotIndex, final ItemStack stack) {
            return CapsuleConfigInventory.isItemValid(stack);
        }

        @Override
        public void setItemDirect(final int slotIndex, final ItemStack stack) {
            if (isItemValid(slotIndex, stack)) {
                this.items[slotIndex] = stack;
            }
            else {
                throw new IllegalArgumentException("Item " + stack + " is not allowed");
            }
        }

        public static boolean isItemValid(final ItemStack stack) {
            return stack.isEmpty() || USABLE_CAPSULES.test(stack);
        }
    }
}
