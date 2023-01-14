package com.tycherin.impen.recipe;

import com.tycherin.impen.ImpenRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class SpatialRiftCollapserRecipe implements Recipe<Container> {

    private final ResourceLocation id;

    private final Ingredient input;
    private final ItemStack output;

    public SpatialRiftCollapserRecipe(final ResourceLocation id, final Ingredient input, final ItemStack output) {
        this.id = id;
        this.input = input;
        this.output = output;
    }

    @Override
    public ItemStack getResultItem() {
        return output;
    }

    @Override
    public boolean matches(final Container container, final Level level) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (input.test(container.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    public Ingredient getInput() {
        return input;
    }

    // ***
    // Recipe boilerplate
    // ***

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack assemble(final Container container) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SpatialRiftCollapserRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.SPATIAL_RIFT_COLLAPSER_RECIPE_TYPE.get();
    }

}
