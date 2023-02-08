package com.tycherin.impen.logic.beam;

import java.util.Optional;

import com.tycherin.impen.logic.beam.BeamNetworkPhysicalConnection.BeamNetworkInWorldConnection;

import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

@UtilityClass
public class BeamNetworkConnectionHelper {
    /**
     * Finds the next hop from an originating node by traveling in a straight line
     * 
     * @param originator  Originating node
     * @param startingPos Position of originating node
     * @param facing      Facing of originating node
     * @param maxDistance Maximum allowed distance for the hop
     * @param level       It's a level
     * @return The next hop if a valid one is found, otherwise empty
     */
    public static Optional<BeamNetworkPhysicalConnection> findLinearConnection(final BeamNetworkPropagator sender,
            final BlockPos startingPos, final Direction facing, final int maxDistance, final Level level) {

        BeamNetworkReceiver target = null;
        BlockPos targetPos = null;
        int distance = 0;
        BlockPos candidatePos = startingPos;
        while (distance < maxDistance) {
            candidatePos = candidatePos.relative(facing);
            distance++;

            final var be = level.getExistingBlockEntity(candidatePos);
            if (be != null && be instanceof BeamNetworkReceiver candidate) {
                // We have a network link; now we need to see if the connection is allowed
                if (candidate.canAcceptConnection(facing.getOpposite())) {
                    target = candidate;
                    targetPos = be.getBlockPos();
                    break;
                }
                else {
                    // We found a link, but it doesn't accept a connection in this direction. We could keep searching,
                    // but that would lead to some very counterintuitive behavior, so we stop instead.
                    break;
                }
            }
            else {
                if (level.getBlockState(candidatePos).isViewBlocking(level, startingPos)) {
                    // This is an opaque/blocking block, so the beam can't go any further
                    break;
                }
                else {
                    continue;
                }
            }
        }

        if (target == null) {
            // Ran past the max distance, so there's nothing in range
            return Optional.empty();
        }

        return Optional.of(new BeamNetworkInWorldConnection(sender, target, startingPos, targetPos, distance, facing));
    }
}
