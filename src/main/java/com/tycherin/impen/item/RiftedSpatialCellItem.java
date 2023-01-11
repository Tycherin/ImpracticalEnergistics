package com.tycherin.impen.item;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import appeng.items.AEBaseItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class RiftedSpatialCellItem extends AEBaseItem {

    private static final Logger LOGGER = LogUtils.getLogger();
    
    private static final String TAG_PLOT_ID = "plot_id";
    private static final String TAG_ORIGINAL_CELL_SIZE = "orig_size";
    private static final String TAG_INGREDIENTS = "ingredients";

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

    public void setOriginalCellSize(final ItemStack is, final int size) {
        final CompoundTag c = is.getOrCreateTag();
        c.putInt(TAG_ORIGINAL_CELL_SIZE, size);
    }

    public int getOriginalCellSize(final ItemStack is) {
        final CompoundTag c = is.getTag();
        if (c != null && c.contains(TAG_ORIGINAL_CELL_SIZE)) {
            return c.getInt(TAG_ORIGINAL_CELL_SIZE);
        }
        else {
            return -1;
        }
    }

    public boolean addIngredient(final ItemStack is, final Item item) {
        final CompoundTag c = is.getTag();
        if (!c.contains(TAG_INGREDIENTS)) {
            c.put(TAG_INGREDIENTS, new CompoundTag());
        }
        
        final CompoundTag ingTag = c.getCompound(TAG_INGREDIENTS);
        
        if (ingTag.size() >= 16) {
            // Safety net to avoid NBT overflow problems
            return false;
        }
        
        final String key = item.getRegistryName().toString();
        if (ingTag.contains(key)) {
            final var oldCount = ingTag.getInt(key);
            ingTag.putInt(key, oldCount + 1);
        }
        else {
            ingTag.putInt(key, 1);
        }
        
        return true;
    }

    public Map<Item, Integer> getIngredients(final ItemStack is) {
        final Map<Item, Integer> ingMap = new HashMap<>();
        final CompoundTag c = is.getTag();

        if (c.contains(TAG_INGREDIENTS)) {
            final CompoundTag ingTag = c.getCompound(TAG_INGREDIENTS);

            ingTag.getAllKeys().forEach(itemName -> {
                ForgeRegistries.ITEMS.getHolder(new ResourceLocation(itemName))
                        .ifPresentOrElse(
                                holder -> ingMap.put(holder.value(), ingTag.getInt(itemName)),
                                () -> LOGGER.warn("No registry entry found for {}, ignoring stored ingredient",
                                        itemName));
            });
        }

        return ingMap;
    }
}
