package com.tycherin.impen.datagen;

import java.util.function.Consumer;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;

import appeng.core.definitions.AEItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;

public class ImpenRecipeProvider extends RecipeProvider {

    public ImpenRecipeProvider(final DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void buildCraftingRecipes(final Consumer<FinishedRecipe> consumer) {
        ShapelessRecipeBuilder.shapeless(ImpenRegistry.PLANTABLE_CERTUS.item())
                .requires(AEItems.CERTUS_CRYSTAL_SEED)
                .unlockedBy("has_certus_seed", has(AEItems.CERTUS_CRYSTAL_SEED))
                .save(consumer, makeId("plantable_certus_seeds"));
        ShapelessRecipeBuilder.shapeless(ImpenRegistry.PLANTABLE_FLUIX.item())
                .requires(AEItems.FLUIX_CRYSTAL_SEED)
                .unlockedBy("has_fluix_seed", has(AEItems.FLUIX_CRYSTAL_SEED))
                .save(consumer, makeId("plantable_fluix_seeds"));
    }

    private ResourceLocation makeId(final String key) {
        return new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID, key);
    }
}
