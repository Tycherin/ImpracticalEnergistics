package com.tycherin.impen.logic.ism;

import net.minecraft.world.level.block.Block;

public record IsmWeight(Block block, double probability) {
    @Override
    public String toString() {
        return String.format("%s @ %s", block.toString(), probability);
    }
}
