package com.tycherin.impen.logic.rift;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.RiftCatalystRecipe;

import net.minecraft.world.level.block.Block;

/**
 * Class for tracking weights of blocks for the SRM system
 * 
 * @author Tycherin
 *
 */
public record RiftWeightTracker(List<RiftWeight> displayedWeights, List<RiftWeight> actualWeights,
        boolean isConflict, boolean hasCatalysts) {

    public static final double DIMINISHING_RETURNS_RATE = 1.0; // I am unclear if this mechanic is a good idea or not
    public static final double WEIGHT_TOTAL_MINIMUM = 100.0;

    private static final Logger LOGGER = LogUtils.getLogger();

    public static RiftWeightTracker fromRecipeCounts(final Map<RiftCatalystRecipe, Integer> recipeCounts) {
        final Map<Block, Double> runningWeights = new HashMap<>();
        final Set<Block> baseBlocks = new HashSet<>();

        recipeCounts.forEach((recipe, count) -> {
            for (int i = 0; i < count; i++) {
                final double multiplier = Math.pow(RiftWeightTracker.DIMINISHING_RETURNS_RATE, i);
                recipe.getWeights().forEach(weight -> {
                    runningWeights.merge(weight.block(), weight.probability() * multiplier, Double::sum);
                });
            }
            baseBlocks.add(recipe.getBaseBlock());
        });

        final List<RiftWeight> aggregateWeights = runningWeights.entrySet().stream()
                .map(entry -> new RiftWeight(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        final double totalWeight = aggregateWeights.stream().mapToDouble(RiftWeight::probability).sum();
        if (totalWeight < WEIGHT_TOTAL_MINIMUM) {
            final double baseWeight = WEIGHT_TOTAL_MINIMUM - totalWeight;
            baseBlocks.forEach(block -> {
                aggregateWeights.add(new RiftWeight(block, baseWeight / baseBlocks.size()));
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

            aggregateWeights.add(new RiftWeight(ImpenRegistry.RIFTSTONE.block(), riftstoneChance));
            aggregateWeights.add(new RiftWeight(ImpenRegistry.RIFT_SHARD_ORE.block(), riftOreChance));
        }

        LOGGER.info("Generated new set of rift weights: {}",
                String.join(", ", aggregateWeights.stream().map(Object::toString).collect(Collectors.toList())));

        return new RiftWeightTracker(aggregateWeights, aggregateWeights, false, aggregateWeights.size() > 1);
    }

    public Supplier<Block> getSupplier() {
        return new RiftBlockSupplier(actualWeights);
    }

    public static class RiftBlockSupplier implements Supplier<Block> {
        private static final Random RAND = new Random();

        private final int[] probabilities;
        private final Block[] blocks;
        private final int totalProbability;

        public RiftBlockSupplier(final List<RiftWeight> weights) {
            if (weights.isEmpty()) {
                throw new IllegalArgumentException("Must have at least 1 weight in the weights list");
            }

            this.probabilities = new int[weights.size()];
            this.blocks = new Block[weights.size()];

            int cumulativeProbability = 0;
            for (int i = 0; i < weights.size(); i++) {
                final RiftWeight weight = weights.get(i);
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
