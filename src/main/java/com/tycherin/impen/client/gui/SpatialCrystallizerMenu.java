package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.SpatialCrystallizerBlockEntity;

import appeng.api.config.SecurityPermissions;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class SpatialCrystallizerMenu extends AEBaseMenu implements IProgressProvider {
    
    public static final MenuType<SpatialCrystallizerMenu> TYPE = MenuTypeBuilder
            .create(SpatialCrystallizerMenu::new, SpatialCrystallizerBlockEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("spatialcrystallizer");

    @GuiSync(0)
    public int progress;
    @GuiSync(1)
    public int maxProgress;
    
    public SpatialCrystallizerMenu(final int id, final Inventory playerInv, final SpatialCrystallizerBlockEntity be) {
        super(TYPE, id, playerInv, be);

        // It would be nice to have this slot display the item that would be produced if there's nothing present, but
        // AppEngSlot hardcodes the set of possible icons to use, so that's a no-go
        this.addSlot(new AppEngSlot(be.getInternalInventory(), 0), SlotSemantics.MACHINE_OUTPUT);
        
        this.maxProgress = be.getMaxProgress();

        this.createPlayerInventorySlots(playerInv);
    }

    @Override
    public void broadcastChanges() {
        this.progress = ((SpatialCrystallizerBlockEntity) this.getBlockEntity()).getProgress();
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
