package com.tycherin.impen.logic.beam;

import java.util.Optional;

import appeng.api.exceptions.ExistingConnectionException;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.exceptions.SecurityConnectionException;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BeamNetworkGridConnection extends BeamNetworkConnection {

    // These are technically overlapping with the equivalent fields in the superclass, but we need to save them
    // separately to capture the more specific subclasses
    private final BeamNetworkGridPropagator gridSource;
    private final BeamNetworkGridReceiver gridDestination;

    private Optional<IGridConnection> gridConnection;

    public BeamNetworkGridConnection(final BeamNetworkGridPropagator sender, final BeamNetworkGridReceiver receiver) {
        super(sender, receiver);
        this.gridSource = sender;
        this.gridDestination = receiver;
    }

    @Override
    public void onActivate() {
        final IGridNode sourceNode = gridSource.getSourceGridNode();
        final IGridNode destinationNode = gridDestination.getReceivingGridNode();

        if (sourceNode == null || destinationNode == null) {
            // This *shouldn't* happen, but weird stuff happens during BE lifecycles (e.g. chunk loading/unloading.
            // Rather than hard fail, we log a warning and skip the connection, since it's probably invalid anyway.
            log.warn("Attempted to create a connection with null grid nodes; connection will be ignored");
            this.gridConnection = Optional.empty();
        }

        try {
            this.gridConnection = Optional.of(GridHelper.createGridConnection(sourceNode, destinationNode));
        }
        catch (final ExistingConnectionException | SecurityConnectionException e) {
            // Either:
            // 1) The two nodes are already connected, e.g. by being adjacent to each other
            // 2) The two nodes are both part of a network already, and those networks have different security settings
            //
            // Either way, this is now a no-op
            this.gridConnection = Optional.empty();
        }
        catch (final FailedConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDeactivate() {
        this.gridConnection.ifPresent(IGridConnection::destroy);
    }

    @Override
    public int getPowerCost() {
        return 0;
    }

    @Override
    public String toString() {
        return "grid:" + super.toString();
    }
}
