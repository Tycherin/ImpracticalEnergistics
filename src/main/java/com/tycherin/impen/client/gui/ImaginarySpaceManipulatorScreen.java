package com.tycherin.impen.client.gui;

import com.tycherin.impen.util.GuiComponentFactory;
import com.tycherin.impen.util.GuiComponentFactory.GuiComponentWrapper;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.core.localization.GuiText;
import appeng.util.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;

public class ImaginarySpaceManipulatorScreen extends AEBaseScreen<ImaginarySpaceManipulatorMenu> {
    
    private static final GuiComponentWrapper EFFECT_TEXT;
    private static final GuiComponentWrapper EFFECT_MISSING_TEXT;
    private static final GuiComponentWrapper POWER_TEXT;
    static {
        final var factory = new GuiComponentFactory("imaginary_space_manipulator");
        EFFECT_TEXT = factory.build("effect_text");
        EFFECT_MISSING_TEXT = factory.build("effect_missing_text");
        POWER_TEXT = factory.build("power_text");
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

        final Component scsSizeText;
        if (this.menu.xSize != 0 && this.menu.ySize != 0 && this.menu.zSize != 0) {
            scsSizeText = GuiText.SCSSize.text(this.menu.xSize, this.menu.ySize, this.menu.zSize);
        } else {
            scsSizeText = GuiText.SCSInvalid.text();
        }
        setTextContent("scs_size", scsSizeText);
        
        final int progress = (int)(this.menu.getCurrentProgressPercent() * 100);
        this.prog.setFullMsg(new TextComponent(progress + "%"));
    }
}
