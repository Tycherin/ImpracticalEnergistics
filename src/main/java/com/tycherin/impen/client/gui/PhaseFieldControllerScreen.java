package com.tycherin.impen.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class PhaseFieldControllerScreen extends AEBaseScreen<PhaseFieldControllerMenu> {

    public PhaseFieldControllerScreen(final PhaseFieldControllerMenu menu, final Inventory playerInventory,
            final Component title, final ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
    }

    @Override
    protected void slotClicked(Slot slot, int slotIndex, int p_97780_, ClickType clickType) {
        // Middle clicking does weird things on the PFC item slots, so I'm just suppressing that
        if (clickType.equals(ClickType.CLONE)) {
            return;
        }
        else {
            super.slotClicked(slot, slotIndex, p_97780_, clickType);
        }
    }
}
