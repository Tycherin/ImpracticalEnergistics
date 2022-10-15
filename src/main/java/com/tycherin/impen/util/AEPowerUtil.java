package com.tycherin.impen.util;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.me.helpers.IGridConnectedBlockEntity;

public class AEPowerUtil {

    private static final double FLOATING_POINT_MATH_HELPER = 0.0000001;

    public static boolean drawPower(final IGridConnectedBlockEntity be, final double powerToDraw) {
        if (powerToDraw == 0) {
            return true;
        }

        final var energySvc = be.getMainNode().getGrid().getEnergyService();
        final var projectedPowerDraw = energySvc.extractAEPower(powerToDraw, Actionable.SIMULATE,
                PowerMultiplier.CONFIG);
        if (powerToDraw - projectedPowerDraw < FLOATING_POINT_MATH_HELPER) {
            energySvc.extractAEPower(powerToDraw, Actionable.MODULATE, PowerMultiplier.CONFIG);
            return true;
        }
        else {
            return false;
        }
    }
}
