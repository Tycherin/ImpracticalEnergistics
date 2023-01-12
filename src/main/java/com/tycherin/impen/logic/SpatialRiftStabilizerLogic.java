package com.tycherin.impen.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.rift.SpatialRiftStabilizerBlockEntity;
import com.tycherin.impen.config.ImpenConfig;
import com.tycherin.impen.recipe.RiftCatalystRecipe;
import com.tycherin.impen.recipe.RiftCatalystRecipeManager;

import appeng.core.definitions.AEBlocks;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SpatialRiftStabilizerLogic {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double WEIGHT_TOTAL_MINIMUM = 100.0;
    private static final double DIMINISHING_RETURNS_RATE = 1.0; // I am unclear if this mechanic is a good idea or not

    private final SpatialRiftStabilizerBlockEntity be;

    public SpatialRiftStabilizerLogic(final SpatialRiftStabilizerBlockEntity be) {
        this.be = be;
    }

    public boolean addBlocksToPlot(final SpatialStoragePlot plot, final Map<Item, Integer> ingredients) {
        final Stream<BlockPos> blocksToReplace = getBlocksToReplace(plot);
        final Supplier<Block> blockReplacer = getBlockReplacer(ingredients);
        final var spatialLevel = SpatialStoragePlotManager.INSTANCE.getLevel();

        blocksToReplace.forEach(blockPos -> {
            spatialLevel.setBlock(blockPos, blockReplacer.get().defaultBlockState(), Block.UPDATE_NONE);
        });

        return true;
    }

    private Stream<BlockPos> getBlocksToReplace(final SpatialStoragePlot plot) {
        final var spatialLevel = SpatialStoragePlotManager.INSTANCE.getLevel();
        final BlockPos startPos = plot.getOrigin();
        final BlockPos endPos = new BlockPos(
                startPos.getX() + plot.getSize().getX() - 1,
                startPos.getY() + plot.getSize().getY() - 1,
                startPos.getZ() + plot.getSize().getZ() - 1);

        final Stream<BlockPos> blocks = BlockPos.betweenClosedStream(startPos, endPos);
        if (ImpenConfig.SETTINGS.riftOverwriteBlocks()) {
            return blocks.filter(blockPos -> {
                // Overwriting block entities is weird, and I don't want to mess with that
                return spatialLevel.getBlockEntity(blockPos) == null;
            });
        }
        else {
            return blocks.filter(blockPos -> {
                final BlockState bs = spatialLevel.getBlockState(blockPos);
                // Matrix frame blocks are used to fill the empty space in the allocated space, so we overwrite
                // those
                return bs.isAir() || bs.getBlock().equals(AEBlocks.MATRIX_FRAME.block());
            });
        }
    }

    private Supplier<Block> getBlockReplacer(final Map<Item, Integer> ingredients) {
        final Map<Block, Double> runningWeights = new HashMap<>();
        final Set<Block> baseBlocks = new HashSet<>();

        ingredients.forEach((item, count) -> {
            final Optional<RiftCatalystRecipe> recipeOpt = RiftCatalystRecipeManager.getRecipe(be.getLevel(),
                    item.getDefaultInstance());
            if (recipeOpt.isEmpty()) {
                LOGGER.warn("No recipe found for stored item {}; item will be discarded", item);
            }
            else {
                final RiftCatalystRecipe recipe = recipeOpt.get();
                for (int i = 0; i < count; i++) {
                    final double multiplier = Math.pow(DIMINISHING_RETURNS_RATE, i);
                    recipe.getWeights().forEach(weight -> {
                        runningWeights.merge(weight.block(), weight.probability() * multiplier, Double::sum);
                    });
                }
                baseBlocks.add(recipe.getBaseBlock());
            }
        });

        final List<SpatialRiftWeight> aggregateWeights = runningWeights.entrySet().stream()
                .map(entry -> new SpatialRiftWeight(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        final double totalWeight = aggregateWeights.stream().mapToDouble(SpatialRiftWeight::probability).sum();
        if (totalWeight < WEIGHT_TOTAL_MINIMUM) {
            final double baseWeight = WEIGHT_TOTAL_MINIMUM - totalWeight;
            baseBlocks.forEach(block -> {
                aggregateWeights.add(new SpatialRiftWeight(block, baseWeight / baseBlocks.size()));
            });
        }

        if (baseBlocks.size() > 1) {
            // Conflicting base blocks leads to exciting things happening
            final double existingWeightMax = Math.max(totalWeight, WEIGHT_TOTAL_MINIMUM);
            final double riftstoneChance = switch (baseBlocks.size()) {
            case 2 -> existingWeightMax / 3;
            case 3 -> existingWeightMax;
            default -> existingWeightMax * 4;
            };
            final double riftOreChance = riftstoneChance / 8;

            aggregateWeights.add(new SpatialRiftWeight(ImpenRegistry.RIFTSTONE.block(), riftstoneChance));
            aggregateWeights.add(new SpatialRiftWeight(ImpenRegistry.RIFT_SHARD_ORE.block(), riftOreChance));
        }

        LOGGER.info("Generated new set of rift weights: {}",
                String.join(", ", aggregateWeights.stream().map(Object::toString).collect(Collectors.toList())));

        return new SpatialRiftBlockReplacer(aggregateWeights);
    }

    public static class SpatialRiftBlockReplacer implements Supplier<Block> {
        private static final Random RAND = new Random();

        private final int[] probabilities;
        private final Block[] blocks;
        private final int totalProbability;

        public SpatialRiftBlockReplacer(final List<SpatialRiftWeight> weights) {
            if (weights.isEmpty()) {
                throw new IllegalArgumentException("Must have at least 1 weight in the weights list");
            }

            this.probabilities = new int[weights.size()];
            this.blocks = new Block[weights.size()];

            int cumulativeProbability = 0;
            for (int i = 0; i < weights.size(); i++) {
                final SpatialRiftWeight weight = weights.get(i);
                cumulativeProbability += (int)weight.probability();
                probabilities[i] = cumulativeProbability;
                blocks[i] = weight.block();
            }

            this.totalProbability = cumulativeProbability;
        }

        @Override
        public Block get() {
            final int diceRoll = RAND.nextInt(totalProbability);
            // It would theoretically be faster to put this in a tree to get O(log n) search times, but these arrays are
            // small enough that O(n) is fine
            for (int i = 0; i < probabilities.length; i++) {
                if (diceRoll > probabilities[i]) {
                    continue;
                }
                return blocks[i];
            }
            // If the list is constructed properly, this should never happen
            throw new RuntimeException("Dice roll not found in probability list, somehow???");
        }
    }
}
