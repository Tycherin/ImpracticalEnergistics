package com.tycherin.impen.logic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.logic.SpatialRiftCellDataManager.SpatialRiftCellData;
import com.tycherin.impen.util.TagUtil;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.tags.ITag;

public class SpatialRiftCellCalculator {

    public static final SpatialRiftCellCalculator INSTANCE = new SpatialRiftCellCalculator();

    public static record SpatialRiftCellCalculatorResult(Optional<Block> baseBlock, int precision) {
    }

    private final ITag<Block> stoneOresTag = TagUtil.getBlockTag(Tags.Blocks.ORES_IN_GROUND_STONE);
    private final ITag<Block> deepslateOresTag = TagUtil.getBlockTag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE);
    private final ITag<Block> netherrackOresTag = TagUtil.getBlockTag(Tags.Blocks.ORES_IN_GROUND_NETHERRACK);
    private final ITag<Block> allOresTag = TagUtil.getBlockTag(Tags.Blocks.ORES);

    private final Map<Block, BaseBlockCompatibility> compatibilityCache = new HashMap<>();

    private static enum BaseBlockCompatibility {
        STONE,
        DEEPSLATE,
        NETHERRACK,
        RIFTSTONE,
        DIRT
    }

    private Set<BaseBlockCompatibility> getBaseBlocks(final Collection<Block> blocks) {
        return blocks.stream()
                .map(this::getCompatibility)
                .distinct()
                .collect(Collectors.toSet());
    }

    private BaseBlockCompatibility getCompatibility(final Block block) {
        if (compatibilityCache.containsKey(block)) {
            return compatibilityCache.get(block);
        }

        // First step: if the ore is nicely tagged, we can rely on that
        if (stoneOresTag.contains(block)) {
            return BaseBlockCompatibility.STONE;
        }
        else if (deepslateOresTag.contains(block)) {
            return BaseBlockCompatibility.DEEPSLATE;
        }
        else if (netherrackOresTag.contains(block)) {
            return BaseBlockCompatibility.NETHERRACK;
        }
        // Not a tag, but maybe it should be??? (probably not)
        else if (ImpenRegistry.RIFT_SHARD_ORE.asBlock().equals(block)) {
            return BaseBlockCompatibility.RIFTSTONE;
        }

        // Block isn't tagged nicely, so we have to get creative

        if (allOresTag.contains(block)) {
            // Probably an ore, just not tagged cleanly
            final String blockName = block.getRegistryName().toString();
            if (blockName.contains("deepslate")) {
                return BaseBlockCompatibility.DEEPSLATE;
            }
            else if (blockName.contains("nether")) {
                return BaseBlockCompatibility.NETHERRACK;
            }
            else {
                return BaseBlockCompatibility.STONE;
            }
        }
        else {
            // Probably not an ore
            return BaseBlockCompatibility.DIRT;
        }
    }

    public SpatialRiftCellCalculatorResult calculate(final SpatialRiftCellData data) {
        final Set<Block> inputs = data.getInputs();

        if (inputs.isEmpty()) {
            return new SpatialRiftCellCalculatorResult(Optional.empty(), -1);
        }

        final Set<BaseBlockCompatibility> validBases = getBaseBlocks(inputs);

        final Block baseBlock;
        final boolean isConflict;
        if (validBases.size() > 1) {
            isConflict = true;
            // Conflict - there are multiple possible base stones
            // TODO Switch to Unstable Riftstone
            baseBlock = Blocks.END_STONE;

        }
        else {
            isConflict = false;
            baseBlock = switch (validBases.iterator().next()) {
            case STONE -> Blocks.STONE;
            case DEEPSLATE -> Blocks.DEEPSLATE;
            case NETHERRACK -> Blocks.NETHERRACK;
            case RIFTSTONE -> ImpenRegistry.RIFTSTONE.asBlock();
            case DIRT -> Blocks.DIRT;
            };
        }

        final int scale = data.getMaxInputCount();
        final double scaleFactor = switch (scale) {
        case 1 -> 1.0;
        case 2 -> 1.1;
        case 3 -> 1.2;
        case 4 -> 1.25;
        case 5 -> 1.30;
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
