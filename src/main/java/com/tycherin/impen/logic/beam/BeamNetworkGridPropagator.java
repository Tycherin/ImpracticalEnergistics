package com.tycherin.impen.logic.beam;

import appeng.api.networking.IGridNode;

/**
 * A special kind of BeamNetworkPropagator that supplies a connection to an AE2 grid
 */
public interface BeamNetworkGridPropagator extends BeamNetworkPropagator {
    
    IGridNode getSourceGridNode();
    
    int getColor();
}
