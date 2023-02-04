package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.rift.SpatialRiftManipulatorBlockEntity;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.interfaces.IProgressProvider;
import lombok.Getter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class SpatialRiftManipulatorMenu extends AEBaseMenu implements IProgressProvider {

    public static final MenuType<SpatialRiftManipulatorMenu> TYPE = MenuTypeBuilder
            .create(SpatialRiftManipulatorMenu::new, SpatialRiftManipulatorBlockEntity.class)
            .build("spatial_rift_manipulator");

    @GuiSync(10)
    @Getter
    public int currentProgress;
    @GuiSync(11)
    @Getter
    public int maxProgress;

    public SpatialRiftManipulatorMenu(final int id, final Inventory playerInv,
            final SpatialRiftManipulatorBlockEntity be) {
        super(TYPE, id, playerInv, be);

        this.addSlot(new FilteredInputSlot(be.getInventoryFilter(), be.getInternalInventory(), 0),
                SlotSemantics.MACHINE_INPUT);
        this.addSlot(new FilteredInputSlot(be.getInventoryFilter(), be.getInternalInventory(), 1),
                SlotSemantics.INSCRIBER_PLATE_BOTTOM); // Sigh
        this.addSlot(new FilteredInputSlot(be.getInventoryFilter(), be.getInternalInventory(), 2),
                SlotSemantics.MACHINE_OUTPUT);

        this.maxProgress = be.getMaxProgress();

        this.createPlayerInventorySlots(playerInv);
    }

    @Override
    public void broadcastChanges() {
        this.currentProgress = ((SpatialRiftManipulatorBlockEntity)this.getBlockEntity()).getProgress();
        super.broadcastChanges();
    }
}
