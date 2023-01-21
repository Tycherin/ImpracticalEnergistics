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

    protected final Ingredient bottomInput;

    public SpatialRiftManipulatorRecipe(final ResourceLocation id, final Ingredient bottomInput) {
        this.id = id;
        this.bottomInput = bottomInput;
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
        return ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_RECIPE_TYPE.get();
    }

    /**
     * Normal crafting recipe for Spatial Rift Manipulator, one that doesn't operate on Spatial Rift Cells
     */
    public static class GenericManipulatorRecipe extends SpatialRiftManipulatorRecipe {

        private final Ingredient topInput;
        private final ItemStack output;

        public GenericManipulatorRecipe(final ResourceLocation id, final Ingredient topInput,
                final Ingredient bottomInput, final ItemStack output) {
            super(id, bottomInput);
            this.topInput = topInput;
            this.output = output;
        }

        public ItemStack getOutput() {
            return this.output;
        }

        public Ingredient getTopInput() {
            return this.topInput;
        }
    }

    /**
     * Recipe that manipulates the values inside a Spatial Rift Cell
     * <p>
     * The input for this recipe is always some type of Spatial Rift Cell, and the output is the same cell with modified
     * properties
     */
    public static class SpatialRiftEffectRecipe extends SpatialRiftManipulatorRecipe {

        private final Block block;

        public SpatialRiftEffectRecipe(final ResourceLocation id, final Ingredient bottomInput, final Block block) {
            super(id, bottomInput);
            this.block = block;
        }

        public Block getBlock() {
            return block;
        }
    }
    
    /**
     * There are a couple of recipes that manipulate rift cells, but don't have an associated block, and therefore need
     * special handling
     */
    public static class SpecialSpatialRecipe extends SpatialRiftManipulatorRecipe {

        public static enum SpecialSpatialRecipeType {
            CLEAR_PRECISION,
            BOOST_PRECISION
        }

        private final SpecialSpatialRecipeType specialType;

        public SpecialSpatialRecipe(final ResourceLocation id, final Ingredient bottomInput,
                final SpecialSpatialRecipeType specialType) {
            super(id, bottomInput);
            this.specialType = specialType;
        }

        public SpecialSpatialRecipeType getSpecialType() {
            return specialType;
        }
    }
}
