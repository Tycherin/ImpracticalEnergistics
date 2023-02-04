package com.tycherin.impen.util;

import org.apache.commons.lang3.NotImplementedException;

import appeng.api.inventories.InternalInventory;
import net.minecraft.world.item.ItemStack;

public class ManagedOutputInventory {

    private final InternalInventory inv;

    public ManagedOutputInventory(final InternalInventory inv) {
        if (inv.size() != 1) {
            throw new NotImplementedException("Currently only handles single-slot inventories");
        }
        this.inv = inv;
    }

    public boolean isEmpty() {
        return this.inv.isEmpty();
    }

    /** @return true if the item can be safely added; false otherwise */
    public boolean canAdd(final ItemStack is) {
        return addInternal(is, false);
    }

    /** @return true if the item was added; false otherwise */
    public boolean tryAdd(final ItemStack is) {
        return addInternal(is, true);
    }

    private boolean addInternal(final ItemStack is, final boolean doAction) {
        final ItemStack curr = this.inv.getStackInSlot(0);
        if (curr.isEmpty()) {
            // Inventory is empty - ok to insert
            if (doAction) {
                this.inv.setItemDirect(0, is);
            }
            return true;
        }
        else {
            if (curr.getItem().equals(is.getItem())) {
                // Same item
                if (curr.getCount() + is.getCount() < curr.getMaxStackSize()) {
                    // Room in stack - ok to insert
                    if (doAction) {
                        curr.setCount(curr.getCount() + is.getCount());
                    }
                    return true;
                }
                else {
                    // No room in stack - can't insert
                    return false;
                }
            }
            else {
                // Different items - can't insert
                return false;
            }
        }
    }
}
