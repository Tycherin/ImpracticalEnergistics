package com.tycherin.impen.datagen.recipe;

import java.util.function.Consumer;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.recipe.SpatialRiftCollapserRecipe;

import appeng.core.definitions.AEItems;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

public class SpatialRiftCollapserRecipeProvider {

    public void addRecipes(final Consumer<FinishedRecipe> consumer) {
        final BuilderHelper helper = new BuilderHelper(consumer);

        helper.add(ImpenRegistry.RIFT_PRISM, ImpenRegistry.STABILIZED_RIFT_PRISM);

        helper.add(ImpenRegistry.SPATIAL_RIFT_CELL_2_ITEM, AEItems.SPATIAL_CELL2);
        helper.add(ImpenRegistry.SPATIAL_RIFT_CELL_16_ITEM, AEItems.SPATIAL_CELL16);
        helper.add(ImpenRegistry.SPATIAL_RIFT_CELL_128_ITEM, AEItems.SPATIAL_CELL128);
    }

    @RequiredArgsConstructor
    private static class BuilderHelper {
        private final Consumer<FinishedRecipe> consumer;

        public void add(final ItemLike input, final ItemLike output) {
            final String recipeName = input.asItem().getRegistryName().getPath();
            final var recipe = new SpatialRiftCollapserRecipe(null, Ingredient.of(input),
                    output.asItem().getDefaultInstance());
            consumer.accept(new RealRecipe(recipeName, recipe));
        }
    }

    private static class RealRecipe extends CustomRecipeResult<SpatialRiftCollapserRecipe> {

        public RealRecipe(final @NonNull String recipeName, @NonNull final SpatialRiftCollapserRecipe data) {
            super(recipeName, data);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return SpatialRiftCollapserRecipe.Serializer.INSTANCE;
        }

        @Override
        protected RegistryObject<?> getRecipeHolder() {
            return ImpenRegistry.SPATIAL_RIFT_COLLAPSER_RECIPE_TYPE;
        }
    }
}
