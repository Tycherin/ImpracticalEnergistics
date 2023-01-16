package com.tycherin.impen.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.ForgeRegistries;

public class SpatialRiftCellDataManager {

    public static final SpatialRiftCellDataManager INSTANCE = new SpatialRiftCellDataManager();

    private static final Logger LOGGER = LogUtils.getLogger();

    private SpatialRiftCellDataManager() {
    }

    public Optional<RiftCellData> getDataForPlot(final int plotId) {
        return getData().get(plotId);
    }

    public void putDataForPlot(final RiftCellData data) {
        getData().put(data.getPlotId(), data);
    }

    public void clearPlot(final int plotId) {
        getData().remove(plotId);
    }

    private RiftCellWorldData getData() {
        return SpatialStoragePlotManager.INSTANCE.getLevel().getChunkSource().getDataStorage().computeIfAbsent(
                RiftCellWorldData::load,
                RiftCellWorldData::new,
                RiftCellWorldData.ID);
    }

    private static class RiftCellWorldData extends SavedData {
        private static final String ID = "impen_rift_cell_storage";
        private static final String TAG_PLOTS = "plots";

        private final Int2ObjectLinkedOpenHashMap<RiftCellData> entries;

        public RiftCellWorldData() {
            entries = new Int2ObjectLinkedOpenHashMap<>();
        }

        public void put(final int plotId, final RiftCellData data) {
            entries.put(plotId, data);
            this.setDirty();
        }

        public void remove(final int plotId) {
            entries.remove(plotId);
            this.setDirty();
        }

        public Optional<RiftCellData> get(final int plotId) {
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
                final RiftCellData cellData = RiftCellData.fromTag((CompoundTag)entryTag);
                worldData.put(cellData.getPlotId(), cellData);
            });
            LOGGER.info("Loaded rift cell data for {} plots", plots.size());
            return worldData;
        }
    }

    public static class RiftCellData {
        private static final String TAG_PLOT_ID = "id";
        private static final String TAG_INPUTS = "inputs";

        private final int plotId;
        private final Map<Block, Integer> storedInputs;

        public RiftCellData(final int plotId, final Map<Block, Integer> storedInputs) {
            this.plotId = plotId;
            this.storedInputs = storedInputs;
        }
        
        public RiftCellData(final int plotId) {
            this(plotId, new HashMap<>());
        }

        public int getPlotId() {
            return plotId;
        }

        public SpatialStoragePlot getPlot() {
            return SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        }

        public Map<Block, Integer> getStoredInputs() {
            return storedInputs;
        }

        public void addOrIncrementBlock(final Block block) {
            if (storedInputs.containsKey(block)) {
                storedInputs.put(block, storedInputs.get(block) + 1);
            }
            else {
                storedInputs.put(block, 1);
            }
        }

        public CompoundTag getAsTag() {
            final CompoundTag tag = new CompoundTag();
            tag.putInt(TAG_PLOT_ID, plotId);
            final CompoundTag inputsTag = new CompoundTag();
            tag.put(TAG_INPUTS, inputsTag);
            storedInputs.forEach((block, count) -> {
                inputsTag.putInt(block.getRegistryName().toString(), count);
            });
            return tag;
        }

        public static RiftCellData fromTag(final CompoundTag tag) {
            final int plotId = tag.getInt(TAG_PLOT_ID);
            final CompoundTag inputsTag = tag.getCompound(TAG_INPUTS);
            final Map<Block, Integer> storedInputs = new HashMap<>();
            inputsTag.getAllKeys().forEach(blockId -> {
                final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                if (block == null) {
                    LOGGER.warn("Block {} missing from registry; value will be ignored", blockId);
                }
                storedInputs.put(block, tag.getInt(blockId));
            });
            return new RiftCellData(plotId, storedInputs);
        }
    }
}
