package com.tycherin.impen.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;

public class SpatialRiftSpawnerScreen extends AEBaseScreen<SpatialRiftSpawnerMenu> {

    private final ProgressBar prog;
    private final FuelBarWidget fuel;

    public SpatialRiftSpawnerScreen(final SpatialRiftSpawnerMenu menu, final Inventory playerInventory,
            final Component title, final ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.prog = new ProgressBar(this.menu, style.getImage("progressBar"), Direction.VERTICAL);
        this.fuel = new FuelBarWidget(this.menu, style.getImage("fuelBar"), Direction.VERTICAL);
        this.widgets.add("progressBar", prog);
        this.widgets.add("fuelBar", fuel);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        final int progress = this.menu.getCurrentProgress() * 100 / this.menu.getMaxProgress();
        this.prog.setFullMsg(new TextComponent(progress + "%"));
        this.fuel.setFullMsg(new TextComponent(this.menu.getCurrentFuel() + " Aerofuel"));
    }
}
