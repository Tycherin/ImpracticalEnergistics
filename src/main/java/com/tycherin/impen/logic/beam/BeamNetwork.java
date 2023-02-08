package com.tycherin.impen.logic.beam;

import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BeamNetwork {

    @Getter
    private final BeamNetworkGridPropagator origin;

    private BeamNetworkMap oldMap = BeamNetworkMap.empty();

    public void update() {
        final BeamNetworkMap newMap = BeamNetworkMap.from(this);
        final var diff = BeamNetworkMap.diff(oldMap, newMap);
        diff.removedConnections().forEach(BeamNetworkConnection::deactivate);
        diff.addedConnections().forEach(BeamNetworkConnection::activate);
        diff.removedNodes().forEach(node -> node.setNetwork(Optional.empty()));
        diff.addedNodes().forEach(node -> node.setNetwork(Optional.of(this)));
    }

    public void destroy() {
        oldMap.getConnections().forEach(BeamNetworkConnection::deactivate);
        oldMap.getNodes().forEach(node -> node.setNetwork(Optional.empty()));
    }

    public int getPowerCost() {
        return oldMap.getTotalPowerCost();
    }

    public int getColor() {
        return origin.getColor();
    }
}
