package com.tycherin.impen.recipe;

import java.util.List;

import com.tycherin.impen.logic.ism.IsmWeight;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class IsmCatalystRecipe implements Recipe<Container> {

    public static final RecipeType<IsmCatalystRecipe> TYPE = RecipeType
            .register(new ResourceLocation("impracticalenergistics:ism_catalyst").toString());

    private final ResourceLocation id;

    private final Item catalyst;
    private final Block baseBlock;
    private final List<IsmWeight> weights;

    public IsmCatalystRecipe(final ResourceLocation id, final Item catalyst, final Block baseBlock,
            final List<IsmWeight> weights) {
        this.id = id;
        this.catalyst = catalyst;
        this.baseBlock = baseBlock;
        this.weights = weights;
    }

    public Item getCatalyst() {
        return this.catalyst;
    }

    public Block getBaseBlock() {
        return this.baseBlock;
    }

    public List<IsmWeight> getWeights() {
        return this.weights;
    }

    @Override
    public boolean matches(final Container container, final Level level) {
        if (container.getContainerSize() > 1) {
            return false;
        }
        return container.countItem(getCatalyst()) > 0;
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
        return IsmCatalystRecipe.TYPE;
    }
}