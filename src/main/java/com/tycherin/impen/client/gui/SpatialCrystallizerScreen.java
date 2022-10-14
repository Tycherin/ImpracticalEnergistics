package com.tycherin.impen.client.gui;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;

public class SpatialCrystallizerScreen extends UpgradeableScreen<SpatialCrystallizerMenu> {

    private final ProgressBar prog;

    public SpatialCrystallizerScreen(final SpatialCrystallizerMenu menu, final Inventory playerInventory,
            final Component title, final ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.prog = new ProgressBar(this.menu, style.getImage("progressBar"), Direction.VERTICAL);
        this.widgets.add("progressBar", prog);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        final int progress = this.menu.getCurrentProgress() * 100 / this.menu.getMaxProgress();
        this.prog.setFullMsg(new TextComponent(progress + "%"));
    }
}
