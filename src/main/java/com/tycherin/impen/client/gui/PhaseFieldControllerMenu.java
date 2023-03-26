package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.PhaseFieldControllerBlockEntity;
import com.tycherin.impen.blockentity.PhaseFieldControllerBlockEntity.CapsuleConfigInventory;

import appeng.api.config.SecurityPermissions;
import appeng.api.inventories.InternalInventory;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.InaccessibleSlot;
import appeng.util.inv.AppEngInternalInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PhaseFieldControllerMenu extends AEBaseMenu {

    public static final MenuType<PhaseFieldControllerMenu> TYPE = MenuTypeBuilder
            .create(PhaseFieldControllerMenu::new, PhaseFieldControllerBlockEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("phase_field_controller");

    private final FakeSlot[] configSlots;
    private final ValidatingInaccessibleSlot[] mirrorSlots;

    public PhaseFieldControllerMenu(final int id, final Inventory playerInv,
            final PhaseFieldControllerBlockEntity be) {
        super(TYPE, id, playerInv, be);

        this.configSlots = new FakeSlot[be.getInternalInventory().size()];
        this.mirrorSlots = new ValidatingInaccessibleSlot[configSlots.length];

        for (int i = 0; i < configSlots.length; i++) {
            mirrorSlots[i] = new ValidatingInaccessibleSlot(new AppEngInternalInventory(1), 0);
            mirrorSlots[i].setNotDraggable();
            configSlots[i] = new FilteringFakeSlot(be.getInternalInventory(), i, mirrorSlots[i]);
            configSlots[i].setHideAmount(true);
            this.addSlot(configSlots[i], ImpenSlotSemantics.PFC_CAPSULE);
            this.addSlot(mirrorSlots[i], ImpenSlotSemantics.PFC_CAPSULE_MIRROR);
        }

        this.createPlayerInventorySlots(playerInv);
    }

    @Override
    public void broadcastChanges() {
        // Populate the state of the mirror slots based on what's in the config slots
        for (int i = 0; i < configSlots.length; i++) {
            final boolean currentState = mirrorSlots[i].getCurrentValidationState();
            if (configSlots[i].getItem().isEmpty()) {
                mirrorSlots[i].set(ItemStack.EMPTY);
            }
            else {
                mirrorSlots[i].set(
                        ((PhaseFieldControllerBlockEntity)this.getBlockEntity())
                                .getStoredItemCount(configSlots[i].getItem()));
            }
            final boolean newState = mirrorSlots[i].getCurrentValidationState();
            if (newState != currentState) {
                mirrorSlots[i].resetCachedValidation();
            }
        }
        super.broadcastChanges();
    }

    @Override
    public void onServerDataSync() {
        super.onServerDataSync();
        // This field isn't synced by default, so we need to re-derive it here
        for (int i = 0; i < configSlots.length; i++) {
            mirrorSlots[i].resetCachedValidation();
        }
    }

    @Override
    public void onSlotChange(final Slot s) {
        // This is a very funny acronym given the amount of headache this particular "feature" has given me
        if (s instanceof FilteringFakeSlot ffs) {
            ffs.mirrorSlot.isEmptyExpected = ffs.getItem().isEmpty();
            ffs.mirrorSlot.resetCachedValidation();
        }
    }

    private static class FilteringFakeSlot extends FakeSlot {

        private final ValidatingInaccessibleSlot mirrorSlot;

        public FilteringFakeSlot(final InternalInventory inv, final int invSlot,
                final ValidatingInaccessibleSlot mirrorSlot) {
            super(inv, invSlot);
            this.mirrorSlot = mirrorSlot;
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

    private static class ValidatingInaccessibleSlot extends InaccessibleSlot {

        private boolean isEmptyExpected = false;

        public ValidatingInaccessibleSlot(final InternalInventory inv, final int invSlot) {
            super(inv, invSlot);
        }

        @Override
        protected boolean getCurrentValidationState() {
            return isEmptyExpected || this.getItem().getCount() > 0;
        }
    }
}
