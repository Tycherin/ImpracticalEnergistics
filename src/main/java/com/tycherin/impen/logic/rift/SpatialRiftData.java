package com.tycherin.impen.logic.rift;

import java.util.Optional;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

public class SpatialRiftData extends SavedData {
    
    /** ID of this data when it is attached to a level. */
    public static final String DATA_ID = "impen_rift_storage";
    public static final int DATA_FORMAT_VERSION = 1;
    
    private static final String TAG_FORMAT = "format";
    private static final String TAG_DRIVES = "drives";
    
    private final Int2ObjectOpenHashMap<SpatialRiftAllocation> allocations = new Int2ObjectOpenHashMap<>();
    
    // ***
    // Rift data methods
    // ***

    public Optional<SpatialRiftAllocation> getAllocation(final int plotId) {
        return Optional.ofNullable(allocations.get(plotId));
    }

    public SpatialRiftAllocation createAllocation(final int plotId) {
        if (allocations.containsKey(plotId)) {
            throw new RuntimeException("Allocation for " + plotId + " already exists");
        }
        final SpatialRiftAllocation allocation = new SpatialRiftAllocation(plotId);
        allocations.put(plotId, allocation);
        return allocation;
    }
    
    // ***
    // Save/load methods
    // ***
    
    public static SpatialRiftData load(final CompoundTag tag) {
        final int formatVersion = tag.getInt("format");
        if (formatVersion != DATA_FORMAT_VERSION) {
            throw new IllegalStateException("Invalid Impen spatial rift info version: " + formatVersion);
        }
        
        final SpatialRiftData result = new SpatialRiftData();
        final ListTag drivesTag = tag.getList(TAG_DRIVES, Tag.TAG_COMPOUND);
        for (final Tag driveTag : drivesTag) {
            final SpatialRiftAllocation entry = SpatialRiftAllocation.fromTag((CompoundTag) driveTag);
//            if (result.plots.containsKey(plot.getId())) {
//                AELog.warn("Overwriting duplicate plot id %s", plot.getId());
//            }
            result.allocations.put(entry.getPlotId(), entry);
        }
        return result;
    }

    @Override
    public CompoundTag save(final CompoundTag tag) {
        tag.putInt(TAG_FORMAT, DATA_FORMAT_VERSION);

        final ListTag allocationTags = new ListTag();
        for (final SpatialRiftAllocation entry : allocations.values()) {
            allocationTags.add(entry.toTag());
        }
        tag.put(TAG_DRIVES, allocationTags);

        return tag;
    }

}
