package com.tycherin.impen.block.beam;

import com.tycherin.impen.blockentity.beam.BeamNetworkReceiverBlockEntity;

import appeng.block.AEBaseEntityBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class BeamNetworkReceiverBlock extends AEBaseEntityBlock<BeamNetworkReceiverBlockEntity> {
    private static final EnumProperty<Direction> PROP_FACING = EnumProperty.create("facing", Direction.class);

    public BeamNetworkReceiverBlock(final BlockBehaviour.Properties props) {
        super(props);
        props.requiresCorrectToolForDrops();

        this.registerDefaultState(this.defaultBlockState()
                .setValue(PROP_FACING, Direction.NORTH));
    }

    @Override
    public void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder
                .add(PROP_FACING);
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(final BlockState currentState,
            final BeamNetworkReceiverBlockEntity be) {
        return currentState
                .setValue(PROP_FACING, be.getForward());
    }
}