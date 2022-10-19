package com.tycherin.impen.integrations;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.recipe.RiftCatalystRecipe;
import com.tycherin.impen.recipe.AtmosphericCrystallizerRecipe;

import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.integration.modules.jei.throwinginwater.ThrowingInWaterCategory;
import appeng.integration.modules.jei.throwinginwater.ThrowingInWaterDisplay;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;

@JeiPlugin
public class ImpracticalEnergisticsJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID, "core");

    private static final RecipeType<RiftCatalystRecipe> RIFT_CATALYST_RECIPE_TYPE = RecipeType
            .create(ImpracticalEnergisticsMod.MOD_ID, "rift_catalyst", RiftCatalystRecipe.class);
    private static final RecipeType<AtmosphericCrystallizerRecipe> ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE = RecipeType
            .create(ImpracticalEnergisticsMod.MOD_ID, "atmospheric_crystallizer", AtmosphericCrystallizerRecipe.class);
    
    private static final List<Item> ITEMS_WITH_DESCRIPTION = ImmutableList.of(
            ImpenRegistry.BEAMED_NETWORK_LINK.item());

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(final IRecipeCategoryRegistration registry) {
        final var guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(
                new RiftCatalystRecipeCategory(guiHelper),
                new AtmosphericCrystallizerRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(final IRecipeRegistration registry) {
        @SuppressWarnings("resource")
        final RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        registry.addRecipes(RIFT_CATALYST_RECIPE_TYPE,
                recipeManager.getAllRecipesFor(ImpenRegistry.RIFT_CATALYST_RECIPE_TYPE.get()));
        registry.addRecipes(ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE,
                recipeManager.getAllRecipesFor(ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE.get()));

        final List<ThrowingInWaterDisplay> waterRecipes = new ArrayList<>();
        if (AEConfig.instance().isInWorldFluixEnabled()) {
            waterRecipes.add(new ThrowingInWaterDisplay(
                    List.of(
                            Ingredient.of(Items.REDSTONE),
                            Ingredient.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED),
                            Ingredient.of(ImpenRegistry.RIFT_PRISM.item())),
                    AEItems.FLUIX_DUST.stack(4),
                    false));
        }
        // Make a new RecipeType here because AE2 is still doing things the old way
        registry.addRecipes(new RecipeType<>(ThrowingInWaterCategory.ID, ThrowingInWaterDisplay.class), waterRecipes);

        ITEMS_WITH_DESCRIPTION.forEach(item -> {
            final String translationKey = "jei.impracticalenergistics.description."
                    + item.getRegistryName().getPath();
            registry.addIngredientInfo(item.getDefaultInstance(), VanillaTypes.ITEM_STACK,
                    new TranslatableComponent(translationKey));
        });
    }

    @Override
    public void registerRecipeCatalysts(final IRecipeCatalystRegistration registry) {
        registry.addRecipeCatalyst(
                ImpenRegistry.SPATIAL_RIFT_MANIPULATOR.item().getDefaultInstance(),
                RIFT_CATALYST_RECIPE_TYPE);
        registry.addRecipeCatalyst(
                ImpenRegistry.SPATIAL_RIFT_STABILIZER.item().getDefaultInstance(),
                RIFT_CATALYST_RECIPE_TYPE);
        registry.addRecipeCatalyst(
                ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER.item().getDefaultInstance(),
                ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE);
    }

}
