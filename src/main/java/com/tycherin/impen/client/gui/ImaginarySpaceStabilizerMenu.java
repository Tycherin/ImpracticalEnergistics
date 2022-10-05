package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.ImaginarySpaceStabilizerBlockEntity;
import com.tycherin.impen.logic.ism.IsmCatalyst;

import appeng.api.config.SecurityPermissions;
import appeng.api.inventories.InternalInventory;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ImaginarySpaceStabilizerMenu extends AEBaseMenu {
    
    public static final MenuType<ImaginarySpaceStabilizerMenu> TYPE = MenuTypeBuilder
            .create(ImaginarySpaceStabilizerMenu::new, ImaginarySpaceStabilizerBlockEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("imaginaryspacestabilizer");

    // TODO Add fields needed to display weight impacts
//    @GuiSync(0)
//    public long currentPower;
    
    private int delay = 40;

    public ImaginarySpaceStabilizerMenu(final int id, final Inventory playerInv, final ImaginarySpaceStabilizerBlockEntity be) {
        super(TYPE, id, playerInv, be);

        this.addSlot(new CatalystItemSlot(be.getInternalInventory(), 0), SlotSemantics.PROCESSING_INPUTS);

        this.createPlayerInventorySlots(playerInv);
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (this.isServerSide()) {
            this.delay++;

            var be = (ImaginarySpaceStabilizerBlockEntity) this.getBlockEntity();
            var gridNode = be.getGridNode();
            var grid = gridNode != null ? gridNode.getGrid() : null;

            if (grid != null && this.delay > 15) {
                this.delay = 0;
            }
        }

        super.broadcastChanges();
    }
    
    private static class CatalystItemSlot extends AppEngSlot {
        public CatalystItemSlot(InternalInventory inv, int invSlot) {
            super(inv, invSlot);
        }

        @Override
        public boolean mayPlace(final ItemStack stack) {
            return super.mayPlace(stack) && IsmCatalyst.isCatalyst(stack);
        }
    }
}
