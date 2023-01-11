package com.tycherin.impen.util;

import java.util.Optional;

import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.items.storage.SpatialStorageCellItem;
import appeng.spatial.SpatialStoragePlotManager;
import net.minecraft.world.item.ItemStack;

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
}
