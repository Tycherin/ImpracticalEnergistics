package com.tycherin.impen.blockentity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpenRegistry;
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
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ImaginarySpaceManipulatorBlockEntity extends AENetworkInvBlockEntity
        implements IGridTickable, IUpgradeableObject {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Number of blocks that can be affected by a single catalyst item */
    private static final int BLOCKS_PER_CYCLE = 64;
    /** Base number of ticks required to process one block */
    private static final double TICKS_PER_BLOCK = 8;

    private static class InventorySlots {
        public static final int INPUT = 0;
        public static final int OUTPUT = 1;
    }

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 2);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new InventoryItemFilter());
    private final AppEngInternalInventory catalystInv = new AppEngInternalInventory(this, IsmService.MAX_CATALYSTS);
    private final IUpgradeInventory upgrades;
    private final ILevelRunnable callback = level -> startOperation();

    /**
     * This field has different semantics depending on the ISM's state.
     * 
     * If the ISM is running:
     * This is the operation that is currently running.
     * 
     * If the ISM is not running:
     * This is the operation that would run if the ISM was activated.
     */
    private Optional<IsmOperation> operation = Optional.empty();

    private int progressTicks = -1;
    private int maxProgressTicks = -1;
    private YesNo lastRedstoneState = YesNo.UNDECIDED;
    private int statusCode = IsmStatusCodes.UNKNOWN;
    private double basePowerDraw;

    public ImaginarySpaceManipulatorBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.IMAGINARY_SPACE_MANIPULATOR_BE.get(), pos, blockState);

        this.getMainNode()
                .addService(IGridTickable.class, this)
                .setFlags();

        this.upgrades = UpgradeInventories.forMachine(ImpenRegistry.IMAGINARY_SPACE_MANIPULATOR_ITEM.get(),
                3, this::saveChanges);
        this.basePowerDraw = ImpenConfig.POWER.imaginarySpaceManipulatorCost();
    }

    // ***
    // State graph maintenance methods
    // ***

    public int computeStatus() {
        final int newStatusCode;
        if (!this.getMainNode().isActive()) {
            newStatusCode = IsmStatusCodes.MISSING_CHANNEL;
        }
        else if (this.inv.getStackInSlot(0).isEmpty()) {
            newStatusCode = IsmStatusCodes.IDLE;
        }
        else if (!this.inv.getStackInSlot(1).isEmpty()) {
            newStatusCode = IsmStatusCodes.OUTPUT_FULL;
        }
        else if (this.isRunning()) {
            newStatusCode = IsmStatusCodes.RUNNING;
        }
        else if (this.operation.isEmpty()) {
            newStatusCode = IsmStatusCodes.UNKNOWN;
        }
        else if (this.operation.get().plot.isEmpty()) {
            newStatusCode = IsmStatusCodes.NOT_FORMATTED;
        }
        else if (this.operation.get().blocksToUpdate == 0) {
            newStatusCode = IsmStatusCodes.CELL_FULL;
        }
        else if (!this.operation.get().hasCatalysts) {
            newStatusCode = IsmStatusCodes.NO_CATALYSTS;
        }
        else {
            newStatusCode = IsmStatusCodes.READY;
        }
        return newStatusCode;
    }

    @Override
    public void onMainNodeStateChanged(final IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.getMainNode().ifPresent((grid, node) -> {
                grid.getTickManager().alertDevice(node);
            });
        }
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot) {
        if (this.isClientSide()) {
            return;
        }

        this.getMainNode().ifPresent((grid, node) -> {
            grid.getTickManager().alertDevice(node);
        });
    }

    // ***
    // Oreify operation methods
    // ***

    /**
     * Begins a new operation by collecting catalysts from the {@link IsmService}, storing them in the catalyst
     * inventory, and flagging this object's state accordingly.
     */
    public void startOperation() {
        if (this.isClientSide() || this.isRemoved()) {
            return;
        }

        this.updateOperation();
        if (this.operation.isEmpty() || !this.operation.get().isValid()) {
            return;
        }

        final int cycleCount = this.getCycleCount(operation.get().plot().get());
        final List<ItemStack> catalysts = IsmService.get(this).get().triggerOperation(cycleCount);
        catalysts.forEach(this.catalystInv::addItems);
        this.maxProgressTicks = (int) Math.ceil(cycleCount * BLOCKS_PER_CYCLE / this.getWorkRate());
        this.progressTicks = 0;
        this.statusCode = IsmStatusCodes.RUNNING;

        this.getMainNode().ifPresent((grid, node) -> {
            grid.getTickManager().alertDevice(node);
        });
        this.markForUpdate();
    }

    /** @return True if the operation was completed successfully; false otherwise */
    public boolean completeOperation() {
        if (this.level.isClientSide() || this.isRemoved()) {
            return false;
        }

        if (this.operation.isEmpty()) {
            LOGGER.warn("Attempted to complete operation, but operation was missing");
            return false;
        }
        else if (!this.operation.get().isValid()) {
            LOGGER.warn("Attempted to complete operation, but operation was invalid");
            return false;
        }

        final SpatialStoragePlot plot = this.operation.get().plot().get();
        final var spatialLevel = SpatialStoragePlotManager.INSTANCE.getLevel();
        final int cycleCount = this.getCycleCount(plot);
        final Collection<ItemStack> catalystItems = Lists.newArrayList(this.catalystInv.iterator());
        final var weights = IsmWeightWrapper.fromCatalysts(catalystItems, cycleCount, this.level);

        // Go through the target zone and update each block based on the supplier results
        final Supplier<Block> blockSupplier = weights.getSupplier();
        this.getReplaceableBlocks(plot).forEach(blockPos -> {
            spatialLevel.setBlock(blockPos, blockSupplier.get().defaultBlockState(), Block.UPDATE_NONE);
        });

        // Operation completed, so move the input to the output
        this.inv.setItemDirect(InventorySlots.INPUT, ItemStack.EMPTY);
        this.inv.setItemDirect(InventorySlots.OUTPUT, this.operation.get().cell.get());
        this.resetOperation();
        return true;
    }
    
    private void updateOperation() {
        if (this.operation.isPresent() && this.isRunning()) {
            // Do nothing
            // The operation can't change while the input item is constant, so there's no need to recompute it here
            // If the input item changes, resetOperation() will clear catalysts and isRunning() will be false
        }
        else {
            this.operation = this.buildOperation();
        }
    }

    /** @return An operation object based on the current input cell, or empty if there isn't a valid input cell */
    private Optional<IsmOperation> buildOperation() {
        final ItemStack cell = this.inv.getStackInSlot(InventorySlots.INPUT);
        if (cell.isEmpty()) {
            return Optional.empty();
        }

        if (!ImaginarySpaceManipulatorBlockEntity.isSpatialCell(cell)) {
            // This should be blocked by the inventory filter
            throw new IllegalArgumentException(String.format("ItemStack %s is not a valid input", cell));
        }

        final Optional<SpatialStoragePlot> plot = this.getPlot(cell);
        if (plot.isEmpty()) {
            return Optional.of(new IsmOperation(Optional.of(cell), plot, 0L, false));
        }

        final long blocksToUpdate = this.getReplaceableBlocks(plot.get()).count();
        if (blocksToUpdate == 0) {
            return Optional.of(new IsmOperation(Optional.of(cell), plot, blocksToUpdate, false));
        }

        final boolean hasCatalysts;
        if (this.isRunning()) {
            hasCatalysts = true;
        }
        else {
            hasCatalysts = IsmService.get(this).get().hasCatalysts();
        }
        return Optional.of(new IsmOperation(Optional.of(cell), plot, blocksToUpdate, hasCatalysts));
    }

    private void resetOperation() {
        this.progressTicks = -1;
        this.maxProgressTicks = -1;
        for (int i = 0; i < this.catalystInv.size(); i++) {
            this.catalystInv.setItemDirect(i, ItemStack.EMPTY);
        }
        this.updateOperation();
    }

    // ***
    // Misc getters & utility methods
    // ***

    /**
     * @param plot Input plot to evaluate
     * @return A stream of all the blocks in the target plot that can be replaced
     */
    private Stream<BlockPos> getReplaceableBlocks(final SpatialStoragePlot plot) {
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

    /** @return The spatial storage plot for the given cell, or empty if ther isn't one allocated */
    private Optional<SpatialStoragePlot> getPlot(final ItemStack cell) {
        if (cell.isEmpty()) {
            return Optional.empty();
        }
        final var plotManager = SpatialStoragePlotManager.INSTANCE;
        final var spatialCell = (SpatialStorageCellItem) cell.getItem();
        return Optional.ofNullable(plotManager.getPlot(spatialCell.getAllocatedPlotId(cell)));
    }

    /** @return The number of cycles required to process this plot */
    private int getCycleCount(final SpatialStoragePlot plot) {
        final int modifiableBlocks = (int) this.getReplaceableBlocks(plot).count();
        final int cycleCount = (int) Math.ceil(modifiableBlocks / (double) BLOCKS_PER_CYCLE);
        return cycleCount;
    }

    /** @return The work rate, in blocks per tick */
    private double getWorkRate() {
        return Math.pow(2, this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD)) / TICKS_PER_BLOCK;
    }

    /** @return The amount of power drawn per tick */
    public double getPowerDraw() {
        return this.basePowerDraw * this.getWorkRate();
    }

    public void updateRedstoneState() {
        final YesNo currentState = this.level.getBestNeighborSignal(this.worldPosition) != 0 ? YesNo.YES : YesNo.NO;
        if (this.lastRedstoneState != currentState) {
            this.lastRedstoneState = currentState;

            if (!this.isClientSide() && this.lastRedstoneState == YesNo.YES) {
                TickHandler.instance().addCallable(null, callback);
            }
        }
    }

    /** @return True if there is currently an operation running; false otherwise */
    private boolean isRunning() {
        if (this.isClientSide()) {
            return this.statusCode == IsmStatusCodes.RUNNING;
        }
        else {
            // If any catalysts are present, then there is an operation running
            for (var stack : this.catalystInv) {
                if (!stack.isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }

    // ***
    // Ticking methods
    // ***

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(1, 20, !this.isRunning(), true);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (this.isRemoved()) {
            return TickRateModulation.SAME;
        }

        // First, update the current status
        this.updateOperation();
        final int oldStatus = this.statusCode;
        final int newStatus = this.computeStatus();
        this.statusCode = newStatus;

        final TickRateModulation rate;
        if (this.statusCode != IsmStatusCodes.RUNNING) {
            // If the input got removed, cancel the current operation
            if (this.statusCode == IsmStatusCodes.IDLE && this.isRunning()) {
                this.resetOperation();
            }

            // If we're not running an operation, the device can go idle. It will get alerted if the inventory changes
            // or an operation is triggered.
            rate = TickRateModulation.IDLE;
        }
        else {
            if (AEPowerUtil.drawPower(this, this.getPowerDraw() * ticksSinceLastCall)) {
                this.progressTicks += Math.floor(this.getWorkRate() * ticksSinceLastCall);
            }

            if (this.progressTicks >= this.maxProgressTicks) {
                if (this.completeOperation()) {
                    this.resetOperation();
                    rate = TickRateModulation.IDLE;
                }
                else {
                    rate = TickRateModulation.SAME;
                }
            }
            else {
                rate = TickRateModulation.FASTER;
            }
        }

        // We delay doing this update until the end to avoid triggering it multiple times if there are multiple updates
        if (oldStatus != newStatus) {
            this.markForUpdate();
        }
        return rate;
    }

    // ***
    // Data serialization stuff
    // ***

    @Override
    public void saveAdditional(final CompoundTag data) {
        super.saveAdditional(data);
        data.putInt("lastRedstoneState", this.lastRedstoneState.ordinal());
        if (this.maxProgressTicks > -1) {
            data.putInt("maxProgressTicks", this.maxProgressTicks);
            data.putInt("progressTicks", this.progressTicks);
        }
        this.upgrades.writeToNBT(data, "upgrades");
        this.catalystInv.writeToNBT(data, "catalysts");
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
        }
        this.upgrades.readFromNBT(data, "upgrades");
        this.catalystInv.readFromNBT(data, "catalysts");
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

    // ***
    // Boilerplate stuff
    // ***

    @Override
    public void addAdditionalDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        upgrades.forEach(drops::add);
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
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

    public int getCurrentProgress() {
        return this.progressTicks;
    }

    public int getMaxProgress() {
        return this.maxProgressTicks;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return ImpenRegistry.IMAGINARY_SPACE_MANIPULATOR_ITEM.get();
    }

    // ***
    // Inner classes
    // ***

    private static record IsmOperation(Optional<ItemStack> cell, Optional<SpatialStoragePlot> plot,
            Long blocksToUpdate, boolean hasCatalysts) {
        public boolean isValid() {
            return this.plot.isPresent() && blocksToUpdate > 0 && hasCatalysts;
        }
    }

    private class InventoryItemFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(final InternalInventory inv, final int slot, final int amount) {
            return slot == InventorySlots.OUTPUT;
        }

        @Override
        public boolean allowInsert(final InternalInventory inv, final int slot, final ItemStack stack) {
            return slot == InventorySlots.INPUT && ImaginarySpaceManipulatorBlockEntity.isSpatialCell(stack);
        }
    }

    public static boolean isSpatialCell(final ItemStack cell) {
        if (!cell.isEmpty() && cell.getItem() instanceof ISpatialStorageCell spatialCell) {
            return spatialCell.isSpatialStorage(cell);
        }
        else {
            return false;
        }
    }
}
