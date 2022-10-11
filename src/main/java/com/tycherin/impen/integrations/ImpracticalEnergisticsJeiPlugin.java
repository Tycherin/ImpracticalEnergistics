package com.tycherin.impen.integrations;

import java.util.ArrayList;
import java.util.List;

import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.recipe.IsmCatalystRecipe;
import com.tycherin.impen.recipe.SpatialCrystallizerRecipe;

import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.integration.modules.jei.throwinginwater.ThrowingInWaterCategory;
import appeng.integration.modules.jei.throwinginwater.ThrowingInWaterDisplay;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;

@JeiPlugin
public class ImpracticalEnergisticsJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID, "core");

    private static final RecipeType<IsmCatalystRecipe> ISM_CATALYST_RECIPE_TYPE = RecipeType
            .create(ImpracticalEnergisticsMod.MOD_ID, "ism_catalyst", IsmCatalystRecipe.class);
    private static final RecipeType<SpatialCrystallizerRecipe> SPATIAL_CRYSTALLIZER_RECIPE_TYPE = RecipeType
            .create(ImpracticalEnergisticsMod.MOD_ID, "spatial_crystallizer", SpatialCrystallizerRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(final IRecipeCategoryRegistration registry) {
        final var guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(
                new IsmCatalystRecipeCategory(guiHelper),
                new SpatialCrystallizerRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(final IRecipeRegistration registry) {
        @SuppressWarnings("resource")
        final RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        registry.addRecipes(ISM_CATALYST_RECIPE_TYPE,
                recipeManager.getAllRecipesFor(ImpracticalEnergisticsMod.ISM_CATALYST_RECIPE_TYPE.get()));
        registry.addRecipes(SPATIAL_CRYSTALLIZER_RECIPE_TYPE,
                recipeManager.getAllRecipesFor(ImpracticalEnergisticsMod.SPATIAL_CRYSTALLIZER_RECIPE_TYPE.get()));

        final List<ThrowingInWaterDisplay> waterRecipes = new ArrayList<>();
        if (AEConfig.instance().isInWorldFluixEnabled()) {
            waterRecipes.add(new ThrowingInWaterDisplay(
                    List.of(
                            Ingredient.of(Items.REDSTONE),
                            Ingredient.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED),
                            Ingredient.of(ImpracticalEnergisticsMod.FLUIX_CATALYST_ITEM.get())),
                    AEItems.FLUIX_DUST.stack(4),
                    false));
        }
        // Make a new RecipeType here because AE2 is still doing things the old way
        registry.addRecipes(new RecipeType<>(ThrowingInWaterCategory.ID, ThrowingInWaterDisplay.class), waterRecipes);

        // Item descriptions go here
    }

    @Override
    public void registerRecipeCatalysts(final IRecipeCatalystRegistration registry) {
        registry.addRecipeCatalyst(
                ImpracticalEnergisticsMod.IMAGINARY_SPACE_MANIPULATOR_ITEM.get().getDefaultInstance(),
                ISM_CATALYST_RECIPE_TYPE);
        registry.addRecipeCatalyst(
                ImpracticalEnergisticsMod.IMAGINARY_SPACE_STABILIZER_ITEM.get().getDefaultInstance(),
                ISM_CATALYST_RECIPE_TYPE);
        registry.addRecipeCatalyst(
                ImpracticalEnergisticsMod.SPATIAL_CRYSTALLIZER_ITEM.get().getDefaultInstance(),
                SPATIAL_CRYSTALLIZER_RECIPE_TYPE);
    }

}
