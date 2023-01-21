package com.tycherin.impen.client.gui;

import java.util.function.Supplier;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ProgressBar;
import appeng.menu.interfaces.IProgressProvider;
import net.minecraft.network.chat.Component;

/**
 * Everything we need to make a "ProgressBar" act as a fuel bar is configurable, except for the
 * {@code IProgressProvider} interface. This class does some musical chairs with interfaces to work around that problem.
 */
public class FuelBarWidget extends ProgressBar {

    public FuelBarWidget(final IFuelProvider source, final Blitter blitter, final Direction dir) {
        super(new ProgressFaker(source), blitter, dir, null);
    }

    public FuelBarWidget(final IFuelProvider source, final Blitter blitter,
            final Direction dir, final Component title) {
        super(new ProgressFaker(source), blitter, dir, title);
    }

    public static interface IFuelProvider {
        int getCurrentFuel();

        int getMaxFuel();
    }

    private static class ProgressFaker implements IProgressProvider {

        private final Supplier<Integer> getCurrentFuelFunc;
        private final Supplier<Integer> getMaxFuelFunc;

        public ProgressFaker(final IFuelProvider provider) {
            this(() -> provider.getCurrentFuel(), () -> provider.getMaxFuel());
        }

        public ProgressFaker(final Supplier<Integer> getCurrentFuelFunc, final Supplier<Integer> getMaxFuelFunc) {
            this.getCurrentFuelFunc = getCurrentFuelFunc;
            this.getMaxFuelFunc = getMaxFuelFunc;
        }

        @Override
        public int getCurrentProgress() {
            return getCurrentFuelFunc.get();
        }

        @Override
        public int getMaxProgress() {
            return getMaxFuelFunc.get();
        }
    }
}
