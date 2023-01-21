package com.tycherin.impen.blockentity.rift;

import java.util.Optional;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.MachineBlockEntity;
import com.tycherin.impen.config.ImpenConfig;
import com.tycherin.impen.item.SpatialRiftCellItem;
import com.tycherin.impen.logic.SpatialRiftCellDataManager;
import com.tycherin.impen.logic.SpatialRiftCellDataManager.SpatialRiftCellData;
import com.tycherin.impen.logic.SpatialRiftCollapserLogic;
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

public class SpatialRiftCollapserBlockEntity extends MachineBlockEntity {

    private static final Logger LOGGER = LogUtils.getLogger();

    // TODO Change the time required for an operation based on the size of the plot
    private static final int DEFAULT_SPEED_TICKS = 20 * 5; // 5s

    private final FilteredInventoryWrapper invWrapper;
    private final MachineOperation op;
    private final SpatialRiftCollapserLogic logic;
    private final IAEItemFilter filter;
    private final double powerPerTick;

    public SpatialRiftCollapserBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        super(ImpenRegistry.SPATIAL_RIFT_COLLAPSER, blockPos, blockState);
        this.op = new MachineOperation(
                DEFAULT_SPEED_TICKS,
                this::enableOperation,
                this::doOperation);
        this.logic = new SpatialRiftCollapserLogic();
        this.filter = new InventoryItemFilter();
        this.invWrapper = new FilteredInventoryWrapper(this, this.filter);
        this.powerPerTick = ImpenConfig.POWER.spatialRiftCollapserCost();
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
        // TODO Switch this to progress the operation gradually rather than doing it all at once
        // (mainly to reduce performance impact)

        // TODO Handle inputs other than rift cells

        final ItemStack input = this.invWrapper.getInput().getStackInSlot(0);
        final int plotId = ((SpatialRiftCellItem)input.getItem()).getPlotId(input);
        final Optional<SpatialRiftCellData> dataOpt = SpatialRiftCellDataManager.INSTANCE.getDataForPlot(plotId);
        if (dataOpt.isEmpty()) {
            // TODO Ideally this should put the machine to sleep or something, since otherwise it'll keep retrying the
            // operation over and over again with no chance of success
            LOGGER.warn("Rift cell data not found for {}", plotId);
            return false;
        }
        final SpatialRiftCellData data = dataOpt.get();

        final ItemStack output = ((SpatialRiftCellItem)(input.getItem())).getOriginalItem().asItem()
                .getDefaultInstance();
        final SpatialStoragePlot plot = SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        ((SpatialStorageCellItem)AEItems.SPATIAL_CELL2.asItem()).setStoredDimension(output, plotId, plot.getSize());

        logic.addBlocksToPlot(plot, data);

        this.invWrapper.getInput().setItemDirect(0, ItemStack.EMPTY);
        this.invWrapper.getOutput().setItemDirect(0, output);

        return true;
    }

    @Override
    protected int progressOperation() {
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

    public int getMaxProgress() {
        return DEFAULT_SPEED_TICKS;
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
            return slot == 0
                    && inv.getStackInSlot(0).isEmpty()
                    && stack.getItem() instanceof SpatialRiftCellItem;
        }
    }

    @Override
    public IAEItemFilter getInventoryFilter() {
        return this.filter;
    }

    @Override
    protected double getPowerDraw() {
        return this.powerPerTick;
    }
}
