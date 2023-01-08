package com.tycherin.impen.logic.rift;

import net.minecraft.nbt.CompoundTag;

public class SpatialRiftAllocation {

    private static final String TAG_PLOT_ID = "plot_id";
//    private static final String TAG_ORIGINAL_ITEM = "original_item";
//    private static final String TAG_MODIFICATIONS = "modifications";

    private final int plotId;

    public SpatialRiftAllocation(final int plotId) {
        this.plotId = plotId;
    }

    public int getPlotId() {
        return this.plotId;
    }

    public CompoundTag toTag() {
        final CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_PLOT_ID, plotId);
        return tag;
    }

    public static SpatialRiftAllocation fromTag(final CompoundTag tag) {
        final int plotId = tag.getInt(TAG_PLOT_ID);
        return new SpatialRiftAllocation(plotId);
    }
}
