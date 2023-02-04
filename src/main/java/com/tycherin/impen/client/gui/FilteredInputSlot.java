package com.tycherin.impen.client.gui;

import appeng.api.inventories.InternalInventory;
import appeng.menu.slot.AppEngSlot;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FilteredInputSlot extends AppEngSlot {

    private final IAEItemFilter filter;

    public FilteredInputSlot(final IAEItemFilter filter, final InternalInventory inv, final int invSlot) {
        super(inv, invSlot);
        this.filter = filter;
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        if (!this.getMenu().isValidForSlot(this, stack)) {
            return false;
        }

        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() == Items.AIR) {
            return false;
        }

        if (!super.mayPlace(stack)) {
            return false;
        }

        return filter.allowInsert(getInventory(), getSlotIndex(), stack);
    }

    @Override
    public boolean mayPickup(final Player player) {
        return super.mayPickup(player);
    }
}
