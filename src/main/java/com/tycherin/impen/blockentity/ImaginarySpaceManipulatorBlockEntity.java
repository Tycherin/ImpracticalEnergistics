package com.tycherin.impen.blockentity;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpracticalEnergisticsMod;

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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

public class ImaginarySpaceManipulatorBlockEntity extends AENetworkInvBlockEntity implements IGridTickable {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int TICKS_REQUIRED = 10 * 20; // TODO Replace with configuration

    private static class InventorySlots {
        public static final int INPUT = 0;
        public static final int PROCESSING = 1;
        public static final int OUTPUT = 2;
    }

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 3);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new InventoryItemFilter());
    private YesNo lastRedstoneState = YesNo.UNDECIDED;

    private final ILevelRunnable callback = level -> startOreify();

    private int progressTicks = -1;
    private int maxProgressTicks = -1;

    public ImaginarySpaceManipulatorBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpracticalEnergisticsMod.IMAGINARY_SPACE_MANIPULATOR_BE.get(), pos, blockState);

        this.getMainNode()
                .addService(IGridTickable.class, this)
                .setFlags();
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
        final ItemStack cell = this.inv.getStackInSlot(InventorySlots.INPUT);
        if (!this.isSpatialCell(cell)) {
            // Shouldn't be possible, but check just in case
            return;
        }
        if (!this.inv.getStackInSlot(InventorySlots.PROCESSING).isEmpty()
                || !this.inv.getStackInSlot(InventorySlots.OUTPUT).isEmpty()) {
            return;
        }
        if (!this.getMainNode().isActive()) {
            return;
        }

        final Optional<SpatialStoragePlot> plotOpt = this.getOrAllocatePlot(cell);
        if (plotOpt.isPresent()) {
            // Move the cell into the processing slot
            this.inv.setItemDirect(InventorySlots.INPUT, ItemStack.EMPTY);
            this.inv.setItemDirect(InventorySlots.PROCESSING, cell);

            this.maxProgressTicks = TICKS_REQUIRED;
            this.progressTicks = 0;

            this.getMainNode().ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        }
        else {
            // Unable to allocate plot, e.g. because of size mismatch
            // TODO It would be nice to signal this to the player somehow. Ideally we would show the failure on the UI
            // rather than just doing nothing.
            return;
        }
    }

    public void doOreify() {
        if (this.level.isClientSide) {
            return;
        }
        final ItemStack cell = this.inv.getStackInSlot(InventorySlots.PROCESSING);
        if (!this.isSpatialCell(cell)) {
            // Shouldn't be possible, but check just in case
            return;
        }
        if (!this.inv.getStackInSlot(InventorySlots.OUTPUT).isEmpty()) {
            // Shouldn't be possible, but check just in case
            return;
        }
        if (!this.getMainNode().isActive()) {
            return;
        }

        final Optional<SpatialStoragePlot> plotOpt = this.getOrAllocatePlot(cell);

        if (plotOpt.isPresent()) {
            final SpatialStoragePlot plot = plotOpt.get();

            // TODO Fancy block replacement logic (with gameplay mechanics!) goes here
            // TODO Do this setup once during the relevant registry population event
            // TODO Make this configurable - only replaceable ore types for the relevant blocks
            final TagKey<Block> oreTagKey = Tags.Blocks.ORES;
            final ITag<Block> oreTag = ForgeRegistries.BLOCKS.tags().getTag(oreTagKey);
            final Random random = new Random();

            final var spatialLevel = SpatialStoragePlotManager.INSTANCE.getLevel();
            final BlockPos startPos = plot.getOrigin();
            final BlockPos endPos = new BlockPos(
                    startPos.getX() + plot.getSize().getX() - 1,
                    startPos.getY() + plot.getSize().getY() - 1,
                    startPos.getZ() + plot.getSize().getZ() - 1);
            final AtomicInteger updateCount = new AtomicInteger();
            BlockPos.betweenClosedStream(startPos, endPos).forEach(pos -> {
                final BlockState bs = spatialLevel.getBlockState(pos);
                // Matrix frame blocks are used to fill the empty space in the allocated space, so we overwrite those
                if (bs.isAir() || bs.getBlock().equals(AEBlocks.MATRIX_FRAME.block())) {

                    final Block blockToPlace;
                    if (random.nextDouble() > .9) {
                        blockToPlace = oreTag.getRandomElement(random).orElse(Blocks.STONE);
                    }
                    else {
                        blockToPlace = Blocks.STONE;
                    }

                    spatialLevel.setBlock(pos, blockToPlace.defaultBlockState(), Block.UPDATE_NONE);
                    updateCount.incrementAndGet();
                }
            });
        }
        else {
            // This should be caught when the operation is started, so it's weird to have it here
            LOGGER.warn("Attempted to finish cell {}, but no plot was found",
                    ((SpatialStorageCellItem) cell.getItem()).getAllocatedPlotId(cell));
        }

        // Move the cell into the output slot
        this.inv.setItemDirect(InventorySlots.PROCESSING, ItemStack.EMPTY);
        this.inv.setItemDirect(InventorySlots.OUTPUT, cell);
    }

    private Optional<SpatialStoragePlot> getOrAllocatePlot(final ItemStack cell) {
        final var node = this.getMainNode();
        final var grid = node.getGrid();
        final var plotManager = SpatialStoragePlotManager.INSTANCE;
        final var spatialCell = (SpatialStorageCellItem) cell.getItem();

        final SpatialStoragePlot existingPlot = plotManager.getPlot(spatialCell.getAllocatedPlotId(cell));
        if (existingPlot != null) {
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

            final int spatialCellMaxDim = spatialCell.getMaxStoredDim(cell);
            final BlockPos size;
            if (grid.getSpatialService() != null && grid.getSpatialService().isValidRegion()) {
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
                    LOGGER.info("SCS has size {} but cell has size {}; aborting", spatialSvcSize, spatialCellMaxDim);
                    return Optional.empty();
                }

                size = spatialSvcSize;
            }
            else {
                LOGGER.info("No SCS found on grid, so unable to populate cell {}",
                        spatialCell.getAllocatedPlotId(cell));
                return Optional.empty();
            }

            LOGGER.info("Allocating new plot of size {} to cell {}", size, spatialCell.getAllocatedPlotId(cell));

            final SpatialStoragePlot newPlot = plotManager.allocatePlot(size, playerId);
            spatialCell.setStoredDimension(cell, newPlot.getId(), newPlot.getSize());
            return Optional.of(newPlot);
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
        return new TickingRequest(1, 1, !this.hasWork(), false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (!this.hasWork()) {
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
            this.markForUpdate();
        }
    }

    private boolean isSpatialCell(final ItemStack cell) {
        if (!cell.isEmpty() && cell.getItem() instanceof ISpatialStorageCell spatialCell) {
            return spatialCell.isSpatialStorage(cell);
        }
        return false;
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
    }

    public int getCurrentProgress() {
        return this.progressTicks;
    }

    public int getMaxProgress() {
        return this.maxProgressTicks;
    }

    public boolean hasWork() {
        return this.getMaxProgress() > 0;
    }

}
