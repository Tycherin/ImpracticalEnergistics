package com.tycherin.impen.blockentity.beam;

import java.util.EnumSet;
import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.logic.beam.BeamNetwork;
import com.tycherin.impen.logic.beam.BeamNetworkGridReceiver;

import appeng.api.networking.IGridNode;
import appeng.blockentity.grid.AENetworkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BeamNetworkReceiverBlockEntity extends AENetworkBlockEntity implements BeamNetworkGridReceiver {

    private Optional<BeamNetwork> network = Optional.empty();

    public BeamNetworkReceiverBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.BEAM_NETWORK_RECEIVER.blockEntity(), pos, blockState);

        this.getMainNode()
                .setExposedOnSides(EnumSet.noneOf(Direction.class))
                .setFlags(); // force to not require a channel
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        this.getMainNode().setExposedOnSides(EnumSet.of(inForward.getOpposite()));
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getMainNode().setExposedOnSides(EnumSet.of(this.getForward().getOpposite()));
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        network.ifPresent(BeamNetwork::forceUpdate);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        network.ifPresent(BeamNetwork::forceUpdate);
    }

    @Override
    public boolean canAcceptConnection(final Direction dir) {
        return !this.isRemoved()
                // The node is null specifically during network setup/teardown
                && this.getGridNode() != null
                && dir.equals(this.getForward());
    }

    @Override
    public IGridNode getReceivingGridNode() {
        return this.getGridNode();
    }

    @Override
    public void setNetwork(final Optional<BeamNetwork> networkOpt) {
        this.network = networkOpt;
    }
}
