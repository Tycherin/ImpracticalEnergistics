package com.tycherin.impen.integrations.jei;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.item.RiftedSpatialCellItem;
import com.tycherin.impen.recipe.SpatialRiftSpawnerRecipe;

import appeng.core.AppEng;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

public class SpatialRiftSpawnerRecipeCategory implements IRecipeCategory<SpatialRiftSpawnerRecipe> {

    private static final String TITLE_TRANSLATION_KEY = "gui.impracticalenergistics.jei.srs_recipe_title";

    public static final ResourceLocation UID = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID,
            "spatial_rift_spawner");

    private final IDrawable background;
    private final IDrawable icon;
    private final Ingredient fuelIngredient;

    public SpatialRiftSpawnerRecipeCategory(final IGuiHelper guiHelper) {
        final ResourceLocation location = new ResourceLocation(AppEng.MOD_ID,
                "textures/guis/spatial_rift_spawner.png");
        this.background = guiHelper.createDrawable(location, 14, 36, 135, 30);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                ImpenRegistry.SPATIAL_RIFT_SPAWNER.asItem().getDefaultInstance());
        this.fuelIngredient = Ingredient.of(
                ImpenRegistry.AEROCRYSTAL,
                ImpenRegistry.BLAZING_AEROCRYSTAL,
                ImpenRegistry.EXOTIC_AEROCRYSTAL,
                ImpenRegistry.AEROCRYSTAL_BLOCK,
                ImpenRegistry.BLAZING_AEROCRYSTAL_BLOCK,
                ImpenRegistry.EXOTIC_AEROCRYSTAL_BLOCK);
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder layoutBuilder, final SpatialRiftSpawnerRecipe recipe,
            final IFocusGroup focusGroup) {

        if (recipe.getResultItem().getItem() instanceof RiftedSpatialCellItem) {
            // Spatial cell recipe
            layoutBuilder.addSlot(RecipeIngredientRole.INPUT, 52, 7)
                    .addIngredients(recipe.getInput())
                    .addTooltipCallback((recipeSlotView, tooltip) -> {
                        tooltip.add(new TextComponent("Must be formatted")
                                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
                    });

            layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 114, 7)
                    .addItemStack(recipe.getResultItem())
                    .addTooltipCallback((recipeSlotView, tooltip) -> {
                        tooltip.add(new TextComponent("Matches original cell")
                                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
                    });
        }
        else {
            // Normal recipe
            layoutBuilder.addSlot(RecipeIngredientRole.INPUT, 52, 7)
                    .addIngredients(recipe.getInput());
            layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 114, 7)
                    .addItemStack(recipe.getResultItem());
        }
        
        layoutBuilder.addSlot(RecipeIngredientRole.INPUT, 13, 7)
                .addIngredients(this.fuelIngredient);
        // TODO Display fuel cost graphically
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent(TITLE_TRANSLATION_KEY);
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends SpatialRiftSpawnerRecipe> getRecipeClass() {
        return SpatialRiftSpawnerRecipe.class;
    }
}
