package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.AtmosphericCrystallizerBlockEntity;

import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.AppEngSlot;
import lombok.Getter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class AtmosphericCrystallizerMenu extends UpgradeableMenu<AtmosphericCrystallizerBlockEntity>
        implements IProgressProvider {

    public static final MenuType<AtmosphericCrystallizerMenu> TYPE = MenuTypeBuilder
            .create(AtmosphericCrystallizerMenu::new, AtmosphericCrystallizerBlockEntity.class)
            .build("atmospheric_crystallizer");

    @GuiSync(10)
    @Getter
    public int currentProgress;
    @GuiSync(11)
    @Getter
    public int maxProgress;

    public AtmosphericCrystallizerMenu(final int id, final Inventory playerInv, final AtmosphericCrystallizerBlockEntity be) {
        super(TYPE, id, playerInv, be);

        // It would be nice to have this slot display the item that would be produced if there's nothing present, but
        // AppEngSlot hardcodes the set of possible icons to use, so that's a no-go
        this.addSlot(new AppEngSlot(be.getInternalInventory(), 0), SlotSemantics.MACHINE_OUTPUT);

        this.maxProgress = be.getMaxProgress();
    }

    @Override
    public void broadcastChanges() {
        this.currentProgress = ((AtmosphericCrystallizerBlockEntity) this.getBlockEntity()).getProgress();
        super.broadcastChanges();
    }
}
