package com.tycherin.impen.blockentity.beam;

import java.util.Collections;
import java.util.List;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.beam.BeamRenderingBlockEntity.BeamRenderingBaseBlockEntity;
import com.tycherin.impen.logic.beam.BeamNetworkConnectionHelper;
import com.tycherin.impen.logic.beam.BeamNetworkPhysicalConnection;
import com.tycherin.impen.logic.beam.BeamNetworkReceiver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BeamNetworkAmplifierBlockEntity extends BeamRenderingBaseBlockEntity
        implements BeamNetworkReceiver {

    private static final int MAX_DISTANCE = 32;

    public BeamNetworkAmplifierBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.BEAM_NETWORK_AMPLIFIER.blockEntity(), pos, blockState);
    }

    @Override
    public boolean canAcceptConnection(final Direction dir) {
        return !this.isRemoved() && dir.equals(this.getForward());
    }

    @Override
    public List<BeamNetworkPhysicalConnection> propagate() {
        return BeamNetworkConnectionHelper
                .findVisualConnection(this, getBlockPos(), getForward().getOpposite(), MAX_DISTANCE, level)
                .map(List::of)
                .orElseGet(Collections::emptyList);
    }
}
