package com.tycherin.impen.item;

import appeng.items.AEBaseItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RiftedSpatialCellItem extends AEBaseItem {
    
    private static final String TAG_PLOT_ID = "plot_id";

    public RiftedSpatialCellItem(final Item.Properties props) {
        super(props);
    }

    public void setPlotId(final ItemStack is, final int plotId) {
        final CompoundTag c = is.getOrCreateTag();
        c.putInt(TAG_PLOT_ID, plotId);
    }

    public int getPlotId(final ItemStack is) {
        final CompoundTag c = is.getTag();
        if (c != null && c.contains(TAG_PLOT_ID)) {
            return c.getInt(TAG_PLOT_ID);
        }
        else {
            return -1;
        }
    }
}
