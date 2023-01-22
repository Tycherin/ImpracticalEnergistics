package com.tycherin.impen.logic;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import com.tycherin.impen.item.SpatialRiftCellItem;
import com.tycherin.impen.logic.SpatialRiftCellDataManager.SpatialRiftCellData;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipeManager;

import appeng.spatial.SpatialStoragePlotManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SpatialRiftManipulatorLogic {

    private static final Logger LOGGER = LogUtils.getLogger();

    // TODO Get rid of this set and depend on the special recipe type instead
    private static final Item MODIFIER_ITEM_CLEAR = Items.GLASS;
    private static final Item MODIFIER_ITEM_BOOST = Items.BOW;
    private static final Set<Item> SPECIAL_MODIFIERS = ImmutableSet.of(
            MODIFIER_ITEM_CLEAR, MODIFIER_ITEM_BOOST);

    private static final int MODIFIER_BOOST_AMOUNT = 20;

    private final Level level = SpatialStoragePlotManager.INSTANCE.getLevel();

    public boolean isValidInput(final ItemStack topSlot, final ItemStack bottomSlot) {
        if (topSlot.getItem() instanceof SpatialRiftCellItem) {
            return isValidSpatialCellInput(topSlot, bottomSlot);
        }
        else {
            return getRecipeInput(topSlot, bottomSlot).isPresent();
        }
    }

    private boolean isValidSpatialCellInput(final ItemStack spatialCellIs, final ItemStack modifierIs) {
        final Optional<SpatialRiftCellData> dataOpt = SpatialRiftCellDataManager.INSTANCE.getDataForCell(spatialCellIs);
        if (dataOpt.isEmpty()) {
            // If the rift cell isn't formatted, then it isn't valid
            return false;
        }
        else if (dataOpt.get().getRemainingSlots() > 0) {
            return getRecipeInput(spatialCellIs, modifierIs).isPresent();
        }
        else if (modifierIs.isEmpty()) {
            // If the modifier slot is empty, then this is okay
            return true;
        }
        else if (SPECIAL_MODIFIERS.contains(modifierIs.getItem())) {
            return true;
        }
        else {
            // Otherwise, it's an item that won't work
            return false;
        }
    }

    private Optional<? extends SpatialRiftManipulatorRecipe> getRecipeInput(final ItemStack topSlot,
            final ItemStack bottomSlot) {
        return SpatialRiftManipulatorRecipeManager.getRecipe(level, topSlot, bottomSlot);
    }

    public ItemStack processInputs(final ItemStack topSlot, final ItemStack bottomSlot) {
        if (topSlot.getItem() instanceof SpatialRiftCellItem) {
            return processInputsSpatialCell(topSlot, bottomSlot);
        }
        else {
            return processInputsRecipe(topSlot, bottomSlot);
        }
    }

    private ItemStack processInputsSpatialCell(final ItemStack spatialCellIs, final ItemStack modifierIs) {
        final Optional<SpatialRiftCellData> dataOpt = SpatialRiftCellDataManager.INSTANCE.getDataForCell(spatialCellIs);
        if (dataOpt.isEmpty()) {
            LOGGER.warn("No rift cell data found for input spatial cell!");
            return ItemStack.EMPTY;
        }
        final SpatialRiftCellData data = dataOpt.get();
        final var recipe = getRecipeInput(spatialCellIs,
                modifierIs).get();
        
        if (recipe instanceof SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe spatialRecipe) {
            // TODO How to handle the situation where the modifier already exists?
            data.addInput(spatialRecipe.getBlock());
            return spatialCellIs;
        }
        else if (recipe instanceof SpatialRiftManipulatorRecipe.SpecialSpatialRecipe specialRecipe) {
            switch (specialRecipe.getSpecialType()) {
            case BOOST_PRECISION -> data.addPrecisionBoost(MODIFIER_BOOST_AMOUNT);
            case CLEAR_PRECISION -> data.clearInputs();
            }
            return spatialCellIs;
        }
        else {
            throw new RuntimeException("Unhandled recipe subtype for " + recipe.getId());
        }
    }

    private ItemStack processInputsRecipe(final ItemStack topSlot, final ItemStack bottomSlot) {
        return getRecipeInput(topSlot, bottomSlot)
                .filter(recipe -> (recipe instanceof SpatialRiftManipulatorRecipe.GenericManipulatorRecipe))
                .map(recipe -> (SpatialRiftManipulatorRecipe.GenericManipulatorRecipe)recipe)
                .map(SpatialRiftManipulatorRecipe.GenericManipulatorRecipe::getOutput)
                .orElse(ItemStack.EMPTY);
    }
}
