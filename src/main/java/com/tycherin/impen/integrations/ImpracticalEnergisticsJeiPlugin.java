package com.tycherin.impen.integrations;

import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.recipe.IsmCatalystRecipe;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

@JeiPlugin
public class ImpracticalEnergisticsJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID, "core");

    private static final RecipeType<IsmCatalystRecipe> ISM_CATALYST_RECIPE_TYPE = RecipeType
            .create(ImpracticalEnergisticsMod.MOD_ID, "ism_catalyst", IsmCatalystRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(final IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new IsmCatalystRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(final IRecipeRegistration registry) {
        @SuppressWarnings("resource")
        final RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        registry.addRecipes(ISM_CATALYST_RECIPE_TYPE,
                recipeManager.getAllRecipesFor(ImpracticalEnergisticsMod.ISM_CATALYST_RECIPE_TYPE.get()));

        // In-world recipes go here
        // https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/forge/1.18.2/src/main/java/appeng/integration/modules/jei/JEIPlugin.java#L140

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
    }

}
