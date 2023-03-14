package com.tycherin.impen.logic.rift;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tycherin.impen.util.RegistryUtil;
import com.tycherin.impen.util.SpatialRiftUtil;

import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;

/**
 * Persisted representation of Spatial Rift Cell data
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class SpatialRiftCellData {

    private static final int MAX_BOOST_LEVEL = 15;

    /** Plot ID of matching spatial storage plot */
    private final int plotId;
    /** Size of the stored plot in blocks */
    private final int blockCount;
    /** The number of slots available on this cell */
    private final int totalSlots;
    /** Flag for whether the corresponding spatial plot had a clean plate when created */
    private final boolean isPlateClean;

    /** The number of slots in use on this cell */
    private int usedSlots = 0;
    /** The number of precision upgrades applied to this cell */
    private int precisionLevel = 0;
    /** The number of richness upgrades applied to this cell */
    private int richnessLevel = 0;
    /** The base block used for filling space in the cell */
    @Setter
    private Optional<Block> baseBlock = Optional.empty();
    /** The list of blocks being boosted in this cell */
    private List<BlockBoost> boosts = new ArrayList<>();

    public SpatialRiftCellData(final SpatialStoragePlot plot) {
        this(plot.getId(),
                plot.getSize().getX() * plot.getSize().getY() * plot.getSize().getZ(),
                // Plate is clean iff there are no replaceable blocks in the plot already
                SpatialRiftUtil.getExistingBlocks(plot).findAny().isEmpty());
    }

    public SpatialRiftCellData(final int plotId, final int blockCount, final boolean isPlateClean) {
        this.plotId = plotId;
        this.blockCount = blockCount;
        this.isPlateClean = isPlateClean;

        // This seemed like a better way of doing things rather than doing cube root shenanigans
        final int maxSlots;
        if (blockCount <= (2 * 2 * 2)) {
            maxSlots = 1;
        }
        else if (blockCount <= (4 * 4 * 4)) {
            maxSlots = 2;
        }
        else if (blockCount <= (6 * 6 * 6)) {
            maxSlots = 3;
        }
        else if (blockCount <= (8 * 8 * 8)) {
            maxSlots = 5;
        }
        else if (blockCount <= (10 * 10 * 10)) {
            maxSlots = 6;
        }
        else if (blockCount <= (12 * 12 * 12)) {
            maxSlots = 7;
        }
        else {
            maxSlots = 8;
        }
        this.totalSlots = maxSlots;
    }

    /**
     * @return The number of slots available for use on this cell
     */
    public int getAvailableSlots() {
        return this.getTotalSlots() - this.getUsedSlots();
    }

    public SpatialStoragePlot getPlot() {
        return SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
    }

    /**
     * Boosts the cell's precision level by one
     */
    public void boostPrecision() {
        this.boostPrecision(1);
    }

    /**
     * @param amount Amount to add to the cells current precision level
     */
    public void boostPrecision(final int amount) {
        if (!useSlot()) {
            return;
        }
        int newBoostLevel = this.precisionLevel + amount;
        if (newBoostLevel > MAX_BOOST_LEVEL) {
            log.error("Attempted to boost precision for {} to {}; level will be set to max instead",
                    this.plotId, newBoostLevel);
            newBoostLevel = MAX_BOOST_LEVEL;
        }
        this.precisionLevel = newBoostLevel;
    }

    /**
     * Boosts the cell's richness level by one
     */
    public void boostRichness() {
        this.boostRichness(1);
    }

    /**
     * @param amount Amount to add to the cells current richness level
     */
    public void boostRichness(final int amount) {
        if (!useSlot()) {
            return;
        }
        int newBoostLevel = this.richnessLevel + amount;
        if (newBoostLevel > MAX_BOOST_LEVEL) {
            log.error("Attempted to boost richness for {} to {}; level will be set to max instead",
                    this.plotId, newBoostLevel);
            newBoostLevel = MAX_BOOST_LEVEL;
        }
        this.richnessLevel = newBoostLevel;
    }

    /**
     * @param block Block to boost
     */
    public void boostBlock(final Block block) {
        if (!useSlot()) {
            return;
        }
        this.boosts.stream()
                .filter(boost -> boost.block.equals(block))
                .findFirst()
                .ifPresentOrElse(
                        (boost) -> {
                            int newBoostLevel = boost.count + 1;
                            if (newBoostLevel > MAX_BOOST_LEVEL) {
                                log.error(
                                        "Attempted to boost block {} on cell {}, but level is at max; operation will be ignored",
                                        block.getRegistryName().toString(), this.plotId);
                            }
                            else {
                                boost.count = newBoostLevel;
                            }
                        },
                        () -> this.boosts.add(new BlockBoost(block)));
    }

    /**
     * @return True if a slot was reserved; false if not
     */
    private boolean useSlot() {
        if (this.totalSlots > this.usedSlots) {
            this.usedSlots++;
            return true;
        }
        else {
            log.warn("Attempted to use slot on {}, but all {} slots are already in use",
                    this.plotId, this.totalSlots);
            return false;
        }
    }

    /**
     * Clears all inputs from this cell, resetting it to its base state
     */
    public void clearInputs() {
        this.precisionLevel = 0;
        this.richnessLevel = 0;
        this.baseBlock = Optional.empty();
        this.boosts.clear();
        this.usedSlots = 0;
    }

    @Getter
    @RequiredArgsConstructor
    public static class BlockBoost {
        private final Block block;
        private int count = 1;
    }

    // ***
    // Serialization & Deserialization
    // ***

    private static class Tags {
        static final String PLOT_ID = "plot_id";
        static final String PLOT_SIZE = "plot_size";
        static final String CLEAN_PLATE = "clean_plate";
        static final String BASE_BLOCK = "base_block";
        static final String PRECISION = "precision";
        static final String RICHNESS = "richness";
        static final String BOOSTS = "boosts";
        static final String BOOSTS_BLOCK = "block";
        static final String BOOSTS_COUNT = "count";
    }

    public CompoundTag getAsTag() {
        final CompoundTag tag = new CompoundTag();
        tag.putInt(Tags.PLOT_ID, plotId);
        tag.putInt(Tags.PLOT_SIZE, blockCount);
        tag.putBoolean(Tags.CLEAN_PLATE, isPlateClean);
        baseBlock.ifPresent(block -> tag.putString(Tags.BASE_BLOCK, block.getRegistryName().toString()));
        tag.putInt(Tags.PRECISION, precisionLevel);
        tag.putInt(Tags.RICHNESS, richnessLevel);

        final ListTag boostsListTag = new ListTag();
        boosts.forEach(boost -> {
            final CompoundTag boostTag = new CompoundTag();
            boostTag.putString(Tags.BOOSTS_BLOCK, boost.block.getRegistryName().toString());
            boostTag.putInt(Tags.BOOSTS_COUNT, boost.count);
            boostsListTag.add(boostTag);
        });
        tag.put(Tags.BOOSTS, boostsListTag);
        return tag;
    }

    public static SpatialRiftCellData fromTag(final CompoundTag tag) {

        final int plotId = tag.getInt(Tags.PLOT_ID);
        final int plotSize = tag.getInt(Tags.PLOT_SIZE);
        final boolean isPlateClean = tag.getBoolean(Tags.CLEAN_PLATE);
        final SpatialRiftCellData data = new SpatialRiftCellData(plotId, plotSize, isPlateClean);

        if (tag.contains(Tags.BASE_BLOCK)) {
            final String baseBlockId = tag.getString(Tags.BASE_BLOCK);
            data.baseBlock = RegistryUtil.getBlockOptional(baseBlockId);
            if (data.baseBlock.isEmpty()) {
                log.warn("Unable to find base block with id {} for cell {}; input will be discarded",
                        baseBlockId, plotId);
            }
        }
        else {
            data.baseBlock = Optional.empty();
        }

        data.precisionLevel = tag.getInt(Tags.PRECISION);
        data.richnessLevel = tag.getInt(Tags.RICHNESS);

        tag.getList(Tags.BOOSTS, Tag.TAG_COMPOUND).forEach(boostTag -> {
            final CompoundTag ctag = (CompoundTag)boostTag;
            final String blockId = ctag.getString(Tags.BOOSTS_BLOCK);
            final Optional<Block> block = RegistryUtil.getBlockOptional(blockId);
            final int blockCount = ctag.getInt(Tags.BOOSTS_COUNT);

            if (block.isPresent()) {
                final BlockBoost blockBoost = new BlockBoost(block.get());
                blockBoost.count = blockCount;
                data.boosts.add(blockBoost);
            }
            else {
                log.warn("Unable to find block with id {} for cell {}; input will be discarded",
                        blockId, plotId);
            }
        });

        return data;
    }
}