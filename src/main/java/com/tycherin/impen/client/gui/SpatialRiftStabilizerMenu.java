package com.tycherin.impen.client.gui;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.SpatialRiftStabilizerBlockEntity;

import appeng.api.config.SecurityPermissions;
import appeng.api.inventories.InternalInventory;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SpatialRiftStabilizerMenu extends AEBaseMenu {
    
    public static final MenuType<SpatialRiftStabilizerMenu> TYPE = MenuTypeBuilder
            .create(SpatialRiftStabilizerMenu::new, SpatialRiftStabilizerBlockEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("spatial_rift_stabilizer");

    // TODO Add fields needed to display weight impacts
    
    private int delay = 40;

    public SpatialRiftStabilizerMenu(final int id, final Inventory playerInv, final SpatialRiftStabilizerBlockEntity be) {
        super(TYPE, id, playerInv, be);

        this.addSlot(new CatalystItemSlot(be.getInternalInventory(), 0, be), SlotSemantics.PROCESSING_INPUTS);

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
    
    private static class CatalystItemSlot extends AppEngSlot {
        private final BlockEntity be;
        
        public CatalystItemSlot(InternalInventory inv, int invSlot, BlockEntity be) {
            super(inv, invSlot);
            this.be = be;
        }

        @Override
        public boolean mayPlace(final ItemStack stack) {
            return super.mayPlace(stack) && be.getLevel().getRecipeManager()
                    .getRecipeFor(ImpenRegistry.RIFT_CATALYST_RECIPE_TYPE.get(), new SimpleContainer(stack),
                            be.getLevel())
                    .isPresent();
        }
    }
}
