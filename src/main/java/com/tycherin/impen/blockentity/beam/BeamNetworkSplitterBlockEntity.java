package com.tycherin.impen.blockentity.beam;

import java.util.ArrayList;
import java.util.List;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.beam.BeamRenderingBlockEntity.BeamRenderingBaseBlockEntity;
import com.tycherin.impen.logic.beam.BeamNetworkConnectionHelper;
import com.tycherin.impen.logic.beam.BeamNetworkPhysicalConnection;
import com.tycherin.impen.logic.beam.BeamNetworkReceiver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BeamNetworkSplitterBlockEntity extends BeamRenderingBaseBlockEntity
        implements BeamNetworkReceiver {

    private static final int MAX_DISTANCE = 16;

    public BeamNetworkSplitterBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.BEAM_NETWORK_SPLITTER.blockEntity(), pos, blockState);
    }

    @Override
    public boolean canAcceptConnection(final Direction dir) {
        return dir.equals(this.getForward());
    }

    @Override
    public List<BeamNetworkPhysicalConnection> propagate() {
        final var leftConn = BeamNetworkConnectionHelper.findLinearConnection(this, getBlockPos(),
                getLeft(), MAX_DISTANCE, level);
        final var rightConn = BeamNetworkConnectionHelper.findLinearConnection(this, getBlockPos(),
                getRight(), MAX_DISTANCE, level);
        final List<BeamNetworkPhysicalConnection> conns = new ArrayList<>();
        leftConn.ifPresent(conns::add);
        rightConn.ifPresent(conns::add);
        return conns;
    }

    private Direction getLeft() {
        return this.getForward().getCounterClockWise();
    }

    private Direction getRight() {
        return this.getForward().getClockWise();
    }
}
