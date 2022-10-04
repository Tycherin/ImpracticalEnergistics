package com.tycherin.impen.logic.ism;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

import net.minecraft.world.level.block.Block;

/**
 * Class for tracking weights of blocks for the ISM system
 * 
 * @author Tycherin
 *
 */
public class IsmWeightTracker {

    private static final int BASELINE_WEIGHT = 1000;
    private static final double DIMINISHING_RETURNS_PCT = 0.05;
    private static final double DIMINISHING_RETURNS_FACTOR = 1.5;

    private final Block baseBlock;
    private final Map<String, IsmWeightProvider> providers;
    private final Map<Block, Integer> weights;
    private final Random rand = new Random();

    private boolean needsUpdate = false;;
    private int totalWeight = 0;
    private BlockSupplier blockSupplier;

    public IsmWeightTracker(final Block baseBlock, final List<Block> weightBlocks) {
        this.baseBlock = baseBlock;
        this.weights = new HashMap<>();
        weights.put(baseBlock, 0);
        weightBlocks.forEach(wb -> weights.put(wb, 0));
        this.providers = new HashMap<>();
        this.update();
    }

    /** Adds a provider to this tracker */
    public void add(final IsmWeightProvider provider) {
        if (providers.containsKey(provider.getId())) {
            throw new RuntimeException(String.format("Provider %s already exists", provider.getId()));
        }
        providers.put(provider.getId(), provider);
        this.needsUpdate = true;
    }

    /** Removes a provider from this tracker */
    public void remove(final IsmWeightProvider provider) {
        if (providers.remove(provider.getId()) != null) {
            this.needsUpdate = true;
        }
        else {
            throw new RuntimeException(
                    String.format("Could not remove provider %s because it was not present", provider.getId()));
        }
    }

    /**
     * Gets the current weights table, suitable for display purposes
     * Note that this can change every tick, so be careful
     */
    public Map<Block, Integer> getWeights() {
        return ImmutableMap.copyOf(weights);
    }

    /** Updates the state of this tracker only if one of the components has changed */
    public void updateIfNeeded() {
        if (this.needsUpdate ||
                this.providers.values().stream().anyMatch(IsmWeightProvider::needsUpdate)) {
            this.update();
            this.providers.values().forEach(IsmWeightProvider::markUpdateSuccessful);
        }
    }

    /**
     * Returns a random block picker based on the weights in this tracker
     * The returned supplier is threadsafe, I think
     */
    public Supplier<Block> getSupplier() {
        return this.blockSupplier;
    }

    private void update() {
        // Reset weights to zero
        weights.replaceAll((block, weight) -> 0);
        weights.put(baseBlock, BASELINE_WEIGHT);

        // Re-sum weights from providers
        providers.values().forEach(provider -> {
            final Collection<IsmWeight> providerWeights = provider.getWeights();
            providerWeights.forEach(weight -> {
                weights.merge(weight.block(), weight.weight(), Integer::sum);
            });
        });

        // Recalculate the total weight, taking diminishing returns into account

        // Sum of all the individual weights being added
        final int addedWeight = weights.values().stream().mapToInt(i -> i).sum();
        // Amount of weight that exceeds the diminishing returns threshold
        final int weightOverDimRet = Math.max(addedWeight - (int) (BASELINE_WEIGHT * DIMINISHING_RETURNS_PCT), 0);
        if (weightOverDimRet > 0) {
            // Weight below the threshold is not subject to DR; weight over is
            this.totalWeight = BASELINE_WEIGHT + (addedWeight - weightOverDimRet)
                    + (int) Math.pow(weightOverDimRet, DIMINISHING_RETURNS_FACTOR);
        }
        else {
            // Otherwise, weights are applied normally
            this.totalWeight = BASELINE_WEIGHT + addedWeight;
        }

        // Create a structure containing the probabilities of getting each block
        final int numEntries = weights.size() + 1;
        final int[] probabilities = new int[numEntries];
        final Block[] blocks = new Block[numEntries];

        int index = 0;
        int cumulativeProb = 0;

        for (final Entry<Block, Integer> entry : weights.entrySet()) {
            cumulativeProb += entry.getValue();
            probabilities[index] = cumulativeProb;
            blocks[index] = entry.getKey();
            index++;
        }
        probabilities[numEntries - 1] = Integer.MAX_VALUE;
        blocks[numEntries - 1] = this.baseBlock;

        this.blockSupplier = new BlockSupplier(probabilities, blocks, this.totalWeight, this.rand);

        this.needsUpdate = false;
    }

    private static class BlockSupplier implements Supplier<Block> {

        private final int[] probabilities;
        private final Block[] blocks;
        private final int totalWeight;
        private final Random rand;

        public BlockSupplier(final int[] probabilities, final Block[] blocks, final int totalWeight,
                final Random rand) {
            if (probabilities.length != blocks.length) {
                throw new IllegalArgumentException(
                        String.format("Array lengths do not match: %s vs %s", probabilities.length, blocks.length));
            }
            this.probabilities = probabilities;
            this.blocks = blocks;
            this.totalWeight = totalWeight;
            this.rand = rand;
        }

        @Override
        public Block get() {
            final int diceRoll = rand.nextInt(totalWeight);
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
