package com.tycherin.impen.logic.beam;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BeamNetwork {

    private final BeamNetworkGridPropagator origin;

    private final BeamNetworkMap networkMap = new BeamNetworkMap();

    private int storedColor = 0;
    private int updateTimer = 10;

    public void update() {
        if (updateTimer-- > 0) {
            return;
        }

        try {
            final BeamNetworkMap updatedMap = this.getLatestMap();

            // We need to manipulate these sets in specific ways to A) retain references to the elements that were
            // removed, and B) avoid ConcurrentModificationExceptions

            // Nodes need to be updated before connections get added, or else we get race conditions where nodes receive
            // connections while their network is still blank

            // Update nodes
            final List<BeamNetworkNode> addedNodes = new ArrayList<>();
            for (final BeamNetworkNode node : updatedMap.getNodes()) {
                if (!this.networkMap.getNodes().contains(node)) {
                    addedNodes.add(node);
                }
            }
            addedNodes.forEach(node -> {
                node.setNetwork(Optional.of(this));
                this.networkMap.getNodes().add(node);
            });

            final List<BeamNetworkNode> removedNodes = new ArrayList<>();
            for (final BeamNetworkNode node : this.networkMap.getNodes()) {
                if (!updatedMap.getNodes().contains(node)) {
                    removedNodes.add(node);
                }
            }
            removedNodes.forEach(node -> {
                node.setNetwork(Optional.empty());
                this.networkMap.getNodes().remove(node);
            });

            // Update connections
            final List<BeamNetworkConnection> addedConns = new ArrayList<>();
            for (final BeamNetworkConnection conn : updatedMap.getConnections()) {
                if (!this.networkMap.getConnections().contains(conn)) {
                    addedConns.add(conn);
                }
            }
            addedConns.forEach(conn -> {
                conn.activate();
                this.networkMap.getConnections().add(conn);
            });

            final List<BeamNetworkConnection> removedConns = new ArrayList<>();
            for (final BeamNetworkConnection conn : this.networkMap.getConnections()) {
                if (!updatedMap.getConnections().contains(conn)) {
                    removedConns.add(conn);
                }
            }
            removedConns.forEach(conn -> {
                conn.deactivate();
                this.networkMap.getConnections().remove(conn);
            });

            // Update power cost
            this.networkMap.recomputePowerCost();

            // Update the beam color, if needed
            if (this.storedColor != this.origin.getBeamColor()) {
                this.storedColor = this.origin.getBeamColor();

                // New connections will get the updated color automatically, but existing ones need to be updated
                this.networkMap.getNodes().forEach(node -> {
                    if (node instanceof BeamNetworkPropagator propagator) {
                        propagator.setColor(this.storedColor);
                    }
                });
            }

            this.updateTimer = 10;
        }
        catch (final RuntimeException e) {
            // If anything goes wrong, we want to try to avoid leaving polluted state behind
            log.error("Encountered error while updating beam network; network will be cleared", e);
            this.reset();
        }
    }

    public void reset() {
        this.networkMap.getConnections().forEach(BeamNetworkConnection::deactivate);
        this.networkMap.getNodes().forEach(node -> node.setNetwork(Optional.empty()));
        this.networkMap.getConnections().clear();
        this.networkMap.getNodes().clear();
        this.networkMap.recomputePowerCost();
    }

    public int getPowerCost() {
        return networkMap.getPowerCost();
    }

    public int getColor() {
        return this.storedColor;
    }

    public void forceUpdate() {
        this.updateTimer = -1;
    }

    private BeamNetworkMap getLatestMap() {
        final BeamNetworkMap map = new BeamNetworkMap();

        // Build the list of physical connections by propagating out from the origin
        final Queue<BeamNetworkPropagator> sourceQueue = new ArrayDeque<>();
        sourceQueue.add(this.origin);

        final Set<BeamNetworkPhysicalConnection> physicalConnections = new HashSet<>();
        while (!sourceQueue.isEmpty()) {
            final BeamNetworkPropagator source = sourceQueue.poll();
            final List<BeamNetworkPhysicalConnection> newConns = source.propagate();
            newConns.forEach(conn -> {
                physicalConnections.add(conn);
                // If we've linked to a destination that is also a propagator, add that to the list of things to
                // discover (this won't go infinite as long as there are no loops)
                if (conn.getReceiver() instanceof BeamNetworkPropagator propagator) {
                    sourceQueue.add(propagator);
                }
            });
            map.getNodes().add(source);
        }
        map.getConnections().addAll(physicalConnections);

        // Now use the set of physical connections to determine which grid connections should exist
        final Set<BeamNetworkGridReceiver> destinations = new HashSet<>();
        physicalConnections.forEach(conn -> {
            final BeamNetworkReceiver receiver = conn.getReceiver();
            map.getNodes().add(receiver);

            if (receiver instanceof BeamNetworkGridReceiver dest) {
                if (!destinations.add(dest)) {
                    // TL;DR - This shouldn't happen due to the physical network topology
                    //
                    // In order to trigger duplicates, one destination would have to receive multiple input signals.
                    // That would require one of two things:
                    // A) A receiver that can accept inputs from multiple directions (which is a bug)
                    // B) Multiple parallel beams overlapping each other (which is a bug)
                    log.warn("Received duplicate request to add {} to network map", dest);
                }
            }
        });
        final Set<BeamNetworkGridConnection> gridConnections = destinations.stream()
                .map(dest -> new BeamNetworkGridConnection(this.origin, dest))
                .collect(Collectors.toSet());
        map.getConnections().addAll(gridConnections);

        return map;
    }

}
