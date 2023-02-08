package com.tycherin.impen.logic.beam;

import net.minecraft.core.Direction;

public interface BeamNetworkReceiver extends BeamNetworkNode {

    boolean canAcceptConnection(Direction dir);
}
