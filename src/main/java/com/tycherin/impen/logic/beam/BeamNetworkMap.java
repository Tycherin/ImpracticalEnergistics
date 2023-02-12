package com.tycherin.impen.logic.beam;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

@Getter
public class BeamNetworkMap {

    private final Set<BeamNetworkConnection> connections;
    private final Set<BeamNetworkNode> nodes;
    
    private int powerCost = 0;

    public BeamNetworkMap() {
        this.connections = new HashSet<>();
        this.nodes = new HashSet<>();
    }

    public void recomputePowerCost() {
        this.powerCost = this.connections.stream()
                .mapToInt(BeamNetworkConnection::getPowerCost)
                .sum();
    }

    public int getPowerCost() {
        return this.powerCost;
    }
}
