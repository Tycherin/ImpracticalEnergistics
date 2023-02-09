package com.tycherin.impen.block.beam;

import com.tycherin.impen.blockentity.beam.BeamNetworkMirrorBlockEntity;

import appeng.block.AEBaseEntityBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class BeamNetworkMirrorBlock extends AEBaseEntityBlock<BeamNetworkMirrorBlockEntity> {

    private static final EnumProperty<Direction> PROP_FORWARD = EnumProperty.create("forward", Direction.class);
    private static final EnumProperty<Direction> PROP_UP = EnumProperty.create("up", Direction.class);

    public BeamNetworkMirrorBlock(final BlockBehaviour.Properties props) {
        super(props);
        props.requiresCorrectToolForDrops();

        this.registerDefaultState(this.defaultBlockState()
                .setValue(PROP_FORWARD, Direction.NORTH)
                .setValue(PROP_UP, Direction.UP));
    }

    @Override
    public void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder
                .add(PROP_FORWARD)
                .add(PROP_UP);
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(final BlockState currentState,
            final BeamNetworkMirrorBlockEntity be) {
        return currentState
                .setValue(PROP_FORWARD, be.getForward())
                .setValue(PROP_UP, be.getUp());
    }
}