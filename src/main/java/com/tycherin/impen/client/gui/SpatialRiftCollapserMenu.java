package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.rift.SpatialRiftCollapserBlockEntity;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.interfaces.IProgressProvider;
import lombok.Getter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class SpatialRiftCollapserMenu extends AEBaseMenu implements IProgressProvider {

    public static final MenuType<SpatialRiftCollapserMenu> TYPE = MenuTypeBuilder
            .create(SpatialRiftCollapserMenu::new, SpatialRiftCollapserBlockEntity.class)
            .build("spatial_rift_collapser");

    @GuiSync(10)
    @Getter
    public int currentProgress;
    @GuiSync(11)
    @Getter
    public int maxProgress;

    public SpatialRiftCollapserMenu(final int id, final Inventory playerInv, final SpatialRiftCollapserBlockEntity be) {
        super(TYPE, id, playerInv, be);

        this.addSlot(new FilteredInputSlot(be.getInventoryFilter(), be.getInternalInventory(), 0),
                SlotSemantics.MACHINE_INPUT);
        this.addSlot(new FilteredInputSlot(be.getInventoryFilter(), be.getInternalInventory(), 1),
                SlotSemantics.MACHINE_OUTPUT);

        this.maxProgress = be.getMaxProgress();

        this.createPlayerInventorySlots(playerInv);
    }

    @Override
    public void broadcastChanges() {
        this.currentProgress = ((SpatialRiftCollapserBlockEntity)this.getBlockEntity()).getProgress();
        super.broadcastChanges();
    }
}
