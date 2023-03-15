package com.tycherin.impen.blockentity.beam;

import java.util.ArrayList;
import java.util.List;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.beam.BeamRenderingBlockEntity.BeamRenderingBaseBlockEntity;
import com.tycherin.impen.config.ImpenConfig;
import com.tycherin.impen.logic.beam.BeamNetworkConnectionHelper;
import com.tycherin.impen.logic.beam.BeamNetworkPhysicalConnection;
import com.tycherin.impen.logic.beam.BeamNetworkReceiver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BeamNetworkSplitterBlockEntity extends BeamRenderingBaseBlockEntity
        implements BeamNetworkReceiver {

    private final int maxDistance;

    public BeamNetworkSplitterBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.BEAM_NETWORK_SPLITTER.blockEntity(), pos, blockState);
        this.maxDistance = ImpenConfig.SETTINGS.beamedNetworkLinkRange();
    }

    @Override
    public boolean canAcceptConnection(final Direction dir) {
        return !this.isRemoved() && dir.equals(this.getForward());
    }

    @Override
    public List<BeamNetworkPhysicalConnection> propagate() {
        final List<BeamNetworkPhysicalConnection> conns = new ArrayList<>();
        // The visual state of this block doesn't distinguish between "up" and the opposite side, so we treat them as
        // equivalent here 
        BeamNetworkConnectionHelper
                .findVisualConnection(this, getBlockPos(), this.getUp(), this.maxDistance, level)
                .ifPresent(conns::add);
        BeamNetworkConnectionHelper
                .findVisualConnection(this, getBlockPos(), this.getUp().getOpposite(), this.maxDistance, level)
                .ifPresent(conns::add);
        return conns;
    }
}
