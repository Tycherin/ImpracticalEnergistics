package com.tycherin.impen.logic;

import net.minecraft.world.level.block.Block;

public record SpatialRiftWeight(Block block, int blockCount) {
    @Override
    public String toString() {
        return String.format("%s @ %s", block.toString(), blockCount);
    }
}
