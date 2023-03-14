package com.tycherin.impen.logic.rift;

import java.util.Optional;

import com.tycherin.impen.item.SpatialRiftCellItem;
import com.tycherin.impen.recipe.SpatialRiftManipulatorBaseBlockRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorBlockWeightRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorCraftingRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipeManager;
import com.tycherin.impen.recipe.SpatialRiftManipulatorSpecialRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorSpecialRecipe.SpecialEffectType;

import appeng.spatial.SpatialStoragePlotManager;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Slf4j
public class SpatialRiftManipulatorLogic {

    private static final int MAX_BOOST_LEVEL = 3;

    private final Level level = SpatialStoragePlotManager.INSTANCE.getLevel();

    /**
     * @param topSlot Input item in top slot; can be empty
     * @param bottomSlot Input item in bottom slot; can be empty
     * @return True if the given input combination is allowed
     */
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
                if (recipeOpt.get() instanceof SpatialRiftManipulatorBaseBlockRecipe baseBlockRecipe) {
                    if (dataOpt.get().getBaseBlock().isPresent()) {
                        // Can't add a base block if one is already set
                        return false;
                    }
                    else {
                        return true;
                    }
                }
                else if (recipeOpt.get() instanceof SpatialRiftManipulatorBlockWeightRecipe blockWeightRecipe) {
                    if (dataOpt.get().getBaseBlock().isEmpty()) {
                        // Can't add target blocks if the base block isn't set
                        return false;
                    }
                    if (dataOpt.get().getAvailableSlots() == 0) {
                        // We can't add inputs if there are no slots left
                        return false;
                    }
                    else {
                        final var existingBoost = dataOpt.get().getBoosts().stream()
                                .filter(boost -> boost.getBlock().equals(blockWeightRecipe.getBlock()))
                                .findAny();
                        if (existingBoost.isPresent()) {
                            // An existing input can be boosted further only up to a certain point
                            return existingBoost.get().getCount() < MAX_BOOST_LEVEL;
                        }
                        else {
                            // No existing input, so we're okay here
                            return true;
                        }
                    }
                }
                else if (recipeOpt.get() instanceof SpatialRiftManipulatorSpecialRecipe specialRecipe) {
                    if (dataOpt.get().getBaseBlock().isEmpty()) {
                        // We want to enforce setting the base block as the first operation
                        return false;
                    }
                    else {
                        if (specialRecipe.getEffectType().equals(SpecialEffectType.CLEAR_INPUTS)) {
                            // Only allow clearing inputs if there are some inputs to clear
                            return dataOpt.get().getUsedSlots() > 0;
                        }
                        else if (specialRecipe.getEffectType().equals(SpecialEffectType.BOOST_PRECISION)) {
                            return dataOpt.get().getPrecisionLevel() < MAX_BOOST_LEVEL;
                        }
                        else if (specialRecipe.getEffectType().equals(SpecialEffectType.BOOST_RICHNESS)) {
                            return dataOpt.get().getRichnessLevel() < MAX_BOOST_LEVEL;
                        }
                        else {
                            log.warn("Unexpected effect type for {}", specialRecipe);
                            return false;
                        }
                    }
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

    /**
     * Takes in a set of inputs, processes them appropriately, and returns the corresponding output.
     * 
     * @param topSlot Input in top slot
     * @param bottomSlot Input in bottom slot
     * @return Output item
     */
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
            log.warn("No rift cell data found for input spatial cell! Inputs will be voided.");
            return ItemStack.EMPTY;
        }
        final SpatialRiftCellData data = dataOpt.get();
        final var recipe = getRecipeInput(spatialCellIs, modifierIs).get();

        if (recipe instanceof SpatialRiftManipulatorBaseBlockRecipe baseBlockRecipe) {
            data.setBaseBlock(Optional.of(baseBlockRecipe.getBaseBlock()));
            return spatialCellIs;
        }
        else if (recipe instanceof SpatialRiftManipulatorBlockWeightRecipe blockWeightRecipe) {
            data.boostBlock(blockWeightRecipe.getBlock());
            return spatialCellIs;
        }
        else if (recipe instanceof SpatialRiftManipulatorSpecialRecipe specialRecipe) {
            switch (specialRecipe.getEffectType()) {
            case CLEAR_INPUTS -> data.clearInputs();
            case BOOST_PRECISION -> data.boostPrecision();
            case BOOST_RICHNESS -> data.boostRichness();
            }
            return spatialCellIs;
        }
        else {
            throw new RuntimeException("Unhandled recipe subtype for " + recipe.getId());
        }
    }

    private ItemStack processInputsRecipe(final ItemStack topSlot, final ItemStack bottomSlot) {
        return getRecipeInput(topSlot, bottomSlot)
                .filter(recipe -> (recipe instanceof SpatialRiftManipulatorCraftingRecipe))
                .map(SpatialRiftManipulatorCraftingRecipe.class::cast)
                .map(SpatialRiftManipulatorCraftingRecipe::getOutput)
                .orElse(ItemStack.EMPTY);
    }
}
