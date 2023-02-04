package com.tycherin.impen.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class OreBlock extends Block {

    public OreBlock(final BlockBehaviour.Properties props) {
        super(props);
        props.requiresCorrectToolForDrops();
    }

    @Override
    public int getExpDrop(final BlockState state, final LevelReader reader, final BlockPos pos, final int fortune,
            final int silktouch) {
        return silktouch == 0
                ? Mth.nextInt(RANDOM, 2, 5)
                : 0;
    }
}
