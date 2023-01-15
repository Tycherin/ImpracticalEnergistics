package com.tycherin.impen.item;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipeManager;

import appeng.items.AEBaseItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
    
    public boolean addRecipe(final ItemStack is, final SpatialRiftManipulatorRecipe.SpatialStorageRecipe recipe) {
        final CompoundTag c = is.getTag();
        if (!c.contains(TAG_INGREDIENTS)) {
            c.put(TAG_INGREDIENTS, new CompoundTag());
        }
        final CompoundTag ingTag = c.getCompound(TAG_INGREDIENTS);
        
        if (ingTag.size() >= this.getMaxRecipes(is)) {
            // Safety net, mostly to avoid NBT overflow problems
            return false;
        }
        
        final String key = recipe.getId().toString();
        if (ingTag.contains(key)) {
            final int oldCount = ingTag.getInt(key);
            ingTag.putInt(key, oldCount + 1);
        }
        else {
            ingTag.putInt(key, 1);
        }
        
        return true;
    }
    
    public int getMaxRecipes(final ItemStack is) {
        // TODO Adjust based on size
        return 16;
    }
    
    public Map<SpatialRiftManipulatorRecipe.SpatialStorageRecipe, Integer> getRecipes(final Level level,
            final ItemStack is) {
        final Map<SpatialRiftManipulatorRecipe.SpatialStorageRecipe, Integer> ingMap = new HashMap<>();
        final CompoundTag c = is.getTag();

        if (c.contains(TAG_INGREDIENTS)) {
            final CompoundTag ingTag = c.getCompound(TAG_INGREDIENTS);

            ingTag.getAllKeys().forEach(recipeName -> {
                SpatialRiftManipulatorRecipeManager.getRecipe(level, recipeName)
                        .filter(recipe -> (recipe instanceof SpatialRiftManipulatorRecipe.SpatialStorageRecipe))
                        .ifPresentOrElse(recipe -> {
                            ingMap.compute((SpatialRiftManipulatorRecipe.SpatialStorageRecipe)recipe,
                                    (r, count) -> count + 1);
                        },
                                () -> LOGGER.warn("No recipe found for key {}; input will be ignored", recipeName));
            });
        }

        return ingMap;
    }
}
