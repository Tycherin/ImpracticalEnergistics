package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.SpatialRiftStabilizerBlockEntity;

import appeng.api.config.SecurityPermissions;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class SpatialRiftStabilizerMenu extends AEBaseMenu {
    
    public static final MenuType<SpatialRiftStabilizerMenu> TYPE = MenuTypeBuilder
            .create(SpatialRiftStabilizerMenu::new, SpatialRiftStabilizerBlockEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("spatial_rift_stabilizer");

    private int delay = 40;

    public SpatialRiftStabilizerMenu(final int id, final Inventory playerInv, final SpatialRiftStabilizerBlockEntity be) {
        super(TYPE, id, playerInv, be);

        this.addSlot(new AppEngSlot(be.getCatalystInventory(), 0), SlotSemantics.PROCESSING_INPUTS);
        
        for (int i = 0; i < 4; i++) {
            this.addSlot(new AppEngSlot(be.getInputInventory(), i), SlotSemantics.MACHINE_INPUT);
        }

        this.createPlayerInventorySlots(playerInv);
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (this.isServerSide()) {
            this.delay++;

            var be = (SpatialRiftStabilizerBlockEntity) this.getBlockEntity();
            var gridNode = be.getGridNode();
            var grid = gridNode != null ? gridNode.getGrid() : null;

            if (grid != null && this.delay > 15) {
                this.delay = 0;
            }
        }

        super.broadcastChanges();
    }
}
