package com.tycherin.impen.logic;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.item.SpatialRiftCellItem;

import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.ForgeRegistries;

public class SpatialRiftCellDataManager {

    public static final SpatialRiftCellDataManager INSTANCE = new SpatialRiftCellDataManager();

    private static final Logger LOGGER = LogUtils.getLogger();

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
        return SpatialStoragePlotManager.INSTANCE.getLevel().getChunkSource().getDataStorage().computeIfAbsent(
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
            LOGGER.info("Loaded rift cell data for {} plots", plots.size());
            return worldData;
        }
    }

    public static class SpatialRiftCellData {
        private static final String TAG_PLOT_ID = "id";
        private static final String TAG_INPUTS = "inputs";

        private final int plotId;
        private final Set<Block> storedInputs;

        public SpatialRiftCellData(final int plotId, final Set<Block> storedInputs) {
            this.plotId = plotId;
            this.storedInputs = storedInputs;
        }

        public SpatialRiftCellData(final int plotId) {
            this(plotId, new HashSet<>());
        }

        public int getPlotId() {
            return plotId;
        }

        public SpatialStoragePlot getPlot() {
            return SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        }

        public Set<Block> getStoredInputs() {
            return storedInputs;
        }

        public boolean addBlock(final Block block) {
            return storedInputs.add(block);
        }

        public CompoundTag getAsTag() {
            final CompoundTag tag = new CompoundTag();
            tag.putInt(TAG_PLOT_ID, plotId);
            final ListTag inputsTag = new ListTag();
            tag.put(TAG_INPUTS, inputsTag);
            storedInputs.forEach(block -> {
                inputsTag.add(StringTag.valueOf(block.getRegistryName().toString()));
            });
            return tag;
        }

        public static SpatialRiftCellData fromTag(final CompoundTag tag) {
            final int plotId = tag.getInt(TAG_PLOT_ID);
            final ListTag inputsTag = tag.getList(TAG_INPUTS, Tag.TAG_STRING);
            final Set<Block> storedInputs = new HashSet<>();
            inputsTag.forEach(blockIdTag -> {
                final String blockId = ((StringTag)blockIdTag).getAsString();
                final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                if (block == null) {
                    LOGGER.warn("Block {} missing from registry; value will be ignored", blockId);
                }
                storedInputs.add(block);
            });
            return new SpatialRiftCellData(plotId, storedInputs);
        }

        public void addModifierClear() {
            this.storedInputs.clear();
            // TODO Mess with precision here
        }

        public void addModifierBoost() {
            // TODO Add bonus precision
        }

        public void addModifier(final Block block) {
            this.storedInputs.add(block);
        }

        public int getRemainingSlots() {
            return getMaxMarkerCount() - this.storedInputs.size();
        }

        public final int getMaxMarkerCount() {
            final SpatialStoragePlot plot = getPlot();
            final int blockCount = plot.getSize().getX()
                    * plot.getSize().getY()
                    * plot.getSize().getZ();

            return Math.max(((int)Math.sqrt(blockCount)) - 1, 1);
        }
    }
}
