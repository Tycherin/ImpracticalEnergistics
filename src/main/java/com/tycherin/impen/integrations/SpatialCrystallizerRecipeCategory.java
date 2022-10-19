package com.tycherin.impen.integrations;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.recipe.SpatialCrystallizerRecipe;

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
import net.minecraft.world.item.Items;

public class SpatialCrystallizerRecipeCategory implements IRecipeCategory<SpatialCrystallizerRecipe> {

    private static final String TITLE_TRANSLATION_KEY = "gui.impracticalenergistics.jei.spatial_crystallizer_recipe_title";

    public static final ResourceLocation UID = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID,
            "spatial_crystallizer");

    private final IDrawable background;
    private final IDrawable icon;

    public SpatialCrystallizerRecipeCategory(final IGuiHelper guiHelper) {
        final ResourceLocation location = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID,
                "textures/gui/imaginary_space_manipulator.png");
        this.background = guiHelper.createDrawable(location, 51, 44, 83, 25);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                ImpenRegistry.SPATIAL_CRYSTALLIZER_ITEM.get().getDefaultInstance());
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder layoutBuilder, final SpatialCrystallizerRecipe recipe,
            final IFocusGroup focusGroup) {
        // TODO Use fake dimension item here
        final ItemStack fakeIs = Items.COMMAND_BLOCK.getDefaultInstance().copy();
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
    public Class<? extends SpatialCrystallizerRecipe> getRecipeClass() {
        return SpatialCrystallizerRecipe.class;
    }
}
