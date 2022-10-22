package com.tycherin.impen.client.gui;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.tycherin.impen.logic.rift.RiftManipulatorStatusCodes;
import com.tycherin.impen.util.GuiComponentFactory;
import com.tycherin.impen.util.GuiComponentFactory.GuiComponentWrapper;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.util.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;

public class SpatialRiftManipulatorScreen extends UpgradeableScreen<SpatialRiftManipulatorMenu> {

    private static final GuiComponentWrapper POWER_TEXT;
    private static final Map<Integer, GuiComponentWrapper> STATUS_CODE_TO_TEXT_KEY;
    static {
        final var factory = new GuiComponentFactory("spatial_rift_manipulator");
        POWER_TEXT = factory.build("power_text");

        STATUS_CODE_TO_TEXT_KEY = ImmutableMap.<Integer, GuiComponentWrapper>builder()
                .put(RiftManipulatorStatusCodes.IDLE, factory.build("status.idle"))
                .put(RiftManipulatorStatusCodes.READY, factory.build("status.ready"))
                .put(RiftManipulatorStatusCodes.RUNNING, factory.build("status.running"))
                .put(RiftManipulatorStatusCodes.OUTPUT_FULL, factory.build("status.output_full"))
                .put(RiftManipulatorStatusCodes.MISSING_CHANNEL, factory.build("status.missing_channel"))
                .put(RiftManipulatorStatusCodes.NOT_FORMATTED, factory.build("status.not_formatted"))
                .put(RiftManipulatorStatusCodes.NO_CATALYSTS, factory.build("status.no_catalysts"))
                .put(RiftManipulatorStatusCodes.CELL_FULL, factory.build("status.cell_full"))
                .put(RiftManipulatorStatusCodes.UNKNOWN, factory.build("status.unknown"))
                .build();
    }

    private final ProgressBar prog;

    public SpatialRiftManipulatorScreen(final SpatialRiftManipulatorMenu menu, final Inventory playerInventory,
            final Component title, final ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.addToLeftToolbar(CommonButtons.togglePowerUnit());
        this.prog = new ProgressBar(this.menu, style.getImage("progressBar"), Direction.VERTICAL);
        this.widgets.add("progressBar", prog);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        setTextContent("power_requirement",
                POWER_TEXT.text(Platform.formatPowerLong(this.menu.getRequiredPower(), true)));
        setTextContent("status_text", STATUS_CODE_TO_TEXT_KEY.get(this.menu.statusCode).text());

        final int progress = (int) (this.menu.getCurrentProgressPercent() * 100);
        this.prog.setFullMsg(new TextComponent(progress + "%"));
    }
}
