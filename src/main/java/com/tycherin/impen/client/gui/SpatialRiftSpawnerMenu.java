package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.rift.SpatialRiftSpawnerBlockEntity;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.interfaces.IProgressProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class SpatialRiftSpawnerMenu extends AEBaseMenu implements IProgressProvider {

    public static final MenuType<SpatialRiftSpawnerMenu> TYPE = MenuTypeBuilder
            .create(SpatialRiftSpawnerMenu::new, SpatialRiftSpawnerBlockEntity.class)
            .build("spatial_rift_spawner");

    @GuiSync(10)
    public int progress;
    @GuiSync(11)
    public int maxProgress;

    public SpatialRiftSpawnerMenu(final int id, final Inventory playerInv, final SpatialRiftSpawnerBlockEntity be) {
        super(TYPE, id, playerInv, be);

        this.addSlot(new FilteredInputSlot(be.getInventoryFilter(), be.getInternalInventory(), 0),
                SlotSemantics.MACHINE_INPUT);
        this.addSlot(new FilteredInputSlot(be.getInventoryFilter(), be.getInternalInventory(), 1),
                SlotSemantics.MACHINE_OUTPUT);
        // TODO Implement fuel

        this.maxProgress = be.getMaxProgress();

        this.createPlayerInventorySlots(playerInv);
    }

    @Override
    public void broadcastChanges() {
        this.progress = ((SpatialRiftSpawnerBlockEntity)this.getBlockEntity()).getProgress();
        super.broadcastChanges();
    }

    @Override
    public int getCurrentProgress() {
        return this.progress;
    }

    @Override
    public int getMaxProgress() {
        return this.maxProgress;
    }
}
