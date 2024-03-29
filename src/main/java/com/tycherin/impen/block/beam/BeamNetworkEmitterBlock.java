package com.tycherin.impen.block.beam;

import com.tycherin.impen.blockentity.beam.BeamNetworkEmitterBlockEntity;

import appeng.block.AEBaseEntityBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class BeamNetworkEmitterBlock extends AEBaseEntityBlock<BeamNetworkEmitterBlockEntity> {

    private static final BooleanProperty PROP_IS_ACTIVE = BooleanProperty.create("is_active");
    private static final EnumProperty<Direction> PROP_FORWARD = EnumProperty.create("forward", Direction.class);

    public BeamNetworkEmitterBlock(final BlockBehaviour.Properties props) {
        super(props);
        props.requiresCorrectToolForDrops();

        this.registerDefaultState(this.defaultBlockState()
                .setValue(PROP_IS_ACTIVE, false)
                .setValue(PROP_FORWARD, Direction.NORTH));
    }

    @Override
    public void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder
                .add(PROP_IS_ACTIVE)
                .add(PROP_FORWARD);
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(final BlockState currentState,
            final BeamNetworkEmitterBlockEntity be) {
        return currentState
                .setValue(PROP_IS_ACTIVE, be.isActive())
                .setValue(PROP_FORWARD, be.getForward());
    }
}
