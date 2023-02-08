package com.tycherin.impen.logic.beam;

import appeng.api.networking.IGridNode;

/**
 * A special kind of BeamNetworkNode that receives incoming connections to an AE2 grid
 */
public interface BeamNetworkGridReceiver extends BeamNetworkReceiver {

    IGridNode getReceivingGridNode();
}
