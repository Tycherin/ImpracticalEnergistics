package com.tycherin.impen.datagen.recipe;

import java.util.function.Consumer;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.SpatialRiftSpawnerRecipe;

import appeng.core.definitions.AEItems;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

public class SpatialRiftSpawnerRecipeProvider {

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        final BuilderHelper helper = new BuilderHelper(consumer);

        helper.add(ImpenRegistry.AEROCRYSTAL_PRISM, ImpenRegistry.RIFT_PRISM, 100);

        helper.add(AEItems.SPATIAL_CELL2, ImpenRegistry.SPATIAL_RIFT_CELL_2_ITEM, 20);
        helper.add(AEItems.SPATIAL_CELL16, ImpenRegistry.SPATIAL_RIFT_CELL_16_ITEM, 40);
        helper.add(AEItems.SPATIAL_CELL128, ImpenRegistry.SPATIAL_RIFT_CELL_128_ITEM, 80);
    }

    @RequiredArgsConstructor
    private static class BuilderHelper {
        private final Consumer<FinishedRecipe> consumer;

        public void add(final ItemLike input, final ItemLike output, final int fuelCost) {
            final String recipeName = input.asItem().getRegistryName().getPath();
            final var recipe = new SpatialRiftSpawnerRecipe(null, Ingredient.of(input),
                    output.asItem().getDefaultInstance(), fuelCost);
            consumer.accept(new RealRecipe(recipeName, recipe));
        }
    }

    private static class RealRecipe extends CustomRecipeResult<SpatialRiftSpawnerRecipe> {

        public RealRecipe(final @NonNull String recipeName, @NonNull final SpatialRiftSpawnerRecipe data) {
            super(recipeName, data);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return SpatialRiftSpawnerRecipe.Serializer.INSTANCE;
        }

        @Override
        protected RegistryObject<?> getRecipeHolder() {
            return ImpenRegistry.SPATIAL_RIFT_SPAWNER_RECIPE_TYPE;
        }
    }
}
