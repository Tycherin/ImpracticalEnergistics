package com.tycherin.impen.client.gui;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.tycherin.impen.logic.ism.IsmStatusCodes;
import com.tycherin.impen.util.GuiComponentFactory;
import com.tycherin.impen.util.GuiComponentFactory.GuiComponentWrapper;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.util.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;

public class ImaginarySpaceManipulatorScreen extends AEBaseScreen<ImaginarySpaceManipulatorMenu> {
    
    private static final GuiComponentWrapper EFFECT_TEXT;
    private static final GuiComponentWrapper EFFECT_MISSING_TEXT;
    private static final GuiComponentWrapper POWER_TEXT;
    private static final Map<Integer, GuiComponentWrapper> STATUS_CODE_TO_TEXT_KEY;
    static {
        final var factory = new GuiComponentFactory("imaginary_space_manipulator");
        EFFECT_TEXT = factory.build("effect_text");
        EFFECT_MISSING_TEXT = factory.build("effect_missing_text");
        POWER_TEXT = factory.build("power_text");
        
        STATUS_CODE_TO_TEXT_KEY = ImmutableMap.<Integer, GuiComponentWrapper>builder()
                .put(IsmStatusCodes.IDLE, factory.build("status.idle"))
                .put(IsmStatusCodes.READY, factory.build("status.ready"))
                .put(IsmStatusCodes.RUNNING, factory.build("status.running"))
                .put(IsmStatusCodes.OUTPUT_FULL, factory.build("status.output_full"))
                .put(IsmStatusCodes.MISSING_CHANNEL, factory.build("status.missing_channel"))
                .put(IsmStatusCodes.NOT_FORMATTED, factory.build("status.not_formatted"))
                .put(IsmStatusCodes.NO_CATALYSTS, factory.build("status.no_catalysts"))
                .put(IsmStatusCodes.UNKNOWN, factory.build("status.unknown"))
                .build();
    }
    
    private final ProgressBar prog;
    
    public ImaginarySpaceManipulatorScreen(final ImaginarySpaceManipulatorMenu menu, final Inventory playerInventory,
            final Component title, final ScreenStyle style) {
        super(menu, playerInventory, title, style);
        
        this.addToLeftToolbar(CommonButtons.togglePowerUnit());
        this.prog = new ProgressBar(this.menu, style.getImage("progressBar"), Direction.VERTICAL);
        this.widgets.add("progressBar", prog);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        if (this.menu.effectName != null) {
            setTextContent("chosen_effect", EFFECT_TEXT.text(this.menu.effectName));
            setTextContent("power_requirement",
                    POWER_TEXT.text(Platform.formatPowerLong(this.menu.getRequiredPower(), true)));
        }
        else {
            setTextContent("chosen_effect", EFFECT_MISSING_TEXT.text());
            setTextContent("power_requirement", new TextComponent(""));
        }
        
        setTextContent("status_text", STATUS_CODE_TO_TEXT_KEY.get(this.menu.statusCode).text());
        
        final int progress = (int)(this.menu.getCurrentProgressPercent() * 100);
        this.prog.setFullMsg(new TextComponent(progress + "%"));
    }
}
