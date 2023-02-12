package com.tycherin.impen.logic.beam;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class BeamNetworkConnection {
    protected final BeamNetworkPropagator sender;
    protected final BeamNetworkReceiver receiver;

    private boolean active = false;

    public final void activate() {
        if (!this.active) {
            this.onActivate();
            this.active = true;
        }
    }

    public final void deactivate() {
        if (this.active) {
            this.onDeactivate();
            this.active = false;
        }
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof BeamNetworkConnection otherConn) {
            return this.toString().equals(otherConn.toString());
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return sender.toString() + "->" + receiver.toString();
    }

    public abstract void onActivate();

    public abstract void onDeactivate();

    public abstract int getPowerCost();
}