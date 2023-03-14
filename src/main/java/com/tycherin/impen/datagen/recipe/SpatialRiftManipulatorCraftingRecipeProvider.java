package com.tycherin.impen.datagen.recipe;

import java.util.List;
import java.util.function.Consumer;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.SpatialRiftManipulatorCraftingRecipe;

import lombok.NonNull;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

public class SpatialRiftManipulatorCraftingRecipeProvider {
    
    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        List.of(
                createRecipe(ImpenRegistry.RIFTSTONE, Items.IRON_PICKAXE, ImpenRegistry.RIFTSTONE_DUST))
                .forEach(recipe -> {
                    final String recipeName = recipe.getOutput().getItem().getRegistryName().getPath();
                    consumer.accept(new RealRecipe(recipeName, recipe));
                });
    }
    
    private SpatialRiftManipulatorCraftingRecipe createRecipe(final ItemLike topInput, final ItemLike bottomInput, final ItemLike output) {
        return new SpatialRiftManipulatorCraftingRecipe(null, Ingredient.of(topInput), Ingredient.of(bottomInput),
                output.asItem().getDefaultInstance());
    }
    
    private static class RealRecipe extends CustomRecipeResult<SpatialRiftManipulatorCraftingRecipe> {

        public RealRecipe(@NonNull String recipeName, @NonNull SpatialRiftManipulatorCraftingRecipe data) {
            super(recipeName, data);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return SpatialRiftManipulatorCraftingRecipe.Serializer.INSTANCE;
        }

        @Override
        protected RegistryObject<?> getRecipeHolder() {
            return ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_CRAFTING_RECIPE_TYPE;
        }
    }
}
