package com.tycherin.impen.logic;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.logic.SpatialRiftCellDataManager.SpatialRiftCellData;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipeManager;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class SpatialRiftCellCalculator {

    public static final SpatialRiftCellCalculator INSTANCE = new SpatialRiftCellCalculator();

    private static final Logger LOGGER = LogUtils.getLogger();

    public static record SpatialRiftCellCalculatorResult(Optional<Block> baseBlock, int precision) {
    }

    public SpatialRiftCellCalculatorResult calculate(final Level level, final SpatialRiftCellData data) {
        final Set<Block> inputs = data.getInputs();
        if (inputs.isEmpty()) {
            return new SpatialRiftCellCalculatorResult(Optional.empty(), -1);
        }

        final Set<Block> baseBlocks = new HashSet<>();
        for (final Block inputBlock : inputs) {
            final var recipeOpt = SpatialRiftManipulatorRecipeManager.getRecipe(level, inputBlock);
            if (recipeOpt.isPresent()) {
                baseBlocks.add(recipeOpt.get().getBaseBlock());
            }
            else {
                LOGGER.warn("No recipe found for block {}; input will be discarded", inputBlock);
            }
        }

        final Block baseBlock;
        final boolean isConflict;
        if (baseBlocks.size() > 1) {
            isConflict = true;
            baseBlock = ImpenRegistry.UNSTABLE_RIFTSTONE.asBlock();
        }
        else {
            isConflict = false;
            baseBlock = baseBlocks.iterator().next();
        }

        final int scale = data.getMaxSlots();
        final double scaleFactor = switch (scale) {
        case 1 -> 1.0;
        case 2 -> 1.1;
        case 3 -> 1.2;
        case 4 -> 1.25;
        case 5 -> 1.3;
        case 6 -> 1.35;
        case 7 -> 1.5;
        case 8 -> 1.75;
        default -> 1.0;
        };

        double precision = (inputs.size() / (scale + 1)) * scaleFactor;
        precision += (data.getBonusPrecision() / (scale * 1.0));

        // Cap precision per input at 50
        if ((precision / inputs.size()) > 50) {
            precision = inputs.size() * 50;
        }

        // Cap overall precision at 80
        if (precision > 80) {
            precision = 80;
        }

        // Conflict penalty is applied after other bonuses to prevent shenanigans
        if (isConflict) {
            precision = precision / 4;
        }

        // Check to make sure we haven't fallen too low
        if (precision < 10) {
            precision = 10;
        }

        return new SpatialRiftCellCalculatorResult(Optional.of(baseBlock), (int)precision);
    }
}
