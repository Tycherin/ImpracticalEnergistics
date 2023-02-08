package com.tycherin.impen.logic.beam;

import java.util.Optional;

public interface BeamNetworkNode {

    /**
     * @param networkOpt Network to join. The node should call the network to update whenever anything interesting
     *                   happens (typically, being removed). If empty, the node will be removed from the network.
     */
    void setNetwork(Optional<BeamNetwork> networkOpt);
}
