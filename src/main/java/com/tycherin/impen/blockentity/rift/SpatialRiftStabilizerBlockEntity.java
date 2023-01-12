package com.tycherin.impen.blockentity.rift;

import java.util.Optional;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.MachineBlockEntity;
import com.tycherin.impen.item.RiftedSpatialCellItem;
import com.tycherin.impen.logic.SpatialRiftStabilizerLogic;
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

    private static final Logger LOGGER = LogUtils.getLogger();
    
    // TODO Change the time required for an operation based on the size of the plot
    private static final int DEFAULT_SPEED_TICKS = 20 * 5; // 5s
    
    private final FilteredInventoryWrapper invWrapper = new FilteredInventoryWrapper(this, new InventoryItemFilter());
    private final MachineOperation op;
    private final SpatialRiftStabilizerLogic logic;
    
    public SpatialRiftStabilizerBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        super(ImpenRegistry.SPATIAL_RIFT_STABILIZER, blockPos, blockState);
        this.op = new MachineOperation(
                DEFAULT_SPEED_TICKS,
                this::enableOperation,
                this::doOperation);
        this.logic = new SpatialRiftStabilizerLogic(this);
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
        final int plotId = ((RiftedSpatialCellItem) input.getItem()).getPlotId(input);

        final int cellSize = ((RiftedSpatialCellItem) input.getItem()).getOriginalCellSize(input);
        final ItemStack output = switch (cellSize) {
        case 2 -> AEItems.SPATIAL_CELL2.stack();
        case 16 -> AEItems.SPATIAL_CELL16.stack();
        case 128 -> AEItems.SPATIAL_CELL128.stack();
        default -> throw new RuntimeException("Unrecognized cell size: " + cellSize);
        };
        final SpatialStoragePlot plot = SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        ((SpatialStorageCellItem)AEItems.SPATIAL_CELL2.asItem()).setStoredDimension(output, plotId, plot.getSize());
        
        final var ingredientMap = ((RiftedSpatialCellItem) input.getItem()).getIngredients(input);
        LOGGER.info("Found ingredients: {}", ingredientMap);
        
        // TODO Actually mutate blocks
        
        if (logic.addBlocksToPlot(plot, ingredientMap)) {
            this.invWrapper.getInput().setItemDirect(0, ItemStack.EMPTY);
            this.invWrapper.getOutput().setItemDirect(0, output);

            return true;
        }
        else {
            // TODO What would cause this?
            return false;
        }
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
