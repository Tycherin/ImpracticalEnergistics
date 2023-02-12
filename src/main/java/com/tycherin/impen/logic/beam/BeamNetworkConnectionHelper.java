package com.tycherin.impen.logic.beam;

import java.util.Optional;

import com.tycherin.impen.logic.beam.BeamNetworkPhysicalConnection.BeamNetworkInWorldConnection;

import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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
    
    /**
     * Finds the next hop from an originating node by doing a visual raytrace between points
     * 
     * @param originator  Originating node
     * @param startingPos Position of originating node
     * @param facing      Facing of originating node
     * @param maxDistance Maximum allowed distance for the hop
     * @param level       It's a level
     * @return The next hop if a valid one is found, otherwise empty
     */
    public static Optional<BeamNetworkPhysicalConnection> findVisualConnection(final BeamNetworkPropagator sender,
            final BlockPos senderPos, final Direction facing, final int maxDistance, final Level level) {
        
        // Do a raytrace from the sender block to the furthest possible target
        
        // To prevent the raytrace from intersecting the sender block itself, we need to nudge the starting point
        // towards the facing direction
        final double startX = senderPos.getX() + 0.5 + (0.5 * facing.getStepX());
        final double startY = senderPos.getY() + 0.5 + (0.5 * facing.getStepY());
        final double startZ = senderPos.getZ() + 0.5 + (0.5 * facing.getStepZ());
        final Vec3 startPos = new Vec3(startX, startY, startZ);
        final Vec3 endPos = Vec3.atCenterOf(senderPos.relative(facing, maxDistance));
        
        final var ctx = new ClipContext(startPos, endPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, null);
        final BlockHitResult hitResult = level.clip(ctx);
        if (hitResult.getType().equals(HitResult.Type.BLOCK)) {
            final var be = level.getExistingBlockEntity(hitResult.getBlockPos());
            if (be != null && be instanceof BeamNetworkReceiver target) {
                // We have a network link; now we need to see if the connection is allowed
                if (target.canAcceptConnection(facing.getOpposite())) {
                    final int distance = senderPos.distManhattan(hitResult.getBlockPos());
                    final var conn = new BeamNetworkInWorldConnection(sender, target, senderPos,
                            hitResult.getBlockPos(), distance, facing);
                    return Optional.of(conn);
                }
                else {
                    // We found a link, but it doesn't accept a connection in this direction. We could keep searching,
                    // but that would lead to some very counterintuitive behavior, so we stop instead.
                    return Optional.empty();
                }
            }
            else {
                // We hit a block, but it isn't a valid target
                return Optional.empty();
            }
        }
        else {
            // If we didn't hit a block, then this is a miss
            return Optional.empty();
        }
    }
}
