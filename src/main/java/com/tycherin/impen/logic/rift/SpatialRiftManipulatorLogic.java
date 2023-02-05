package com.tycherin.impen.logic.rift;

import java.util.Optional;

import com.tycherin.impen.item.SpatialRiftCellItem;
import com.tycherin.impen.logic.rift.SpatialRiftCellDataManager.SpatialRiftCellData;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe.SpecialSpatialRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipeManager;

import appeng.spatial.SpatialStoragePlotManager;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Slf4j
public class SpatialRiftManipulatorLogic {

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
        else if (modifierIs.isEmpty()) {
            // A rift cell is always valid if there's nothing in the other slot
            return true;
        }
        else {
            final var recipeOpt = getRecipeInput(spatialCellIs, modifierIs);
            if (!recipeOpt.isPresent()) {
                // No recipe for these inputs
                return false;
            }
            else {
                if (recipeOpt.get() instanceof SpatialRiftEffectRecipe spatialRecipe) {
                    if (dataOpt.get().getRemainingSlots() == 0) {
                        // We can't add inputs if there are no slots left
                        return false;
                    }
                    else if (dataOpt.get().hasBlock(spatialRecipe.getBlock())) {
                        // We can't add an input that's already present
                        return false;
                    }
                    else {
                        return true;
                    }
                }
                else if (recipeOpt.get() instanceof SpecialSpatialRecipe specialRecipe) {
                    // These modifiers only make sense if the cell has some inputs already
                    return dataOpt.get().getUsedSlots() > 0;
                }
                else {
                    log.warn("Unexpected recipe subtype for {}", recipeOpt.get());
                    return false;
                }
            }
        }
    }

    private Optional<? extends SpatialRiftManipulatorRecipe> getRecipeInput(final ItemStack topSlot,
            final ItemStack bottomSlot) {
        return SpatialRiftManipulatorRecipeManager.getRecipe(level, topSlot, bottomSlot);
    }

    public ItemStack processInputs(final ItemStack topSlot, final ItemStack bottomSlot, final Level level) {
        if (topSlot.getItem() instanceof SpatialRiftCellItem) {
            return processInputsSpatialCell(topSlot, bottomSlot, level);
        }
        else {
            return processInputsRecipe(topSlot, bottomSlot);
        }
    }

    private ItemStack processInputsSpatialCell(final ItemStack spatialCellIs, final ItemStack modifierIs,
            final Level level) {
        final Optional<SpatialRiftCellData> dataOpt = SpatialRiftCellDataManager.INSTANCE.getDataForCell(spatialCellIs);
        if (dataOpt.isEmpty()) {
            log.warn("No rift cell data found for input spatial cell!");
            return ItemStack.EMPTY;
        }
        final SpatialRiftCellData data = dataOpt.get();
        final var recipe = getRecipeInput(spatialCellIs, modifierIs).get();

        if (recipe instanceof SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe spatialRecipe) {
            data.addInput(level, spatialRecipe.getBlock());
            return spatialCellIs;
        }
        else if (recipe instanceof SpatialRiftManipulatorRecipe.SpecialSpatialRecipe specialRecipe) {
            switch (specialRecipe.getSpecialType()) {
            case BOOST_PRECISION -> data.addPrecisionBoost(MODIFIER_BOOST_AMOUNT);
            case CLEAR_INPUTS -> data.clearInputs(level);
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
