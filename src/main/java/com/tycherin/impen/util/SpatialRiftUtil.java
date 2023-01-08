package com.tycherin.impen.util;

import java.util.Optional;

import appeng.api.implementations.items.ISpatialStorageCell;
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

    public static Optional<ISpatialStorageCell> getSpatialCell(final ItemStack cell) {
        if (!cell.isEmpty() && cell.getItem() instanceof ISpatialStorageCell spatialCell) {
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
