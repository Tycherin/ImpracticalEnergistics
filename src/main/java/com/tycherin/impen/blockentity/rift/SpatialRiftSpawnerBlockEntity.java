package com.tycherin.impen.blockentity.rift;

import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.MachineBlockEntity;
import com.tycherin.impen.item.RiftedSpatialCellItem;
import com.tycherin.impen.logic.RiftCellDataManager;
import com.tycherin.impen.logic.RiftCellDataManager.RiftCellData;
import com.tycherin.impen.util.FilteredInventoryWrapper;
import com.tycherin.impen.util.SpatialRiftUtil;

import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.inventories.InternalInventory;
import appeng.items.storage.SpatialStorageCellItem;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SpatialRiftSpawnerBlockEntity extends MachineBlockEntity {

    private static final int DEFAULT_SPEED_TICKS = 20 * 5; // 5s

    private final FilteredInventoryWrapper invWrapper = new FilteredInventoryWrapper(this, new InventoryItemFilter());
    private final int baseSpeedTicks;
    private final MachineOperation op;

    public SpatialRiftSpawnerBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.SPATIAL_RIFT_SPAWNER, pos, blockState);
        this.baseSpeedTicks = DEFAULT_SPEED_TICKS;
        this.op = new MachineOperation(
                this.baseSpeedTicks,
                this::enableOperation,
                this::doOperation);
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

    private boolean enableOperation() {
        return !this.invWrapper.getInput().isEmpty() && this.invWrapper.getOutput().isEmpty();
    }

    protected boolean doOperation() {
        final ItemStack input = this.invWrapper.getInput().getStackInSlot(0);
        final ItemStack output = this.getOutputForItem(input);

        this.invWrapper.getInput().setItemDirect(0, ItemStack.EMPTY);
        this.invWrapper.getOutput().setItemDirect(0, output);

        return true;
    }

    private ItemStack getOutputForItem(final ItemStack input) {
        final var spatialCellOpt = SpatialRiftUtil.getSpatialCell(input);
        if (spatialCellOpt.isPresent()) {
            return getOutputForSpatialCell(input, spatialCellOpt.get());
        }
        else {
            throw new RuntimeException("Doesn't yet support non-spatial cell items");
        }
    }

    private ItemStack getOutputForSpatialCell(final ItemStack inputItem, final ISpatialStorageCell cell) {
        final int plotId = cell.getAllocatedPlotId(inputItem);
        if (plotId == -1) {
            // No plot allocated. This shouldn't happen normally because of the input filter, but in case it does, we
            // fall back on returning the cell as-is as a safety measure.
            return inputItem;
        }

        final ItemStack is = new ItemStack(
                RiftedSpatialCellItem.getMatchingCell((SpatialStorageCellItem)inputItem.getItem()));
        ((RiftedSpatialCellItem) is.getItem()).setPlotId(is, plotId);
        RiftCellDataManager.INSTANCE.putDataForPlot(new RiftCellData(plotId));
        
        return is;
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(final Direction side) {
        return this.invWrapper.getExternal();
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.invWrapper.getInternal();
    }

    // TODO Ideally this shouldn't hardcode slot numbers & should integrate with FilteredInventoryWrapper instead...
    // somehow
    private static class InventoryItemFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(final InternalInventory inv, final int slot, final int amount) {
            return slot == 1;
        }

        @Override
        public boolean allowInsert(final InternalInventory inv, final int slot, final ItemStack stack) {
            // Only allow spatial cells that have allocated plots already
            return slot == 0
                    && SpatialRiftUtil.getPlotId(stack).isPresent();
        }
    }

    @Override
    protected int progressOperation() {
        return 1;
    }
}
