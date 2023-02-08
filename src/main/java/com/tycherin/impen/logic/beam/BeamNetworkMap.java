package com.tycherin.impen.logic.beam;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public class BeamNetworkMap {

    private final Set<BeamNetworkConnection> connections;
    private final Set<BeamNetworkNode> nodes;
    private final int totalPowerCost;

    public static BeamNetworkMap empty() {
        return new BeamNetworkMap(Collections.emptySet(), Collections.emptySet(), 0);
    }

    public static BeamNetworkMap from(final BeamNetwork network) {
        final Set<BeamNetworkNode> nodes = new HashSet<>();

        // Build the list of physical connections by propagating out from the origin
        final BeamNetworkGridPropagator origin = network.getOrigin();
        final Queue<BeamNetworkPropagator> sources = new ArrayDeque<>();
        sources.add(origin);

        final Set<BeamNetworkPhysicalConnection> physicalConnections = new HashSet<>();
        while (!sources.isEmpty()) {
            final BeamNetworkPropagator source = sources.poll();
            physicalConnections.addAll(source.propagate());
            nodes.add(source);
        }

        // Now identify network connections
        final Set<BeamNetworkGridReceiver> destinations = new HashSet<>();
        physicalConnections.forEach(conn -> {
            final BeamNetworkReceiver receiver = conn.getReceiver();
            nodes.add(receiver);

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
                .map(dest -> new BeamNetworkGridConnection(origin, dest))
                .collect(Collectors.toSet());

        final Set<BeamNetworkConnection> allConnections = new HashSet<>();
        allConnections.addAll(physicalConnections);
        allConnections.addAll(gridConnections);

        final int totalPowerCost = allConnections.stream()
                .mapToInt(BeamNetworkConnection::getPowerCost)
                .sum();

        return new BeamNetworkMap(allConnections, nodes, totalPowerCost);
    }

    public static BeamNetworkMapDiff diff(final BeamNetworkMap before, final BeamNetworkMap after) {
        // Use streams instead of Set diffing to avoid mutating the sets themselves
        final List<BeamNetworkConnection> addedConnections = after.getConnections().stream()
                .filter(conn -> !before.getConnections().contains(conn))
                .collect(Collectors.toList());
        final List<BeamNetworkConnection> removedConnections = before.getConnections().stream()
                .filter(conn -> !after.getConnections().contains(conn))
                .collect(Collectors.toList());

        final List<BeamNetworkNode> addedNodes = after.getNodes().stream()
                .filter(node -> !before.getNodes().contains(node))
                .collect(Collectors.toList());
        final List<BeamNetworkNode> removedNodes = before.getNodes().stream()
                .filter(node -> !after.getNodes().contains(node))
                .collect(Collectors.toList());

        return new BeamNetworkMapDiff(addedConnections, removedConnections, addedNodes, removedNodes);
    }

    public static record BeamNetworkMapDiff(
            List<BeamNetworkConnection> addedConnections,
            List<BeamNetworkConnection> removedConnections,
            List<BeamNetworkNode> addedNodes,
            List<BeamNetworkNode> removedNodes) {
    }
}
