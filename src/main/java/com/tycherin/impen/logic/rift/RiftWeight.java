package com.tycherin.impen.logic.rift;

import net.minecraft.world.level.block.Block;

public record RiftWeight(Block block, double probability) {
    @Override
    public String toString() {
        return String.format("%s @ %s", block.toString(), probability);
    }
}
