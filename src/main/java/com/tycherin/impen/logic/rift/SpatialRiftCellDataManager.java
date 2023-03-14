package com.tycherin.impen.logic.rift;

import java.util.Optional;

import com.tycherin.impen.item.SpatialRiftCellItem;

import appeng.spatial.SpatialStoragePlotManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Class for managing storing Spatial Rift Cell data
 */
@Slf4j
public class SpatialRiftCellDataManager {

    public static final SpatialRiftCellDataManager INSTANCE = new SpatialRiftCellDataManager();

    private SpatialRiftCellDataManager() {
    }

    public Optional<SpatialRiftCellData> getDataForPlot(final int plotId) {
        return getData().get(plotId);
    }

    public Optional<SpatialRiftCellData> getDataForCell(final ItemStack is) {
        if (is.getItem() instanceof SpatialRiftCellItem item) {
            return getDataForPlot(item.getPlotId(is));
        }
        else {
            return Optional.empty();
        }
    }

    public void putDataForPlot(final SpatialRiftCellData data) {
        getData().put(data.getPlotId(), data);
    }

    public void clearPlot(final int plotId) {
        getData().remove(plotId);
    }

    private RiftCellWorldData getData() {
        return SpatialStoragePlotManager.INSTANCE.getLevel().getChunkSource().getDataStorage()
                .computeIfAbsent(
                        RiftCellWorldData::load,
                        RiftCellWorldData::new,
                        RiftCellWorldData.ID);
    }

    private static class RiftCellWorldData extends SavedData {
        private static final String ID = "impen_rift_cell_storage";
        private static final String TAG_PLOTS = "plots";

        private final Int2ObjectLinkedOpenHashMap<SpatialRiftCellData> entries;

        public RiftCellWorldData() {
            entries = new Int2ObjectLinkedOpenHashMap<>();
        }

        public void put(final int plotId, final SpatialRiftCellData data) {
            entries.put(plotId, data);
            this.setDirty();
        }

        public void remove(final int plotId) {
            entries.remove(plotId);
            this.setDirty();
        }

        public Optional<SpatialRiftCellData> get(final int plotId) {
            return Optional.ofNullable(entries.get(plotId));
        }

        @Override
        public CompoundTag save(final CompoundTag parentTag) {
            final ListTag tag = new ListTag();
            entries.forEach((plotId, data) -> {
                tag.add(data.getAsTag());
            });
            parentTag.put(TAG_PLOTS, tag);
            return parentTag;
        }

        public static RiftCellWorldData load(final CompoundTag tag) {
            final RiftCellWorldData worldData = new RiftCellWorldData();
            final ListTag plots = tag.getList(TAG_PLOTS, Tag.TAG_COMPOUND);
            plots.forEach(entryTag -> {
                final SpatialRiftCellData cellData = SpatialRiftCellData.fromTag((CompoundTag)entryTag);
                worldData.put(cellData.getPlotId(), cellData);
            });
            log.info("Loaded rift cell data for {} plots", plots.size());
            return worldData;
        }
    }
}
