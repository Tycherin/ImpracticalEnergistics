package com.tycherin.impen.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;

import net.minecraft.world.level.Level;

public class AtmosphericCrystallizerRecipeManager {

    private static final Map<Level, Optional<AtmosphericCrystallizerRecipe>> RECIPE_CACHE = new HashMap<>();

    public static Optional<AtmosphericCrystallizerRecipe> getRecipe(final Level level) {
        if (!RECIPE_CACHE.containsKey(level)) {
            final var recipeOpt = level.getRecipeManager()
                    .getAllRecipesFor(ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE.get()).stream()
                    .filter(recipe -> recipe.getDimensionKey().equals(level.dimension().location()))
                    .findFirst();
            RECIPE_CACHE.put(level, recipeOpt);
        }
        
        return RECIPE_CACHE.get(level);
    }
}