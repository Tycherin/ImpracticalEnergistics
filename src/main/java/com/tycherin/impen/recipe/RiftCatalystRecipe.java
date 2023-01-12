package com.tycherin.impen.recipe;

import java.util.List;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.logic.SpatialRiftWeight;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class RiftCatalystRecipe implements Recipe<Container> {

    private final ResourceLocation id;

    private final Block baseBlock;
    private final Item catalyst;
    private final List<Ingredient> consumedItems;
    private final List<SpatialRiftWeight> weights;

    public RiftCatalystRecipe(final ResourceLocation id, final Block baseBlock, final Item catalyst,
            final List<Ingredient> consumedItems, final List<SpatialRiftWeight> weights) {
        this.id = id;
        this.baseBlock = baseBlock;
        this.catalyst = catalyst;
        this.consumedItems = consumedItems;
        this.weights = weights;
    }

    public Block getBaseBlock() {
        return this.baseBlock;
    }

    public Item getCatalyst() {
        return this.catalyst;
    }

    public List<Ingredient> getConsumedItems() {
        return this.consumedItems;
    }

    public List<SpatialRiftWeight> getWeights() {
        return this.weights;
    }

    public boolean matches(final ItemStack catalystStack, final Container container) {
        if (!catalystStack.getItem().equals(catalyst)) {
            return false;
        }
        for (final var ingredient : consumedItems) {
            boolean matched = false;
            for (int i = 0; i < container.getContainerSize(); i++) {
                matched |= ingredient.test(container.getItem(i));
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matches(final Container container, final Level level) {
        return false;
    }

    // ***
    // Recipe boilerplate
    // ***

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack assemble(Container p_44001_) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return null;
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.RIFT_CATALYST_RECIPE_TYPE.get();
    }
}
