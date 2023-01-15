package com.tycherin.impen.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.config.ImpenConfig;

import appeng.core.definitions.AEBlocks;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SpatialRiftCollapserLogic {

    private static final Random RAND = new Random();

    public SpatialRiftCollapserLogic() {
    }

    public void addBlocksToPlot(final SpatialStoragePlot plot, final Map<Block, Integer> blockWeights) {
        final List<BlockPos> blocksToReplace = getBlocksToReplace(plot);
        final Supplier<Block> blockReplacer = getBlockReplacer(blockWeights, blocksToReplace.size());
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

    private Supplier<Block> getBlockReplacer(final Map<Block, Integer> weights, final int numBlocks) {
        final Set<Block> baseBlocks = new HashSet<>();
        
        // TODO Think about how to re-implement base blocks
        baseBlocks.add(Blocks.STONE);

        final Block baseBlock;
        final double riftAccidentProbability = switch (baseBlocks.size()) {
        case 0 -> throw new RuntimeException("Must have at least one base block");
        case 1 -> 0.0;
        case 2 -> 0.2;
        case 3 -> 0.4;
        case 4 -> 0.8;
        default -> 1;
        };
        if (RAND.nextDouble() < riftAccidentProbability) {
            baseBlock = ImpenRegistry.RIFTSTONE.asBlock();
        }
        else {
            baseBlock = new ArrayList<>(baseBlocks).get(RAND.nextInt(baseBlocks.size()));
        }

        int totalWeight = weights.values().stream().mapToInt(i -> i).sum();

        // So here's the plan:
        // 1. Roll a number between 0 and max(totalWeight, 100)
        // 2. If it matches a block, increment the block count and decrement the block probability
        // 3. If it doesn't match a block, increment the base block count
        // This is slightly inefficient, but we're doing in-memory operations on small arrays, so it's still fast
        final int numBlockTypes = weights.size() + 1;
        final Block[] blockIndexes = new Block[numBlockTypes];
        final int[] blockProbabilities = new int[numBlockTypes];
        final int[] blockCounts = new int[numBlockTypes];
        final AtomicInteger streamsDontHaveCounters = new AtomicInteger(); // sigh
        weights.forEach((block, probability) -> {
            final int idx = streamsDontHaveCounters.incrementAndGet() - 1;
            blockIndexes[idx] = block;
            blockProbabilities[idx] = probability;
            blockCounts[idx] = 0;
        });
        blockIndexes[numBlockTypes - 1] = baseBlock;
        blockProbabilities[numBlockTypes - 1] = Integer.MAX_VALUE;
        blockCounts[numBlockTypes - 1] = 0;

        for (int i = 0; i < numBlocks; i++) {
            int r = RAND.nextInt(Math.max(totalWeight, 100));

            // The reason we don't need to do bounds checking on this next bit is that MAX_VALUE is so much larger than
            // anything we care about that we can math around with it and not worry about the consequences
            int idx = 0;
            while (r > blockProbabilities[idx]) {
                r -= blockProbabilities[idx];
                idx++;
            }

            blockProbabilities[idx]--;
            blockCounts[idx]++;
            totalWeight--;
        }

        final List<Block> blocksToGenerate = new ArrayList<>(numBlocks);
        for (int i = 0; i < numBlockTypes; i++) {
            for (int j = 0; j < blockCounts[i]; j++) {
                blocksToGenerate.add(blockIndexes[i]);
            }
        }
        Collections.shuffle(blocksToGenerate);

        return new SpatialRiftBlockSupplier(blocksToGenerate);
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
