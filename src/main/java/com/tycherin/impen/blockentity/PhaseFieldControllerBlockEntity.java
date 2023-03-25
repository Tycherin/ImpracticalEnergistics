package com.tycherin.impen.blockentity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.logic.phase.PhaseFieldLogic;
import com.tycherin.impen.part.PhaseFieldEmitterPart;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class PhaseFieldControllerBlockEntity extends AENetworkInvBlockEntity
        implements IGridTickable {

    private static final int TICKS_PER_OPERATION = 20;

    private static final Ingredient USABLE_CAPSULES = Ingredient.of(
            ImpenRegistry.PHASIC_CAPSULE_RED,
            ImpenRegistry.PHASIC_CAPSULE_ORANGE,
            ImpenRegistry.PHASIC_CAPSULE_YELLOW,
            ImpenRegistry.PHASIC_CAPSULE_LIME,
            ImpenRegistry.PHASIC_CAPSULE_GREEN,
            ImpenRegistry.PHASIC_CAPSULE_WHITE,
            ImpenRegistry.PHASIC_CAPSULE_BLUE,
            ImpenRegistry.PHASIC_CAPSULE_GRAY,
            ImpenRegistry.PHASIC_CAPSULE_PALE,
            ImpenRegistry.PHASIC_CAPSULE_SLIMY,
            ImpenRegistry.PHASIC_CAPSULE_FLORAL);

    private final PhaseFieldLogic logic = new PhaseFieldLogic(this);
    private final InternalInventory capsuleConfigInv = new CapsuleConfigInventory(this, 3);
    @Getter
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
        return new TickingRequest(1, 20, !this.shouldBeActive(), false);
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
        this.logic.recomputeOperation(); // Need to call this to set up initial state
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (!this.shouldBeActive()) {
            return TickRateModulation.SLEEP;
        }

        if (this.tickCount > 0) {
            this.tickCount -= ticksSinceLastCall;
        }

        if (this.tickCount <= 0) {
            final var operationSuccessful = this.logic.doOperation();
            if (operationSuccessful) {
                // Operation succeeded; reset the operation timer
                this.tickCount = TICKS_PER_OPERATION;
                return TickRateModulation.FASTER;
            }
            else {
                // Operation failed for some reason; slow down tick rate to avoid spamming an unsuccessful operation
                return TickRateModulation.SLOWER;
            }
        }
        else {
            return TickRateModulation.FASTER;
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
        this.tickCount = TICKS_PER_OPERATION;
        this.logic.recomputeAABBCache();
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.capsuleConfigInv;
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot) {
        this.logic.recomputeOperation();
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(final Direction side) {
        // The internal config inventory should only be interacted with by the menu, so expose an empty inventory to
        // everyone else
        return EmptyInventory.INSTANCE;
    }

    @Override
    public void addAdditionalDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        // Override the superclass behavior, which is to drop items in the internal inventory
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            // Don't expose this capability to anyone, since it isn't a real inventory
            return LazyOptional.empty();
        }
        else {
            return super.getCapability(capability, facing);
        }
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) {
        super.writeToStream(data);

        for (int i = 0; i < this.capsuleConfigInv.size(); i++) {
            data.writeItem(capsuleConfigInv.getStackInSlot(i));
        }
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        var c = super.readFromStream(data);

        for (int i = 0; i < this.capsuleConfigInv.size(); i++) {
            this.capsuleConfigInv.setItemDirect(i, data.readItem());
        }

        return c;
    }

    public static final class CapsuleConfigInventory implements InternalInventory {

        private final PhaseFieldControllerBlockEntity host;
        private final ItemStack[] items;

        public CapsuleConfigInventory(final PhaseFieldControllerBlockEntity host, final int size) {
            this.host = host;
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
                this.host.onChangeInventory(this, slotIndex);
                this.host.saveChanges();
            }
            else {
                throw new IllegalArgumentException("Item " + stack + " is not allowed");
            }
        }

        public static boolean isItemValid(final ItemStack stack) {
            return stack.isEmpty() || USABLE_CAPSULES.test(stack);
        }
    }

    // The AE2 version of this class isn't visible for reasons, so here's this instead
    private static class EmptyInventory implements InternalInventory {
        private static final EmptyInventory INSTANCE = new EmptyInventory();

        private EmptyInventory() {
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public ItemStack getStackInSlot(final int slotIndex) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItemDirect(final int slotIndex, final ItemStack stack) {
            // Ignored
        }

        @Override
        public Iterator<ItemStack> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
            return ItemStack.EMPTY;
        }
    }
}
