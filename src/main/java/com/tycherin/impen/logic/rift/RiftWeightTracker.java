package com.tycherin.impen.logic.rift;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.RiftCatalystRecipe;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Class for tracking weights of blocks for the SRM system
 * 
 * @author Tycherin
 *
 */
public class RiftWeightTracker {

    public static final double DIMINISHING_RETURNS_RATE = 0.75;
    public static final double WEIGHT_TOTAL_MINIMUM = 100.0;

    private static final Logger LOGGER = LogUtils.getLogger();
    
    /** Creates an IsmWeightWrapper from a given collection of catalyst items */
    public static RiftWeightWrapper weightsFromCatalysts(final Collection<ItemStack> items, final int cycleCount,
            final Level level) {
        final RecipeManager rm = level.getRecipeManager();

        final Map<RiftCatalystRecipe, Integer> recipeCounts = new HashMap<>();
        final Map<Block, Double> runningWeights = new HashMap<>();

        final Set<Block> baseBlocks = new HashSet<>();
        for (final ItemStack is : items) {
            final Optional<RiftCatalystRecipe> recipeOpt = rm.getRecipeFor(
                    ImpenRegistry.RIFT_CATALYST_RECIPE_TYPE.get(), new SimpleContainer(is), level);
            if (recipeOpt.isEmpty()) {
                LOGGER.warn("ItemStack {} has no registered recipes and will be ignored", is);
                continue;
            }
            else {
                final RiftCatalystRecipe recipe = recipeOpt.get();
                for (int i = is.getCount(); i >= 0; i -= cycleCount) {
                    final int oldCount = recipeCounts.containsKey(recipe) ? recipeCounts.get(recipe) : 0;
                    recipeCounts.merge(recipe, 1, Integer::sum);

                    // Multiplier is (newCount - 1) ^ 0.75, which is equivalent to the below code
                    final double multiplier = Math.pow(RiftWeightTracker.DIMINISHING_RETURNS_RATE, oldCount);

                    // If the amount of this stack remaining is less than what we're looking for, then the effect of
                    // this iteration is proportionately reduced
                    final double ratioMultiplier = (i < cycleCount)
                            ? multiplier * ((double) i / cycleCount)
                            : multiplier;
                    recipe.getWeights().forEach(weight -> {
                        runningWeights.merge(weight.block(), weight.probability() * ratioMultiplier, Double::sum);
                    });
                }
                baseBlocks.add(recipe.getBaseBlock());
            }
        }

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

        return new RiftWeightWrapper(aggregateWeights, aggregateWeights, false, aggregateWeights.size() > 1);
    }

    public static record RiftWeightWrapper(List<RiftWeight> displayedWeights, List<RiftWeight> actualWeights,
            boolean isConflict, boolean hasCatalysts) {
        public Supplier<Block> getSupplier() {
            return new RiftBlockSupplier(actualWeights);
        }
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
                cumulativeProbability += (int) weight.probability();
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
