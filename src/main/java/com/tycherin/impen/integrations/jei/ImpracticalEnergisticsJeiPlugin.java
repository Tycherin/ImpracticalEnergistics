package com.tycherin.impen.integrations.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.recipe.AtmosphericCrystallizerRecipe;
import com.tycherin.impen.recipe.RiftCatalystRecipe;
import com.tycherin.impen.recipe.SpatialRiftCollapserRecipe;
import com.tycherin.impen.recipe.SpatialRiftSpawnerRecipe;

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
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;

@JeiPlugin
public class ImpracticalEnergisticsJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID, "core");

    private static final RecipeType<RiftCatalystRecipe> RIFT_CATALYST_RECIPE_TYPE = RecipeType
            .create(ImpracticalEnergisticsMod.MOD_ID, "rift_catalyst", RiftCatalystRecipe.class);
    private static final RecipeType<AtmosphericCrystallizerRecipe> ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE = RecipeType
            .create(ImpracticalEnergisticsMod.MOD_ID, "atmospheric_crystallizer", AtmosphericCrystallizerRecipe.class);
    private static final RecipeType<SpatialRiftSpawnerRecipe> SPATIAL_RIFT_SPAWNER_RECIPE_TYPE = RecipeType
            .create(ImpracticalEnergisticsMod.MOD_ID, "spatial_rift_spawner", SpatialRiftSpawnerRecipe.class);
    private static final RecipeType<SpatialRiftCollapserRecipe> SPATIAL_RIFT_COLLAPSER_RECIPE_TYPE = RecipeType
            .create(ImpracticalEnergisticsMod.MOD_ID, "spatial_rift_collapser", SpatialRiftCollapserRecipe.class);
    
    // TODO Consider moving some of these to item-level tooltips
    private static final List<ItemLike> ITEMS_WITH_DESCRIPTION = ImmutableList.of(
            ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER,
            ImpenRegistry.BEAMED_NETWORK_LINK,
            ImpenRegistry.CAPTURE_PLANE_ITEM,
            ImpenRegistry.DISINTEGRATOR_CAPSULE_LOOT,
            ImpenRegistry.DISINTEGRATOR_CAPSULE_LUCK,
            ImpenRegistry.DISINTEGRATOR_CAPSULE_EGG,
            ImpenRegistry.DISINTEGRATOR_CAPSULE_PLAYER_KILL,
            ImpenRegistry.LUNCHBOX_CELL_ITEM,
            ImpenRegistry.POSSIBILITY_DISINTEGRATOR,
            ImpenRegistry.EJECTION_DRIVE);

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(final IRecipeCategoryRegistration registry) {
        final var guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(
                new RiftCatalystRecipeCategory(guiHelper),
                new AtmosphericCrystallizerRecipeCategory(guiHelper),
                new SpatialRiftSpawnerRecipeCategory(guiHelper),
                new SpatialRiftCollapserRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(final IRecipeRegistration registry) {
        @SuppressWarnings("resource")
        final RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        registry.addRecipes(RIFT_CATALYST_RECIPE_TYPE,
                recipeManager.getAllRecipesFor(ImpenRegistry.RIFT_CATALYST_RECIPE_TYPE.get()));
        registry.addRecipes(ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE,
                recipeManager.getAllRecipesFor(ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE.get()));
        registry.addRecipes(SPATIAL_RIFT_SPAWNER_RECIPE_TYPE,
                recipeManager.getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_SPAWNER_RECIPE_TYPE.get()));
        registry.addRecipes(SPATIAL_RIFT_COLLAPSER_RECIPE_TYPE,
                recipeManager.getAllRecipesFor(ImpenRegistry.SPATIAL_RIFT_COLLAPSER_RECIPE_TYPE.get()));

        final List<ThrowingInWaterDisplay> waterRecipes = new ArrayList<>();
        if (AEConfig.instance().isInWorldFluixEnabled()) {
            waterRecipes.add(new ThrowingInWaterDisplay(
                    List.of(
                            Ingredient.of(Items.REDSTONE),
                            Ingredient.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED),
                            Ingredient.of(ImpenRegistry.RIFT_PRISM)),
                    AEItems.FLUIX_DUST.stack(4),
                    false));
        }

        registry.addRecipes(ThrowingInWaterCategory.RECIPE_TYPE, waterRecipes);

        // TODO Change this call the bulk insert version
        ITEMS_WITH_DESCRIPTION.forEach(item -> {
            final String translationKey = "jei.impracticalenergistics.description."
                    + item.asItem().getRegistryName().getPath();
            registry.addIngredientInfo(item.asItem().getDefaultInstance(), VanillaTypes.ITEM_STACK,
                    new TranslatableComponent(translationKey));
        });
    }

    @Override
    public void registerRecipeCatalysts(final IRecipeCatalystRegistration registry) {
        registry.addRecipeCatalyst(
                ImpenRegistry.SPATIAL_RIFT_SPAWNER.item().getDefaultInstance(),
                RIFT_CATALYST_RECIPE_TYPE);
        registry.addRecipeCatalyst(
                ImpenRegistry.SPATIAL_RIFT_COLLAPSER.item().getDefaultInstance(),
                RIFT_CATALYST_RECIPE_TYPE);
        registry.addRecipeCatalyst(
                ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER.item().getDefaultInstance(),
                ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE);
        registry.addRecipeCatalyst(
                ImpenRegistry.SPATIAL_RIFT_SPAWNER.item().getDefaultInstance(),
                SPATIAL_RIFT_SPAWNER_RECIPE_TYPE);
        registry.addRecipeCatalyst(
                ImpenRegistry.SPATIAL_RIFT_COLLAPSER.item().getDefaultInstance(),
                SPATIAL_RIFT_COLLAPSER_RECIPE_TYPE);
    }

    @Override
    public void onRuntimeAvailable(final IJeiRuntime jeiRuntime) {
        // Hide the fake dimension item, which shouldn't be exposed to the player
        jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                Collections.singletonList(ImpenRegistry.FAKE_DIMENSION_PLACEHOLDER.asItem().getDefaultInstance()));
    }
}
