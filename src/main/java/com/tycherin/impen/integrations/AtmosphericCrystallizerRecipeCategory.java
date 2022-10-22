package com.tycherin.impen.integrations;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.recipe.AtmosphericCrystallizerRecipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class AtmosphericCrystallizerRecipeCategory implements IRecipeCategory<AtmosphericCrystallizerRecipe> {

    private static final String TITLE_TRANSLATION_KEY = "gui.impracticalenergistics.jei.atmospheric_crystallizer_recipe_title";

    public static final ResourceLocation UID = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID,
            "atmospheric_crystallizer");

    private final IDrawable background;
    private final IDrawable icon;

    public AtmosphericCrystallizerRecipeCategory(final IGuiHelper guiHelper) {
        final ResourceLocation location = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID,
                "textures/gui/spatial_rift_manipulator.png"); // TODO Replace placeholder value
        this.background = guiHelper.createDrawable(location, 51, 44, 83, 25);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER.item().getDefaultInstance());
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder layoutBuilder, final AtmosphericCrystallizerRecipe recipe,
            final IFocusGroup focusGroup) {
        final ItemStack fakeIs = ImpenRegistry.FAKE_DIMENSION_PLACEHOLDER.asItem().getDefaultInstance();
        final Component dimensionName = new TranslatableComponent("dimension." + recipe.getDimensionKey());
        fakeIs.setHoverName(dimensionName);
        layoutBuilder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1, 4)
                .addItemStack(fakeIs);

        layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 62, 5)
                .addItemStack(recipe.getResultItem());
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
    public Class<? extends AtmosphericCrystallizerRecipe> getRecipeClass() {
        return AtmosphericCrystallizerRecipe.class;
    }
}
