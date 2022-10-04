package com.tycherin.impen.blockentity;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.logic.ism.IsmService;
import com.tycherin.impen.logic.ism.IsmStatusCodes;

import appeng.api.config.YesNo;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.spatial.ISpatialService;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.hooks.ticking.TickHandler;
import appeng.items.storage.SpatialStorageCellItem;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import appeng.util.ILevelRunnable;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ImaginarySpaceManipulatorBlockEntity extends AENetworkInvBlockEntity implements IGridTickable {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int TICKS_REQUIRED = 10 * 20; // TODO Replace with configuration

    private static class InventorySlots {
        public static final int INPUT = 0;
        public static final int OUTPUT = 1;
    }

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 2);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new InventoryItemFilter());
    private YesNo lastRedstoneState = YesNo.UNDECIDED;

    private final ILevelRunnable callback = level -> startOreify();

    private int progressTicks = -1;
    private int maxProgressTicks = -1;

    private int statusCode = IsmStatusCodes.IDLE;

    public ImaginarySpaceManipulatorBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpracticalEnergisticsMod.IMAGINARY_SPACE_MANIPULATOR_BE.get(), pos, blockState);

        this.getMainNode()
                .addService(IGridTickable.class, this)
                .setFlags();

        this.updateStatus();
    }

    // TODO Power requirements

    // ***
    // Ore generation methods
    // ***

    public void triggerOreify() {
        if (!this.isClientSide()) {
            final ItemStack cell = this.inv.getStackInSlot(0);
            if (this.isSpatialCell(cell)) {
                // this needs to be cross world synced.
                TickHandler.instance().addCallable(null, callback);
            }
        }
    }

    public void startOreify() {
        if (this.level.isClientSide) {
            return;
        }

        this.updateStatus();

        if (this.statusCode == IsmStatusCodes.READY) {
            this.maxProgressTicks = TICKS_REQUIRED;
            this.progressTicks = 0;
            this.statusCode = IsmStatusCodes.RUNNING;

            this.getMainNode().ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        }
    }

    public void doOreify() {
        if (this.level.isClientSide) {
            return;
        }

        boolean errorFlag = false;

        if (this.statusCode != IsmStatusCodes.RUNNING) {
            errorFlag = true;
        }

        final ItemStack cell = this.inv.getStackInSlot(InventorySlots.INPUT);

        if (!this.isSpatialCell(cell)) {
            errorFlag = true;
        }
        else if (!this.inv.getStackInSlot(InventorySlots.OUTPUT).isEmpty()) {
            // Shouldn't be possible, but check just in case
            errorFlag = true;
        }
        else if (!this.getMainNode().isActive()) {
            errorFlag = true;
        }
        else {
            final Optional<SpatialStoragePlot> plotOpt = this.getPlot(cell, true);
            if (plotOpt.isPresent()) {
                final SpatialStoragePlot plot = plotOpt.get();

                final Supplier<Block> blockSupplier = this.getIsmService().getBlockSupplier();

                final var spatialLevel = SpatialStoragePlotManager.INSTANCE.getLevel();
                final BlockPos startPos = plot.getOrigin();
                final BlockPos endPos = new BlockPos(
                        startPos.getX() + plot.getSize().getX() - 1,
                        startPos.getY() + plot.getSize().getY() - 1,
                        startPos.getZ() + plot.getSize().getZ() - 1);
                final AtomicInteger updateCount = new AtomicInteger();
                BlockPos.betweenClosedStream(startPos, endPos).forEach(pos -> {
                    final BlockState bs = spatialLevel.getBlockState(pos);
                    // Matrix frame blocks are used to fill the empty space in the allocated space, so we overwrite
                    // those
                    if (bs.isAir() || bs.getBlock().equals(AEBlocks.MATRIX_FRAME.block())) {
                        spatialLevel.setBlock(pos, blockSupplier.get().defaultBlockState(), Block.UPDATE_NONE);
                        updateCount.incrementAndGet();
                    }
                });
            }
            else {
                // This should be caught when the operation is started, so it's weird to have it here
                LOGGER.warn("Attempted to finish cell {}, but no plot was found",
                        ((SpatialStorageCellItem) cell.getItem()).getAllocatedPlotId(cell));
            }
        }

        if (errorFlag) {
            // Something weird happened - call for an update and hope that displays an appropriate error
            this.updateStatus();
        }
        else {
            // Operation completed, so move the input to the output
            this.inv.setItemDirect(InventorySlots.INPUT, ItemStack.EMPTY);
            this.inv.setItemDirect(InventorySlots.OUTPUT, cell);
        }
    }

    private Optional<SpatialStoragePlot> getPlot(final ItemStack cell, final boolean allocateIfNotFound) {
        if (cell == null) {
            return Optional.empty();
        }

        final var node = this.getMainNode();
        final var grid = node.getGrid();
        final var plotManager = SpatialStoragePlotManager.INSTANCE;
        final var spatialCell = (SpatialStorageCellItem) cell.getItem();

        final SpatialStoragePlot existingPlot = plotManager.getPlot(spatialCell.getAllocatedPlotId(cell));
        if (existingPlot != null) {
            if (!allocateIfNotFound) {
                this.statusCode = IsmStatusCodes.READY;
            }
            return Optional.of(existingPlot);
        }
        else {
            final int playerId;
            if (grid.getSecurityService().isAvailable()) {
                playerId = grid.getSecurityService().getOwner();
            }
            else {
                playerId = node.getNode().getOwningPlayerId();
            }

            // No plot exists, so we need to create one. In order to figure out how large to make it, we use the size of
            // the network's SCS, or the max holding capacity of the cell, whichever is smaller

            // TODO Probably ditch the allocation stuff, it feels like more trouble than it's worth
            // Forcing a human to initialize each cell avoids a whole slew of problems, not the least of which is
            // unbounded allocation of space

            final BlockPos size;
            if (grid.getSpatialService() != null && grid.getSpatialService().isValidRegion()) {
                final int spatialCellMaxDim = spatialCell.getMaxStoredDim(cell);
                final ISpatialService spatialSvc = grid.getSpatialService();
                final BlockPos spatialSvcSize = new BlockPos(
                        spatialSvc.getMax().getX() - spatialSvc.getMin().getX() - 1,
                        spatialSvc.getMax().getY() - spatialSvc.getMin().getY() - 1,
                        spatialSvc.getMax().getZ() - spatialSvc.getMin().getZ() - 1);

                if (spatialSvcSize.getX() > spatialCellMaxDim
                        || spatialSvcSize.getY() > spatialCellMaxDim
                        || spatialSvcSize.getZ() > spatialCellMaxDim) {
                    // The SCS is bigger than the cell can hold, meaning you can't swap this cell in this SCS. That's
                    // bad and unintuitive, so we just reject the action here.
                    this.statusCode = IsmStatusCodes.SIZE_MISMATCH;
                    return Optional.empty();
                }

                size = spatialSvcSize;
            }
            else {
                this.statusCode = IsmStatusCodes.MISSING_SCS;
                return Optional.empty();
            }

            if (allocateIfNotFound) {
                final SpatialStoragePlot newPlot = plotManager.allocatePlot(size, playerId);
                spatialCell.setStoredDimension(cell, newPlot.getId(), newPlot.getSize());
                return Optional.of(newPlot);
            }
            else {
                this.statusCode = IsmStatusCodes.READY;
                return Optional.empty();
            }
        }
    }

    private class InventoryItemFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(final InternalInventory inv, final int slot, final int amount) {
            return slot == InventorySlots.OUTPUT;
        }

        @Override
        public boolean allowInsert(final InternalInventory inv, final int slot, final ItemStack stack) {
            return slot == InventorySlots.INPUT && ImaginarySpaceManipulatorBlockEntity.this.isSpatialCell(stack);
        }
    }

    // ***
    // Ticking methods
    // ***

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(1, 1, !this.isRunning(), false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (!this.isRunning()) {
            return TickRateModulation.SLEEP;
        }

        this.progressTicks++;

        if (this.progressTicks >= this.maxProgressTicks) {
            this.doOreify();
            this.progressTicks = -1;
            this.maxProgressTicks = -1;
            return TickRateModulation.SLEEP;
        }
        else {
            return TickRateModulation.SAME;
        }
    }

    public void updateStatus() {
        final int oldStatusCode = this.statusCode;
        if (!this.getMainNode().isActive()) {
            this.statusCode = IsmStatusCodes.MISSING_CHANNEL;
        }
        else if (this.inv.getStackInSlot(InventorySlots.INPUT).isEmpty()) {
            this.statusCode = IsmStatusCodes.IDLE;
        }
        else if (!this.inv.getStackInSlot(InventorySlots.OUTPUT).isEmpty()) {
            this.statusCode = IsmStatusCodes.OUTPUT_FULL;
        }
        else {
            // getPlot() will set a status code based on the state of the plot, so we don't need to do anything here
            this.getPlot(this.inv.getStackInSlot(InventorySlots.INPUT), false);
        }

        if (oldStatusCode != this.statusCode) {
            if (oldStatusCode == IsmStatusCodes.RUNNING) {
                // Something has gone wrong while the operation was running, so cancel it out
                this.progressTicks = -1;
                this.maxProgressTicks = -1;
            }

            this.getMainNode().ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        }
    }

    // ***
    // Boilerplate stuff
    // ***

    @Override
    public void saveAdditional(final CompoundTag data) {
        super.saveAdditional(data);
        data.putInt("lastRedstoneState", this.lastRedstoneState.ordinal());
    }

    @Override
    public void loadTag(final CompoundTag data) {
        super.loadTag(data);
        if (data.contains("lastRedstoneState")) {
            this.lastRedstoneState = YesNo.values()[data.getInt("lastRedstoneState")];
        }
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeInt(progressTicks);

        for (int i = 0; i < this.inv.size(); i++) {
            data.writeItem(inv.getStackInSlot(i));
        }
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);

        final int prevProgress = this.progressTicks;
        this.progressTicks = data.readInt();
        ret |= (prevProgress != this.progressTicks);

        if (prevProgress == -1 && this.progressTicks != -1) {
            this.maxProgressTicks = TICKS_REQUIRED;
        }

        for (int i = 0; i < this.inv.size(); i++) {
            this.inv.setItemDirect(i, data.readItem());
        }

        return ret;
    }

    public boolean getRedstoneState() {
        if (this.lastRedstoneState == YesNo.UNDECIDED) {
            this.updateRedstoneState();
        }

        return this.lastRedstoneState == YesNo.YES;
    }

    public void updateRedstoneState() {
        final YesNo currentState = this.level.getBestNeighborSignal(this.worldPosition) != 0 ? YesNo.YES : YesNo.NO;
        if (this.lastRedstoneState != currentState) {
            this.lastRedstoneState = currentState;
            if (this.lastRedstoneState == YesNo.YES) {
                this.triggerOreify();
            }
        }
    }

    @Override
    public void onMainNodeStateChanged(final IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.updateStatus();
            this.markForUpdate();
        }
    }

    private boolean isSpatialCell(final ItemStack cell) {
        if (!cell.isEmpty() && cell.getItem() instanceof ISpatialStorageCell spatialCell) {
            return spatialCell.isSpatialStorage(cell);
        }
        else {
            return false;
        }
    }

    @Override
    public AECableType getCableConnectionType(final Direction dir) {
        return AECableType.SMART;
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(final Direction side) {
        return this.invExt;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot) {
        this.updateStatus();
    }

    public int getCurrentProgress() {
        return this.progressTicks;
    }

    public int getMaxProgress() {
        return this.maxProgressTicks;
    }

    public boolean isRunning() {
        return this.statusCode == IsmStatusCodes.RUNNING;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
    
    private IsmService getIsmService() {
        return  ((IsmService) (this.getMainNode().getGrid().getService(IsmService.class)));
    }
}
