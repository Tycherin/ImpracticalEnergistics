package com.tycherin.impen.logic.beam;

import org.apache.commons.lang3.NotImplementedException;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@Getter
public abstract class BeamNetworkPhysicalConnection extends BeamNetworkConnection {

    private final BlockPos senderPos;
    private final BlockPos receiverPos;

    public BeamNetworkPhysicalConnection(final BeamNetworkPropagator sender, final BeamNetworkReceiver receiver,
            final BlockPos senderPos, final BlockPos receiverPos) {
        super(sender, receiver);
        this.senderPos = senderPos;
        this.receiverPos = receiverPos;
    }

    @Getter
    public static class BeamNetworkInWorldConnection extends BeamNetworkPhysicalConnection {
        private final int distanceBetween;
        private final Direction direction;

        public BeamNetworkInWorldConnection(final BeamNetworkPropagator sender, final BeamNetworkReceiver receiver,
                final BlockPos senderPos, final BlockPos receiverPos, final int distanceBetween,
                final Direction direction) {
            super(sender, receiver, senderPos, receiverPos);
            this.distanceBetween = distanceBetween;
            this.direction = direction;
        }

        @Override
        public void onActivate() {
            sender.renderConnection(this);
        }

        @Override
        public void onDeactivate() {
            sender.stopRenderConnection(this);
        }

        @Override
        public int getPowerCost() {
            return distanceBetween;
        }
    }

    // TODO Implement this
    public static class BeamNetworkInterdimensionalConnection extends BeamNetworkPhysicalConnection {
        public BeamNetworkInterdimensionalConnection(final BeamNetworkPropagator sender,
                final BeamNetworkReceiver receiver, final BlockPos senderPos, final BlockPos receiverPos) {
            super(sender, receiver, senderPos, receiverPos);
        }

        @Override
        public void onActivate() {
            throw new NotImplementedException();
        }

        @Override
        public void onDeactivate() {
            throw new NotImplementedException();
        }

        @Override
        public int getPowerCost() {
            throw new NotImplementedException();
        }
    }
}
