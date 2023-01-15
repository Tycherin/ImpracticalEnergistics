package com.tycherin.impen.item;

import com.tycherin.impen.ImpenRegistry;

import appeng.items.AEBaseItem;
import appeng.items.storage.SpatialStorageCellItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.util.LazyOptional;

public class RiftedSpatialCellItem extends AEBaseItem {
    
    private static final String TAG_PLOT_ID = "plot_id";
    private static final LazyOptional<Ingredient> INGREDIENT = LazyOptional.of(
            () -> Ingredient.of(
                    ImpenRegistry.SPATIAL_RIFT_CELL_2_ITEM,
                    ImpenRegistry.SPATIAL_RIFT_CELL_16_ITEM,
                    ImpenRegistry.SPATIAL_RIFT_CELL_128_ITEM));

    public static final Ingredient getIngredient() {
        return INGREDIENT.orElseThrow(() -> new RuntimeException("Problem creating rift cell ingredient"));
    }

    public static final RiftedSpatialCellItem getMatchingCell(final SpatialStorageCellItem spatialCell) {
        final Item item = (switch (spatialCell.getMaxStoredDim(null)) {
        case 2 -> ImpenRegistry.SPATIAL_RIFT_CELL_2_ITEM;
        case 16 -> ImpenRegistry.SPATIAL_RIFT_CELL_16_ITEM;
        case 128 -> ImpenRegistry.SPATIAL_RIFT_CELL_128_ITEM;
        default -> throw new RuntimeException("Unrecognized cell size for " + spatialCell);
        }).asItem();
        // Theoretically I should change ItemDefinition to track the actual item type to avoid the cast here, but ehhh
        return (RiftedSpatialCellItem)item;
    }
    
    private final ItemLike originalItem;

    public RiftedSpatialCellItem(final Item.Properties props, final ItemLike originalItem) {
        super(props);
        this.originalItem = originalItem;
    }

    public ItemLike getOriginalItem() {
        return originalItem;
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
