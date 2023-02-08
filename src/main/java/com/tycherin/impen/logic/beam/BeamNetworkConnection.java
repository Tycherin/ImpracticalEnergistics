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
        if (this.active) {
            throw new IllegalStateException("Connection " + this.toString() + " is already active");
        }
        this.onActivate();
        this.active = true;
    }

    public final void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("Connection " + this.toString() + " is already inactive");
        }
        this.onDeactivate();
        this.active = false;
    }

    public abstract void onActivate();

    public abstract void onDeactivate();

    public abstract int getPowerCost();
}