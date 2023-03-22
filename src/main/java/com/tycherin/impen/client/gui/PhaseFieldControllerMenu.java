package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.PhaseFieldControllerBlockEntity;
import com.tycherin.impen.blockentity.PhaseFieldControllerBlockEntity.CapsuleConfigInventory;

import appeng.api.config.SecurityPermissions;
import appeng.api.inventories.InternalInventory;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.FakeSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class PhaseFieldControllerMenu extends AEBaseMenu {

    public static final MenuType<PhaseFieldControllerMenu> TYPE = MenuTypeBuilder
            .create(PhaseFieldControllerMenu::new, PhaseFieldControllerBlockEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("phase_field_controller");

    private final FakeSlot[] configSlots;

    public PhaseFieldControllerMenu(final int id, final Inventory playerInv,
            final PhaseFieldControllerBlockEntity be) {
        super(TYPE, id, playerInv, be);

        this.configSlots = new FakeSlot[be.getCapsuleConfigInv().size()];
        for (int i = 0; i < configSlots.length; i++) {
            configSlots[i] = new FilteringFakeSlot(be.getCapsuleConfigInv(), i);
            configSlots[i].setHideAmount(true);
            this.addSlot(configSlots[i], ImpenSlotSemantics.PFC_CAPSULE);
        }

        this.createPlayerInventorySlots(playerInv);
    }

    private static class FilteringFakeSlot extends FakeSlot {

        public FilteringFakeSlot(final InternalInventory inv, final int invSlot) {
            super(inv, invSlot);
        }

        @Override
        public void set(final ItemStack is) {
            // Ignore set() attempts for invalid items
            // Item.EMPTY is a valid item, so this still allows clearing slots with an empty hand
            if (CapsuleConfigInventory.isItemValid(is)) {
                super.set(is);
            }
        }
    }
}
