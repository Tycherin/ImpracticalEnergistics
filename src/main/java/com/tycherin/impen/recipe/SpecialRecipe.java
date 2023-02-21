package com.tycherin.impen.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

public interface SpecialRecipe extends Recipe<Container> {

    @Deprecated
    @Override
    default boolean matches(Container p_44002_, Level p_44003_) {
        return false;
    }

    @Deprecated
    @Override
    default ItemStack assemble(Container p_44001_) {
        return ItemStack.EMPTY;
    }

    @Deprecated
    @Override
    default boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Deprecated
    @Override
    default ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }
}
