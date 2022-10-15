package com.tycherin.impen.blockentity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.config.ImpenConfig;
import com.tycherin.impen.logic.ism.IsmService;
import com.tycherin.impen.logic.ism.IsmStatusCodes;
import com.tycherin.impen.logic.ism.IsmWeightTracker.IsmWeightWrapper;
import com.tycherin.impen.util.AEPowerUtil;

import appeng.api.config.YesNo;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ImaginarySpaceManipulatorBlockEntity extends AENetworkInvBlockEntity implements IGridTickable {

    private static final int BLOCKS_PER_CYCLE = 64;

    private static class InventorySlots {
        public static final int INPUT = 0;
        public static final int OUTPUT = 1;
    }

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 2);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new InventoryItemFilter());
    private final AppEngInternalInventory catalystInv = new AppEngInternalInventory(this, IsmService.MAX_CATALYSTS);
    private final ILevelRunnable callback = level -> startOperation();

    private Optional<IsmWeightWrapper> activeWeights = Optional.empty();
    private int progressTicks = -1;
    private int maxProgressTicks = -1;
    private YesNo lastRedstoneState = YesNo.UNDECIDED;
    private int statusCode = IsmStatusCodes.IDLE;
    private double basePowerDraw;

    public ImaginarySpaceManipulatorBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpracticalEnergisticsMod.IMAGINARY_SPACE_MANIPULATOR_BE.get(), pos, blockState);

        this.getMainNode()
                .addService(IGridTickable.class, this)
                .setFlags();

        this.basePowerDraw = ImpenConfig.POWER.imaginarySpaceManipulatorCost();
        this.updateStatus();
    }

    // ***
    // Ore generation methods
    // ***

    public void triggerOreify() {
        if (!this.isClientSide()) {
            if (this.isRunning()) {
                return;
            }

            final ItemStack cell = this.inv.getStackInSlot(0);
            if (this.isSpatialCell(cell)) {
                // this needs to be cross world synced.
                TickHandler.instance().addCallable(null, callback);
            }
        }
    }

    public void startOperation() {
        if (this.level.isClientSide) {
            return;
        }

        this.updateStatus();

        if (this.statusCode != IsmStatusCodes.READY) {
            return;
        }

        final SpatialStoragePlot plot = this.getPlot(this.inv.getStackInSlot(InventorySlots.INPUT)).get();
        final int modifiableBlocks = (int) this.getModifiableBlocks(plot).count();
        final int cycleCount = (int) Math.ceil(modifiableBlocks / (double) BLOCKS_PER_CYCLE);
        if (cycleCount == 0) {
            this.statusCode = IsmStatusCodes.CELL_FULL;
            this.markForUpdate();
            return;
        }

        final List<ItemStack> catalysts = IsmService.get(this).get().triggerOperation(cycleCount);
        if (catalysts.size() <= 0) {
            this.statusCode = IsmStatusCodes.NO_CATALYSTS;
            this.markForUpdate();
            return;
        }

        catalysts.forEach(this.catalystInv::addItems);
        final Collection<ItemStack> catalystItems = Lists.newArrayList(this.catalystInv.iterator());
        this.activeWeights = Optional.of(IsmWeightWrapper.fromCatalysts(catalystItems, cycleCount, this.level));
        this.maxProgressTicks = (int) Math.ceil(modifiableBlocks / this.getWorkRate());
        this.progressTicks = 0;

        this.statusCode = IsmStatusCodes.RUNNING;

        this.getMainNode().ifPresent((grid, node) -> {
            grid.getTickManager().wakeDevice(node);
        });
        this.markForUpdate();
    }

    public boolean completeOperation() {
        if (this.level.isClientSide) {
            return false;
        }
        else if (IsmService.get(this).isEmpty()) {
            return false;
        }

        final ItemStack cell = this.inv.getStackInSlot(InventorySlots.INPUT);
        if (this.statusCode != IsmStatusCodes.RUNNING
                || !this.isSpatialCell(cell)
                || !this.inv.getStackInSlot(InventorySlots.OUTPUT).isEmpty()
                || !this.getMainNode().isActive()) {
            // Something weird happened - call for an update and hope that displays an appropriate error
            this.updateStatus();
            return false;
        }

        final SpatialStoragePlot plot = this.getPlot(cell).get();
        final var spatialLevel = SpatialStoragePlotManager.INSTANCE.getLevel();
        final Supplier<Block> blockSupplier = this.activeWeights.get().getSupplier();
        this.getModifiableBlocks(plot).forEach(blockPos -> {
            spatialLevel.setBlock(blockPos, blockSupplier.get().defaultBlockState(), Block.UPDATE_NONE);
        });

        // Operation completed, so move the input to the output
        this.inv.setItemDirect(InventorySlots.INPUT, ItemStack.EMPTY);
        this.inv.setItemDirect(InventorySlots.OUTPUT, cell);
        for (int i = 0; i < this.catalystInv.size(); i++) {
            this.catalystInv.setItemDirect(i, ItemStack.EMPTY);
        }

        return true;
    }

    private Stream<BlockPos> getModifiableBlocks(final SpatialStoragePlot plot) {
        final var spatialLevel = SpatialStoragePlotManager.INSTANCE.getLevel();
        final BlockPos startPos = plot.getOrigin();
        final BlockPos endPos = new BlockPos(
                startPos.getX() + plot.getSize().getX() - 1,
                startPos.getY() + plot.getSize().getY() - 1,
                startPos.getZ() + plot.getSize().getZ() - 1);
        return BlockPos.betweenClosedStream(startPos, endPos)
                .filter(blockPos -> {
                    final BlockState bs = spatialLevel.getBlockState(blockPos);
                    // Matrix frame blocks are used to fill the empty space in the allocated space, so we overwrite
                    // those
                    return bs.isAir() || bs.getBlock().equals(AEBlocks.MATRIX_FRAME.block());
                });
    }

    private Optional<SpatialStoragePlot> getPlot(final ItemStack cell) {
        final var plotManager = SpatialStoragePlotManager.INSTANCE;
        final var spatialCell = (SpatialStorageCellItem) cell.getItem();
        return Optional.ofNullable(plotManager.getPlot(spatialCell.getAllocatedPlotId(cell)));
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

    /** @return The work rate, in blocks per tick */
    private double getWorkRate() {
        // TODO Make this configurable
        // TODO Implement acceleration cards here
        return 0.125;
    }

    public double getPowerDraw() {
        return this.basePowerDraw;
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

        if (AEPowerUtil.drawPower(this, this.getPowerDraw())) {
            this.progressTicks++;
            this.statusCode = IsmStatusCodes.RUNNING;
        }
        else {
            this.statusCode = IsmStatusCodes.INSUFFICIENT_POWER;
        }

        if (this.progressTicks >= this.maxProgressTicks) {
            if (this.completeOperation()) {
                this.resetOperation();
                return TickRateModulation.SLEEP;
            }
        }

        return TickRateModulation.SAME;
    }

    public void updateStatus() {
        if (this.isClientSide()) {
            return;
        }

        final int oldStatusCode = this.statusCode;
        if (!this.getMainNode().isActive()) {
            this.statusCode = IsmStatusCodes.MISSING_CHANNEL;
        }
        else if (IsmService.get(this) == null) {
            this.statusCode = IsmStatusCodes.UNKNOWN;
        }
        else if (this.inv.getStackInSlot(InventorySlots.INPUT).isEmpty()) {
            this.statusCode = IsmStatusCodes.IDLE;
        }
        else if (!this.inv.getStackInSlot(InventorySlots.OUTPUT).isEmpty()) {
            this.statusCode = IsmStatusCodes.OUTPUT_FULL;
        }
        else {
            final ItemStack is = this.inv.getStackInSlot(InventorySlots.INPUT);
            final Optional<SpatialStoragePlot> plotOpt = this.getPlot(is);
            if (plotOpt.isEmpty()) {
                this.statusCode = IsmStatusCodes.NOT_FORMATTED;
            }
            else {
                this.statusCode = IsmStatusCodes.READY;
            }
        }

        if (oldStatusCode != this.statusCode) {
            if (!(this.statusCode == IsmStatusCodes.READY) &&
                    (oldStatusCode == IsmStatusCodes.RUNNING || oldStatusCode == IsmStatusCodes.INSUFFICIENT_POWER)) {
                // Something has gone wrong while an operation was running, so cancel it out
                this.resetOperation();
            }

            this.markForUpdate();
            this.getMainNode().ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        }
    }

    private void resetOperation() {
        this.progressTicks = -1;
        this.maxProgressTicks = -1;
        this.activeWeights = Optional.empty();
    }

    // ***
    // Boilerplate stuff
    // ***

    @Override
    public void saveAdditional(final CompoundTag data) {
        super.saveAdditional(data);
        data.putInt("lastRedstoneState", this.lastRedstoneState.ordinal());
        if (this.maxProgressTicks > -1) {
            data.putInt("maxProgressTicks", this.maxProgressTicks);
            data.putInt("progressTicks", this.progressTicks);
        }
    }

    @Override
    public void loadTag(final CompoundTag data) {
        super.loadTag(data);
        if (data.contains("lastRedstoneState")) {
            this.lastRedstoneState = YesNo.values()[data.getInt("lastRedstoneState")];
        }
        if (data.contains("maxProgressTicks") && data.contains("progressTicks")) {
            this.maxProgressTicks = data.getInt("maxProgressTicks");
            this.progressTicks = data.getInt("progressTicks");
            this.statusCode = IsmStatusCodes.RUNNING;
        }
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeInt(maxProgressTicks);
        data.writeInt(progressTicks);
        data.writeInt(statusCode);

        for (int i = 0; i < this.inv.size(); i++) {
            data.writeItem(inv.getStackInSlot(i));
        }
        for (int i = 0; i < this.catalystInv.size(); i++) {
            data.writeItem(catalystInv.getStackInSlot(i));
        }
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);

        final int prevMaxProgress = this.maxProgressTicks;
        this.maxProgressTicks = data.readInt();
        ret |= (prevMaxProgress != this.maxProgressTicks);
        final int prevProgress = this.progressTicks;
        this.progressTicks = data.readInt();
        ret |= (prevProgress != this.progressTicks);
        final int prevStatusCode = this.statusCode;
        this.statusCode = data.readInt();
        ret |= (prevStatusCode != this.statusCode);

        for (int i = 0; i < this.inv.size(); i++) {
            this.inv.setItemDirect(i, data.readItem());
        }
        for (int i = 0; i < this.catalystInv.size(); i++) {
            this.catalystInv.setItemDirect(i, data.readItem());
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
        return this.statusCode == IsmStatusCodes.RUNNING || this.statusCode == IsmStatusCodes.INSUFFICIENT_POWER;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return ImpracticalEnergisticsMod.IMAGINARY_SPACE_MANIPULATOR_ITEM.get();
    }
}
