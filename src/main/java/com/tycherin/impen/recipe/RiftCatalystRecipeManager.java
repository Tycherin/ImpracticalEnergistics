package com.tycherin.impen.recipe;

import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RiftCatalystRecipeManager {

    public static Optional<RiftCatalystRecipe> getRecipe(final Level level, final ItemStack catalyst,
            final Container container) {
        if (catalyst.isEmpty() || container.isEmpty()) {
            return Optional.empty();
        }

        return level.getRecipeManager()
                .getAllRecipesFor(ImpenRegistry.RIFT_CATALYST_RECIPE_TYPE.get()).stream()
                .filter(recipe -> recipe.matches(catalyst, container))
                .findFirst();
    }
}
