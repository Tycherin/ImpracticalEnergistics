package com.tycherin.impen.blockentity;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpracticalEnergisticsMod;

import appeng.api.config.YesNo;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNodeListener;
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

public class ImaginarySpaceManipulatorBlockEntity extends AENetworkInvBlockEntity {
    
    private static final Logger LOGGER = LogUtils.getLogger();

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 2);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new SpatialIOFilter());
    private YesNo lastRedstoneState = YesNo.UNDECIDED;

    private final ILevelRunnable callback = level -> oreifySpatialCell();

    private boolean isActive = false;

    public ImaginarySpaceManipulatorBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpracticalEnergisticsMod.IMAGINARY_SPACE_MANIPULATOR_BE.get(), pos, blockState);
        this.getMainNode().setFlags();
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putInt("lastRedstoneState", this.lastRedstoneState.ordinal());
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        if (data.contains("lastRedstoneState")) {
            this.lastRedstoneState = YesNo.values()[data.getInt("lastRedstoneState")];
        }
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.isActive());
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);

        final boolean isActive = data.readBoolean();
        ret = isActive != this.isActive || ret;
        this.isActive = isActive;

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

    public boolean isActive() {
        if (level != null && !level.isClientSide) {
            return this.getMainNode().isOnline();
        } else {
            return this.isActive;
        }
    }

    @Override
    public void onMainNodeStateChanged(final IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.markForUpdate();
        }
    }

    private void triggerOreify() {
        if (!this.isClientSide()) {
            final ItemStack cell = this.inv.getStackInSlot(0);
            if (this.isSpatialCell(cell)) {
                // this needs to be cross world synced.
                TickHandler.instance().addCallable(null, callback);
            }
        }
    }

    private boolean isSpatialCell(final ItemStack cell) {
        if (!cell.isEmpty() && cell.getItem() instanceof ISpatialStorageCell spatialCell) {
            return spatialCell.isSpatialStorage(cell);
        }
        return false;
    }

    public void oreifySpatialCell() {
        if (this.level.isClientSide) {
            return;
        }
        final ItemStack cell = this.inv.getStackInSlot(0);
        if (!this.isSpatialCell(cell)) {
            // Shouldn't be possible, but check just in case
            return;
        }
        if (!this.inv.getStackInSlot(1).isEmpty()) {
            // Already something in the output
            return;
        }
        if (!this.getMainNode().isActive()) {
            return;
        }
        
        final var node = this.getMainNode();
        final var grid = node.getGrid();
        final var spatialCell = (SpatialStorageCellItem) cell.getItem();
        
        final BlockPos size = new BlockPos(4, 4, 4);
        
        final var plotManager = SpatialStoragePlotManager.INSTANCE;
        
        // TODO If the AE system has a spatial service, use that as the size; otherwise, default to filling the cell (max size that the cell can hold)
        
        final SpatialStoragePlot existingPlot = plotManager.getPlot(spatialCell.getAllocatedPlotId(cell));
        final SpatialStoragePlot plot;
        if (existingPlot != null) {
            // Check if it's the right size
            if (!existingPlot.getSize().equals(size)) {
                LOGGER.info("Allocated plot size {} is different from desired plot size {}", existingPlot.getSize(), size);
                return;
            }
            plot = existingPlot;
        }
        else {
            // Allocate a new plot
            int playerId;
            if (grid.getSecurityService().isAvailable()) {
                playerId = grid.getSecurityService().getOwner();
            } else {
                playerId = node.getNode().getOwningPlayerId();
            }
            plot = plotManager.allocatePlot(size, playerId);
            spatialCell.setStoredDimension(cell, plot.getId(), plot.getSize());
        }
        
        // TODO Fancy block replacement logic (with gameplay mechanics!) goes here
        // TODO Do this setup once during the relevant registry population event
        final TagKey<Block> oreTagKey = Tags.Blocks.ORES;
        final ITag<Block> oreTag = ForgeRegistries.BLOCKS.tags().getTag(oreTagKey);
        final Random random = new Random();
        
        final var spatialLevel = plotManager.getLevel();
        final BlockPos startPos = plot.getOrigin();
        final BlockPos endPos = new BlockPos(
                startPos.getX() + size.getX() - 1,
                startPos.getY() + size.getY() - 1,
                startPos.getZ() + size.getZ() - 1);
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
        
        // Move the cell into the output slot
        this.inv.setItemDirect(0, ItemStack.EMPTY);
        this.inv.setItemDirect(1, cell);
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

    private class SpatialIOFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(final InternalInventory inv, final int slot, final int amount) {
            return slot == 1;
        }

        @Override
        public boolean allowInsert(final InternalInventory inv, final int slot, final ItemStack stack) {
            return slot == 0 && ImaginarySpaceManipulatorBlockEntity.this.isSpatialCell(stack);
        }
    }

}
