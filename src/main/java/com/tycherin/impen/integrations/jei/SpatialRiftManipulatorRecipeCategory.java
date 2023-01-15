package com.tycherin.impen.integrations.jei;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.item.RiftedSpatialCellItem;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe;

import appeng.core.AppEng;
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

public class SpatialRiftManipulatorRecipeCategory implements IRecipeCategory<SpatialRiftManipulatorRecipe> {

    private static final String TITLE_TRANSLATION_KEY = "gui.impracticalenergistics.jei.srm_recipe_title";

    public static final ResourceLocation UID = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID,
            "spatial_rift_manipulator");

    private final IDrawable background;
    private final IDrawable icon;

    public SpatialRiftManipulatorRecipeCategory(final IGuiHelper guiHelper) {
        final ResourceLocation location = new ResourceLocation(AppEng.MOD_ID,
                "textures/guis/spatial_rift_manipulator.png");
        this.background = guiHelper.createDrawable(location, 60, 31, 89, 40);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                ImpenRegistry.SPATIAL_RIFT_MANIPULATOR.asItem().getDefaultInstance());
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder layoutBuilder, final SpatialRiftManipulatorRecipe recipe,
            final IFocusGroup focusGroup) {

        layoutBuilder.addSlot(RecipeIngredientRole.INPUT, 1, 23)
                .addIngredients(recipe.getBottomInput());

        if (recipe instanceof SpatialRiftManipulatorRecipe.GenericManipulatorRecipe genericRecipe) {
            layoutBuilder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                    .addIngredients(genericRecipe.getTopInput());
            layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 68, 10)
                    .addItemStack(genericRecipe.getOutput());
        }
        else if (recipe instanceof SpatialRiftManipulatorRecipe.SpatialRiftEffectRecipe spatialRecipe) {
            // TODO Display effects here
            layoutBuilder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                    .addIngredients(RiftedSpatialCellItem.getIngredient());
            layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 68, 10)
                    .addIngredients(RiftedSpatialCellItem.getIngredient());
        }
        else {
            throw new RuntimeException("Unrecognized recipe type for " + recipe);
        }
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
    public Class<? extends SpatialRiftManipulatorRecipe> getRecipeClass() {
        return SpatialRiftManipulatorRecipe.class;
    }
}
