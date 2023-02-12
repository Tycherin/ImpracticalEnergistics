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
        /** Number of blocks between the target and the destination, excluding both */
        private final int distance;
        /** Direction of the beam relative to the emitting block */
        private final Direction direction;

        public BeamNetworkInWorldConnection(final BeamNetworkPropagator sender, final BeamNetworkReceiver receiver,
                final BlockPos senderPos, final BlockPos receiverPos, final int distance,
                final Direction direction) {
            super(sender, receiver, senderPos, receiverPos);
            this.distance = distance;
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
            return distance;
        }

        @Override
        public String toString() {
            return "world:" + super.toString();
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
