package com.tycherin.impen.integrations.jei;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.recipe.SpatialRiftCollapserRecipe;

import appeng.core.AppEng;
import appeng.items.storage.SpatialStorageCellItem;
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

public class SpatialRiftCollapserRecipeCategory implements IRecipeCategory<SpatialRiftCollapserRecipe> {

    private static final String TITLE_TRANSLATION_KEY = "gui.impracticalenergistics.jei.src_recipe_title";

    public static final ResourceLocation UID = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID,
            "spatial_rift_collapser");

    private final IDrawable background;
    private final IDrawable icon;

    public SpatialRiftCollapserRecipeCategory(final IGuiHelper guiHelper) {
        final ResourceLocation location = new ResourceLocation(AppEng.MOD_ID,
                "textures/guis/spatial_rift_collapser.png");
        this.background = guiHelper.createDrawable(location, 65, 38, 84, 25);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                ImpenRegistry.SPATIAL_RIFT_COLLAPSER.asItem().getDefaultInstance());
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder layoutBuilder, final SpatialRiftCollapserRecipe recipe,
            final IFocusGroup focusGroup) {
        
        if (recipe.getResultItem().getItem() instanceof SpatialStorageCellItem outputItem) {
            // TODO Special formatting here
            layoutBuilder.addSlot(RecipeIngredientRole.INPUT, 1, 5)
                    .addIngredients(recipe.getInput());
            layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 65, 5)
                    .addItemStack(recipe.getResultItem());
        }
        else {
            layoutBuilder.addSlot(RecipeIngredientRole.INPUT, 1, 5)
                    .addIngredients(recipe.getInput());
            layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 65, 5)
                    .addItemStack(recipe.getResultItem());
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
    public Class<? extends SpatialRiftCollapserRecipe> getRecipeClass() {
        return SpatialRiftCollapserRecipe.class;
    }
}
