package com.tycherin.impen.blockentity.rift;

import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.MachineBlockEntity;
import com.tycherin.impen.logic.SpatialRiftManipulatorLogic;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SpatialRiftManipulatorBlockEntity extends MachineBlockEntity {

    private static final int DEFAULT_SPEED_TICKS = 20 * 1; // 1s

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 3);
    private final InternalInventory invExt = new FilteredInternalInventory(inv, new InventoryItemFilter());
    private final MachineOperation op = new MachineOperation(
            DEFAULT_SPEED_TICKS,
            this::enableOperation,
            this::doOperation);
    private final SpatialRiftManipulatorLogic logic = new SpatialRiftManipulatorLogic();

    public SpatialRiftManipulatorBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR, pos, blockState);
    }

    @Override
    protected Optional<MachineOperation> getOperation() {
        if (!this.enableOperation()) {
            return Optional.empty();
        }
        else {
            return Optional.of(op);
        }
    }

    private static class Slots {
        public static final int TOP = 0;
        public static final int BOTTOM = 1;
        public static final int OUTPUT = 2;
    }

    private boolean enableOperation() {
        return !this.inv.getStackInSlot(Slots.TOP).isEmpty()
                && !this.inv.getStackInSlot(Slots.BOTTOM).isEmpty()
                && this.inv.getStackInSlot(Slots.OUTPUT).isEmpty();
    }

    protected boolean doOperation() {
        final ItemStack topInput = this.inv.getStackInSlot(Slots.TOP);
        final ItemStack bottomInput = this.inv.getStackInSlot(Slots.BOTTOM);

        final ItemStack output = logic.processInputs(topInput, bottomInput);

        if (output.isEmpty()) {
            // TODO Ideally this should put the machine to sleep or something, since otherwise it'll keep retrying
            // the operation over and over again with no chance of success
            return false;
        }
        else {
            this.inv.setItemDirect(Slots.TOP, ItemStack.EMPTY);
            this.inv.setItemDirect(Slots.BOTTOM, ItemStack.EMPTY);
            this.inv.setItemDirect(Slots.OUTPUT, output);
            return true;
        }
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(final Direction side) {
        return this.invExt;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    private class InventoryItemFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(final InternalInventory inv, final int slot, final int amount) {
            return slot == Slots.OUTPUT;
        }

        @Override
        public boolean allowInsert(final InternalInventory inv, final int slot, final ItemStack stack) {
            if (slot == Slots.TOP) {
                return logic.isValidInput(stack, inv.getStackInSlot(Slots.BOTTOM));
            }
            else if (slot == Slots.BOTTOM) {
                return logic.isValidInput(inv.getStackInSlot(Slots.TOP), stack);
            }
            else {
                return false;
            }
        }
    }

    @Override
    protected int progressOperation() {
        return 1;
    }
}
