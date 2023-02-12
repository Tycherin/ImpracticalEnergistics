package com.tycherin.impen.logic.beam;

import java.util.List;

import com.tycherin.impen.logic.beam.BeamNetworkPhysicalConnection.BeamNetworkInWorldConnection;

/**
 * A node that propagates the network
 */
public interface BeamNetworkPropagator extends BeamNetworkNode {

    /**
     * @return The nodes that this node propagates to, or empty if there aren't any
     */
    List<BeamNetworkPhysicalConnection> propagate();

    void setColor(int color);

    void renderConnection(BeamNetworkInWorldConnection conn);

    void stopRenderConnection(BeamNetworkInWorldConnection conn);
}
