package com.tycherin.impen.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface SpecialBidirectionalRecipe extends BidirectionalRecipe {

    @Deprecated
    @Override
    default boolean isSpecial() {
        return true;
    }

    @Deprecated
    @Override
    default boolean matches(Container inv, Level level) {
        return false;
    }

    @Deprecated
    @Override
    default ItemStack assemble(Container inv) {
        return ItemStack.EMPTY;
    }

    @Deprecated
    @Override
    default boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    default ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }
}
