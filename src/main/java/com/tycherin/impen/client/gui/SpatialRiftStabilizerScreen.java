package com.tycherin.impen.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SpatialRiftStabilizerScreen extends AEBaseScreen<SpatialRiftStabilizerMenu> {

    public SpatialRiftStabilizerScreen(final SpatialRiftStabilizerMenu menu, final Inventory playerInventory,
            final Component title, final ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
    }
}
