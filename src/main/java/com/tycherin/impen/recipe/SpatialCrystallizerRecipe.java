package com.tycherin.impen.recipe;

import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class SpatialCrystallizerRecipe implements Recipe<Container> {

    private final ResourceLocation id;

    private final ResourceLocation dimensionKey;
    private final ItemStack result;

    public SpatialCrystallizerRecipe(final ResourceLocation id, final ResourceLocation dimensionKey, final ItemStack result) {
        this.id = id;
        this.dimensionKey = dimensionKey;
        this.result = result;
    }

    public ResourceLocation getDimensionKey() {
        return this.dimensionKey;
    }
    
    public Optional<Level> getDimension(final Level sourceLevel) {
        // The dimension registry is weird, so we have to jump through some hoops to get at it
        final var dimensionRegistry = sourceLevel.registryAccess().registry(Registry.DIMENSION_REGISTRY).get();
        if (dimensionRegistry.containsKey(this.dimensionKey)) {
            return Optional.of(dimensionRegistry.get(this.dimensionKey));
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean matches(Container p_44002_, Level p_44003_) {
        return false;
    }

    @Override
    public ItemStack assemble(Container p_44001_) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return this.result;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SpatialCrystallizerRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.SPATIAL_CRYSTALLIZER_RECIPE_TYPE.get();
    }
}
