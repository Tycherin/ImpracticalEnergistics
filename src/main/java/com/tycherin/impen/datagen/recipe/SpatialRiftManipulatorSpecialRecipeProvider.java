package com.tycherin.impen.datagen.recipe;

import java.util.function.Consumer;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.SpatialRiftManipulatorSpecialRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorSpecialRecipe.SpecialEffectType;

import lombok.NonNull;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.RegistryObject;

public class SpatialRiftManipulatorSpecialRecipeProvider {

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        addRecipe(consumer, "clear_precision", Ingredient.of(Tags.Items.GLASS),
                SpatialRiftManipulatorSpecialRecipe.SpecialEffectType.CLEAR_INPUTS);

        addRecipe(consumer, "boost_precision", Ingredient.of(ImpenRegistry.STABILIZED_RIFT_PRISM),
                SpatialRiftManipulatorSpecialRecipe.SpecialEffectType.BOOST_PRECISION);
    }

    private void addRecipe(final Consumer<FinishedRecipe> consumer, final String recipeName,
            final Ingredient bottomInput, final SpecialEffectType type) {
        final var recipe = new SpatialRiftManipulatorSpecialRecipe(null, bottomInput, type);
        consumer.accept(new RealRecipe(recipeName, recipe));
    }

    private static class RealRecipe extends CustomRecipeResult<SpatialRiftManipulatorSpecialRecipe> {

        public RealRecipe(@NonNull String recipeName, @NonNull SpatialRiftManipulatorSpecialRecipe data) {
            super(recipeName, data);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return SpatialRiftManipulatorSpecialRecipe.Serializer.INSTANCE;
        }

        @Override
        protected RegistryObject<?> getRecipeHolder() {
            return ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_SPECIAL_RECIPE_TYPE;
        }
    }
}
