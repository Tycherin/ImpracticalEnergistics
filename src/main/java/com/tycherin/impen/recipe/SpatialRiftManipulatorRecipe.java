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
import net.minecraft.world.level.block.Block;

public class SpatialRiftManipulatorRecipe implements Recipe<Container> {

    private final ResourceLocation id;

    protected final ItemStack topInput;
    protected final Ingredient bottomInput;
    protected final ItemStack output;

    public SpatialRiftManipulatorRecipe(final ResourceLocation id, final ItemStack topInput,
            final Ingredient bottomInput, final ItemStack output) {
        this.id = id;
        this.topInput = topInput;
        this.bottomInput = bottomInput;
        this.output = output;
    }

    public ItemStack getOutput() {
        return this.output;
    }

    public ItemStack getTopInput() {
        return this.topInput;
    }
    
    public Ingredient getBottomInput() {
        return this.bottomInput;
    }
    

    // ***
    // Recipe boilerplate
    // ***
    
    @Override
    public boolean isSpecial() {
        return true;
    }
    
    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean matches(Container inv, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv) {
        return this.output.copy();
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
        return ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_RECIPE_TYPE.get();
    }

    public static class SpatialStorageRecipe extends SpatialRiftManipulatorRecipe {

        private final Block block;
        private final int value;

        public SpatialStorageRecipe(final ResourceLocation id, final Ingredient bottomInput, final ItemStack output,
                final Block block, final int value) {
            super(id, ImpenRegistry.RIFTED_SPATIAL_CELL_ITEM.asItem().getDefaultInstance(), bottomInput,
                    ImpenRegistry.RIFTED_SPATIAL_CELL_ITEM.asItem().getDefaultInstance());
            this.block = block;
            this.value = value;
        }

        public Block getBlock() {
            return block;
        }

        public int getValue() {
            return value;
        }
    }
}
