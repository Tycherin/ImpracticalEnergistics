package com.tycherin.impen.logic.rift;

import appeng.core.AppEng;
import appeng.spatial.SpatialStorageDimensionIds;
import net.minecraft.server.level.ServerLevel;

public class SpatialRiftManager {

    public static final SpatialRiftManager INSTANCE = new SpatialRiftManager();

    public ServerLevel getLevel() {
        // In order to keep things consistent with AE2's SpatialStoragePlotManager, we use the same code here
        final var server = AppEng.instance().getCurrentServer();
        if (server == null) {
            throw new IllegalStateException("No server is currently running.");
        }
        final ServerLevel level = server.getLevel(SpatialStorageDimensionIds.WORLD_ID);
        if (level == null) {
            throw new IllegalStateException("The storage cell level is missing.");
        }
        return level;
    }

    private SpatialRiftData getWorldData() {
        return getLevel().getChunkSource().getDataStorage().computeIfAbsent(
                SpatialRiftData::load,
                SpatialRiftData::new,
                SpatialRiftData.DATA_ID);
    }

    public SpatialRiftAllocation getOrCreate(final int plotId) {
        final SpatialRiftData data = getWorldData();
        final var existingAllocation = data.getAllocation(plotId);
        if (existingAllocation.isPresent()) {
            return existingAllocation.get();
        }
        else {
            return data.createAllocation(plotId);
        }
    }

}
