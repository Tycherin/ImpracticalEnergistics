package com.tycherin.impen.logic.ism;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Class for tracking weights of blocks for the ISM system
 * 
 * @author Tycherin
 *
 */
public class IsmWeightTracker {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<String, Item> catalystsByProvider = new HashMap<>();
    private final Map<Item, Integer> catalystCounts = new HashMap<>();
    private final Map<Block, Double> runningWeights = new HashMap<>();

    private IsmWeightWrapper wrapper;

    public IsmWeightTracker() {
        this.runningWeights.put(Blocks.STONE, 100.0); // TODO Switch this over to recipe system
    }

    public void addProvider(final IsmCatalystProvider provider) {
        if (provider.getCatalyst().isPresent()) {
            catalystsByProvider.put(provider.getId(), provider.getCatalyst().get());
            this.incrementCatalyst(provider.getCatalyst().get());
            this.rebuildWeightWrapper();
        }
    }

    public void removeProvider(final IsmCatalystProvider provider) {
        final var previousCatalyst = catalystsByProvider.get(provider.getId());
        if (previousCatalyst != null) {
            catalystsByProvider.remove(provider.getId());
            this.decrementCatalyst(previousCatalyst);
            this.rebuildWeightWrapper();
        }
    }

    public void updateProvider(final IsmCatalystProvider provider) {
        final var previousCatalyst = catalystsByProvider.get(provider.getId());
        final var newCatalyst = provider.getCatalyst();

        if (previousCatalyst == null) {
            if (newCatalyst.isEmpty()) {
                return;
            }
            else {
                this.addProvider(provider);
            }
        }
        else {
            if (newCatalyst.isEmpty()) {
                this.removeProvider(provider);
            }
            else {
                if (previousCatalyst.equals(newCatalyst.get())) {
                    return;
                }
                else {
                    this.decrementCatalyst(previousCatalyst);
                    this.incrementCatalyst(newCatalyst.get());
                    catalystsByProvider.put(provider.getId(), newCatalyst.get());
                    this.rebuildWeightWrapper();
                }
            }
        }
    }

    public IsmWeightWrapper getWeights() {
        return wrapper;
    }

    private void incrementCatalyst(final Item catalyst) {
        final int oldCount = catalystCounts.containsKey(catalyst) ? catalystCounts.get(catalyst) : 0;
        catalystCounts.merge(catalyst, 1, Integer::sum);

        // Multiplier is (newCount - 1) ^ 0.75, which is equivalent to the below code
        final double multiplier = Math.pow(IsmCatalyst.DIMINISHING_RETURNS_RATE, oldCount);
        IsmCatalyst.getWeights(catalyst).forEach(weight -> {
            runningWeights.merge(weight.block(), weight.probability() * multiplier, Double::sum);
        });
        LOGGER.info("Incremented catalyst {}, oldCount={}, multiplier={}", catalyst, oldCount, multiplier);
    }

    private void decrementCatalyst(final Item catalyst) {
        final int oldCount = catalystCounts.get(catalyst);
        catalystCounts.merge(catalyst, -1, Integer::sum);
        if (catalystCounts.get(catalyst) == 0) {
            catalystCounts.remove(catalyst);
        }

        // Use oldCount - 1 here because we want to undo using the previous multiplier, not the next one
        final double multiplier = Math.pow(IsmCatalyst.DIMINISHING_RETURNS_RATE, oldCount - 1);
        IsmCatalyst.getWeights(catalyst).forEach(weight -> {
            runningWeights.merge(weight.block(), -(weight.probability() * multiplier), Double::sum);
        });
        LOGGER.info("Decremented catalyst {}, oldCount={}, multiplier={}", catalyst, oldCount, multiplier);
    }

    private void rebuildWeightWrapper() {
        final List<IsmWeight> actualWeights = runningWeights.entrySet().stream()
                .map(entry -> new IsmWeight(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        // TODO Implement conflict detection
        this.wrapper = new IsmWeightWrapper(actualWeights, actualWeights, false);
        LOGGER.info("Weights have updated! New weights: {}",
                String.join(", ", actualWeights.stream().map(Object::toString).collect(Collectors.toList())));
    }

    public static record IsmWeightWrapper(List<IsmWeight> displayedWeights, List<IsmWeight> actualWeights,
            boolean isConflict) {
        public Supplier<Block> getSupplier() {
            return new IsmBlockSupplier(actualWeights);
        }
    }

    public static class IsmBlockSupplier implements Supplier<Block> {
        private static final Random RAND = new Random();

        private final int[] probabilities;
        private final Block[] blocks;
        private final int totalProbability;

        public IsmBlockSupplier(final List<IsmWeight> weights) {
            if (weights.isEmpty()) {
                throw new IllegalArgumentException("Must have at least 1 weight in the weights list");
            }

            this.probabilities = new int[weights.size()];
            this.blocks = new Block[weights.size()];

            int cumulativeProbability = 0;
            for (int i = 0; i < weights.size(); i++) {
                final IsmWeight weight = weights.get(i);
                cumulativeProbability += (int) weight.probability();
                probabilities[i] = cumulativeProbability;
                blocks[i] = weight.block();
            }

            this.totalProbability = cumulativeProbability;
            LOGGER.info("Constructed IsmBlockSupplier. probabilities=({}), blocks=({}), totalProb={}",
                    Arrays.toString(probabilities), Arrays.toString(blocks), totalProbability);
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
