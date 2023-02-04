package com.tycherin.impen.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Sets;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.config.ImpenConfig;
import com.tycherin.impen.logic.SpatialRiftCellDataManager.SpatialRiftCellData;

import appeng.core.definitions.AEBlocks;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SpatialRiftCollapserLogic {

    private static final Random RAND = new Random();

    public void addBlocksToPlot(final SpatialStoragePlot plot, final SpatialRiftCellData data, final Level level) {
        final List<BlockPos> blocksToReplace = getBlocksToReplace(plot);
        final Supplier<Block> blockReplacer = getBlockReplacer(data, blocksToReplace.size(), level);
        final var spatialLevel = SpatialStoragePlotManager.INSTANCE.getLevel();

        blocksToReplace.forEach(blockPos -> {
            spatialLevel.setBlock(blockPos, blockReplacer.get().defaultBlockState(), Block.UPDATE_NONE);
        });
    }

    private List<BlockPos> getBlocksToReplace(final SpatialStoragePlot plot) {
        final var spatialLevel = SpatialStoragePlotManager.INSTANCE.getLevel();

        final BlockPos firstCorner = plot.getOrigin();
        final BlockPos secondCorner = firstCorner.offset(
                plot.getSize().getX() - 1,
                plot.getSize().getY() - 1,
                plot.getSize().getZ() - 1);
        Stream<BlockPos> blocks = BlockPos.betweenClosedStream(firstCorner, secondCorner);

        if (ImpenConfig.SETTINGS.riftOverwriteBlocks()) {
            blocks = blocks.filter(blockPos -> {
                // Overwriting block entities is weird, and I don't want to mess with that
                return spatialLevel.getBlockEntity(blockPos) == null;
            });
        }
        else {
            blocks = blocks.filter(blockPos -> {
                final BlockState bs = spatialLevel.getBlockState(blockPos);
                // Matrix frame blocks are used to fill the empty space in the allocated space, so we overwrite
                // those
                return bs.isAir() || bs.getBlock().equals(AEBlocks.MATRIX_FRAME.block());
            });
        }
        return blocks
                // Need this call to freeze the MutableBlockPos that the iterator returns
                .map(BlockPos::immutable)
                .collect(Collectors.toList());
    }

    private Supplier<Block> getBlockReplacer(final SpatialRiftCellData data, final int numBlocks, final Level level) {
        final Set<Block> replacementBlocks;
        final Block baseBlock;
        final int effectivePrecision;
        if (data.getInputs().size() > 0) {
            replacementBlocks = data.getInputs();
            baseBlock = data.getBaseBlock()
                    // This shouldn't happen, I'm just being paranoid
                    .orElseGet(() -> Blocks.MOSSY_COBBLESTONE);
            effectivePrecision = data.getPrecision(level);
        }
        else {
            // Special case: there are no inputs configured in the cell, so we get to simulate random rift space
            replacementBlocks = Sets.newHashSet(
                    ImpenRegistry.RIFT_SHARD_ORE.asBlock(),
                    ImpenRegistry.RIFTSTONE.asBlock());
            baseBlock = ImpenRegistry.UNSTABLE_RIFTSTONE.asBlock();
            effectivePrecision = 20;
        }

        final double replacementRatio = effectivePrecision / 100.0;
        final List<Block> replacements = new ArrayList<>();
        for (final Block replacementBlock : replacementBlocks) {
            final double randFactor = .2 - RAND.nextDouble(0.2);
            final int replacementCount = (int)(numBlocks * (replacementRatio + randFactor));
            for (int i = 0; i < replacementCount; i++) {
                replacements.add(replacementBlock);
            }
        }

        // Fill in whatever's left with base blocks
        while (replacements.size() < numBlocks) {
            replacements.add(baseBlock);
        }

        // Randomize the order so it doesn't come out all clumped up
        Collections.shuffle(replacements);

        return new SpatialRiftBlockSupplier(replacements);
    }

    private static class SpatialRiftBlockSupplier implements Supplier<Block> {
        private final Block[] blocks;
        private int idx = 0;

        public SpatialRiftBlockSupplier(final List<Block> blocksList) {
            this.blocks = blocksList.toArray(new Block[blocksList.size()]);
        }

        @Override
        public Block get() {
            return blocks[idx++];
        }
    }
}
