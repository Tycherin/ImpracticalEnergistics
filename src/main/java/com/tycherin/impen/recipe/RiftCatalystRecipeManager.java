package com.tycherin.impen.recipe;

import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RiftCatalystRecipeManager {

    public static Optional<RiftCatalystRecipe> getRecipe(final Level level, final ItemStack is) {
        return level.getRecipeManager()
            .getAllRecipesFor(ImpenRegistry.RIFT_CATALYST_RECIPE_TYPE.get()).stream()
            // TODO Decide whether I'm doing multiple inputs per recipe or just one
            .filter(recipe -> recipe.getConsumedItems().get(0).test(is))
            .findFirst();
    }
    
    public static boolean hasRecipe(final Level level, final ItemStack is) {
        return RiftCatalystRecipeManager.getRecipe(level, is).isPresent();
    }
}
