package com.tycherin.impen.util;

import java.util.Optional;
import java.util.stream.Stream;

import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.core.definitions.AEBlocks;
import appeng.items.storage.SpatialStorageCellItem;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@UtilityClass
public class SpatialRiftUtil {

    public static boolean isSpatialCell(final ItemStack cell) {
        if (!cell.isEmpty() && cell.getItem() instanceof ISpatialStorageCell spatialCell) {
            return spatialCell.isSpatialStorage(cell);
        }
        else {
            return false;
        }
    }

    /*
     * AE2 provides the ISpatialStorageCell interface here, which we should technically be using instead of relying
     * on SpatialStorageCellItem directly. The only place where this would make a difference is if a non-AE2 mod
     * introduces a spatial cell implementation, and since I don't know how to handle that, I'm going to punt on it.
     */
    public static Optional<SpatialStorageCellItem> getSpatialCell(final ItemStack cell) {
        if (!cell.isEmpty() && cell.getItem() instanceof SpatialStorageCellItem spatialCell) {
            if (spatialCell.isSpatialStorage(cell)) {
                return Optional.of(spatialCell);
            }
        }
        return Optional.empty();
    }

    public static Optional<Integer> getPlotId(final ItemStack cell) {
        final var spatialCellOpt = SpatialRiftUtil.getSpatialCell(cell);
        if (spatialCellOpt.isEmpty()) {
            return Optional.empty();
        }

        final int plotId = ((ISpatialStorageCell)(spatialCellOpt.get())).getAllocatedPlotId(cell);
        final var plot = SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        if (plot == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(plotId);
        }
    }

    /**
     * @return A stream of all BlockPos within the plot
     */
    private static Stream<BlockPos> getAllBlocks(final SpatialStoragePlot plot) {
        final BlockPos firstCorner = plot.getOrigin();
        final BlockPos secondCorner = firstCorner.offset(
                plot.getSize().getX() - 1,
                plot.getSize().getY() - 1,
                plot.getSize().getZ() - 1);
        return BlockPos.betweenClosedStream(firstCorner, secondCorner);
    }

    /**
     * @return A stream of all BlockPos that can be interacted with
     */
    public static Stream<BlockPos> getEligibleBlocks(final SpatialStoragePlot plot) {
        final var spatialLevel = SpatialStoragePlotManager.INSTANCE.getLevel();
        return getAllBlocks(plot)
                .filter(blockPos -> spatialLevel.getBlockEntity(blockPos) != null);
    }

    /**
     * @return A stream of all BlockPos that have blocks in them
     */
    public static Stream<BlockPos> getExistingBlocks(final SpatialStoragePlot plot) {
        final var spatialLevel = SpatialStoragePlotManager.INSTANCE.getLevel();
        return getEligibleBlocks(plot)
                .filter(blockPos -> {
                    final BlockState bs = spatialLevel.getBlockState(blockPos);
                    return !bs.isAir() && !bs.getBlock().equals(AEBlocks.MATRIX_FRAME.block());
                });
    }

    /**
     * @return A stream of all BlockPos that don't have blocks in them
     */
    public static Stream<BlockPos> getClearBlocks(final SpatialStoragePlot plot) {
        final var spatialLevel = SpatialStoragePlotManager.INSTANCE.getLevel();
        return getEligibleBlocks(plot)
                .filter(blockPos -> {
                    final BlockState bs = spatialLevel.getBlockState(blockPos);
                    return bs.isAir() || bs.getBlock().equals(AEBlocks.MATRIX_FRAME.block());
                });
    }

    public static boolean isPlateClean(final SpatialStoragePlot plot) {
        // Plate is clean iff there are no replaceable blocks in the plot already
        return getExistingBlocks(plot).findAny().isEmpty();
    }
}
