package com.tycherin.impen.blockentity.rift;

import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.MachineBlockEntity;
import com.tycherin.impen.item.RiftedSpatialCellItem;
import com.tycherin.impen.util.FilteredInventoryWrapper;

import appeng.api.inventories.InternalInventory;
import appeng.core.definitions.AEItems;
import appeng.items.storage.SpatialStorageCellItem;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SpatialRiftStabilizerBlockEntity extends MachineBlockEntity {

    private static final int DEFAULT_SPEED_TICKS = 20 * 5; // 5s
    
    private final FilteredInventoryWrapper invWrapper = new FilteredInventoryWrapper(this, new InventoryItemFilter());
    private final MachineOperation op;
    
    public SpatialRiftStabilizerBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        super(ImpenRegistry.SPATIAL_RIFT_STABILIZER, blockPos, blockState);
        this.op = new MachineOperation(
                DEFAULT_SPEED_TICKS,
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
        final int plotId = ((RiftedSpatialCellItem)input.getItem()).getPlotId(input);

        // TODO Need to store size/cell type
        
        final ItemStack output = AEItems.SPATIAL_CELL2.stack();
        final SpatialStoragePlot plot = SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        ((SpatialStorageCellItem)AEItems.SPATIAL_CELL2.asItem()).setStoredDimension(output, plotId, plot.getSize());
        
        // TODO Actually mutate blocks
        
        this.invWrapper.getInput().setItemDirect(0, ItemStack.EMPTY);
        this.invWrapper.getOutput().setItemDirect(0, output);

        return true;
    }

    @Override
    protected int progressOperation() {
        // TODO Power draw
        return 1;
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
            return slot == 0 && stack.getItem().equals(ImpenRegistry.RIFTED_SPATIAL_CELL_ITEM.asItem());
        }
    }
}
