package com.tycherin.impen.logic.rift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.tycherin.impen.logic.rift.SpatialRiftCellData.BlockBoost;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipeManager;
import com.tycherin.impen.util.SpatialRiftUtil;

import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class SpatialRiftCollapserLogic {

    private static final Random RAND = new Random();

    public void addBlocksToPlot(final SpatialStoragePlot plot, final SpatialRiftCellData data, final Level level) {
        final List<BlockPos> blocksToReplace = SpatialRiftUtil.getClearBlocks(plot)
                // Need this call to freeze the MutableBlockPos that the iterator returns
                .map(BlockPos::immutable)
                .collect(Collectors.toList());
        final Supplier<Block> blockReplacer = getBlockReplacer(data, blocksToReplace.size(), level);
        final var spatialLevel = SpatialStoragePlotManager.INSTANCE.getLevel();

        blocksToReplace.forEach(blockPos -> {
            spatialLevel.setBlock(blockPos, blockReplacer.get().defaultBlockState(), Block.UPDATE_NONE);
        });
    }

    private Supplier<Block> getBlockReplacer(final SpatialRiftCellData data, final int numBlocks, final Level level) {
        final var baseRecipe = SpatialRiftManipulatorRecipeManager.getRecipe(level, data.getBaseBlock().get())
                .orElseThrow(() -> {
                    return new IllegalArgumentException("Missing recipe for base block " +
                            data.getBaseBlock().get().getRegistryName().toString() + ", plot " + data.getPlotId());
                });

        // Replacement chance goes down slightly as plot size goes up, to account for the increased ability to add
        // modifiers
        double globalMod = 1.0 - ((1 - data.getTotalSlots()) * .05);
        if (!data.isPlateClean()) {
            // If there are already blocks in the cell, the replacement rate goes down, AND those blocks will
            // effectively reduce the replacement pool
            globalMod *= .5;
        }

        final double richnessMod = switch (data.getRichnessLevel()) {
        case 0 -> 1.0;
        case 1 -> 1.2;
        case 2 -> 1.5;
        case 3 -> 2.0;
        default -> 2.0;
        };

        final double precisionMod = switch (data.getPrecisionLevel()) {
        case 0 -> 1.0;
        case 1 -> 0.9;
        case 2 -> 0.6;
        case 3 -> 0.2;
        default -> 0.2;
        };

        final double multiblockMod = switch (data.getBoosts().size()) {
        case 0 -> 1.0;
        case 1 -> 0.9;
        case 2 -> 0.7;
        case 3 -> 0.6;
        default -> 0.5;
        };

        final double baselineReplacementRate = globalMod * richnessMod * precisionMod * multiblockMod;
        final double boostedReplacementRate = globalMod * richnessMod;

        final Map<Block, Integer> boostSet = data.getBoosts().stream()
                .collect(Collectors.toMap(BlockBoost::getBlock, BlockBoost::getCount));

        final List<Block> replacementBlocks = new ArrayList<>();
        baseRecipe.getBaseWeights().forEach((block, weight) -> {
            // One weight here = 1 block per 4000 input blocks
            final double convertedWeight = weight / 4000.0;
            final double randFactor = .2 - RAND.nextDouble(0.2);

            final double rateModifier;
            if (boostSet.containsKey(block)) {
                final double boostRate = switch (boostSet.get(block)) {
                case 0 -> 1.0;
                case 1 -> 1.5;
                case 2 -> 2.2;
                case 3 -> 3.0;
                default -> 3.0;
                };
                rateModifier = boostedReplacementRate * boostRate;
            }
            else {
                rateModifier = baselineReplacementRate;
            }

            final double aggregateRate = convertedWeight * randFactor * rateModifier;
            final int blockCount = (int)(aggregateRate * numBlocks);

            for (int i = 0; i < blockCount; i++) {
                replacementBlocks.add(block);
            }
        });

        // Fill in whatever's left with base blocks
        while (replacementBlocks.size() < numBlocks) {
            replacementBlocks.add(baseRecipe.getBaseBlock());
        }

        // Randomize the order so it doesn't come out all clumped up
        Collections.shuffle(replacementBlocks);

        return new SpatialRiftBlockSupplier(replacementBlocks);
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
