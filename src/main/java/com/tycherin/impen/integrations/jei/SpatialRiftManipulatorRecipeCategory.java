package com.tycherin.impen.integrations.jei;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.item.SpatialRiftCellItem;
import com.tycherin.impen.recipe.SpatialRiftManipulatorBaseBlockRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorBlockWeightRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorCraftingRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorSpecialRecipe;

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

public class SpatialRiftManipulatorRecipeCategory implements IRecipeCategory<SpatialRiftManipulatorRecipe> {

    private static final String TITLE_TRANSLATION_KEY = "gui.impracticalenergistics.jei.srm_recipe_title";

    public static final ResourceLocation UID = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID,
            "spatial_rift_manipulator");

    private final IDrawable background;
    private final IDrawable icon;

    public SpatialRiftManipulatorRecipeCategory(final IGuiHelper guiHelper) {
        final ResourceLocation location = new ResourceLocation(AppEng.MOD_ID,
                "textures/guis/spatial_rift_manipulator.png");
        this.background = guiHelper.createDrawable(location, 40, 31, 109, 40);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                ImpenRegistry.SPATIAL_RIFT_MANIPULATOR.asItem().getDefaultInstance());
    }

    private enum Slots {
        TOP(1, 1),
        BOTTOM(1, 23),
        OUT(88, 11),
        BLOCK(47, 25);

        final int x;
        final int y;

        Slots(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder layoutBuilder, final SpatialRiftManipulatorRecipe recipe,
            final IFocusGroup focusGroup) {

        if (recipe instanceof SpatialRiftManipulatorCraftingRecipe craftingRecipe) {
            layoutBuilder.addSlot(RecipeIngredientRole.INPUT, Slots.TOP.x, Slots.TOP.y)
                    .addIngredients(craftingRecipe.getTopInput());
            layoutBuilder.addSlot(RecipeIngredientRole.INPUT, Slots.BOTTOM.x, Slots.BOTTOM.y)
                    .addIngredients(craftingRecipe.getBottomInput());
            layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, Slots.OUT.x, Slots.OUT.y)
                    .addItemStack(craftingRecipe.getOutput());
        }
        else if (recipe instanceof SpatialRiftManipulatorBlockWeightRecipe blockWeightRecipe) {
            final var topSlot = layoutBuilder.addSlot(RecipeIngredientRole.INPUT, Slots.TOP.x, Slots.TOP.y)
                    .addIngredients(SpatialRiftCellItem.getIngredient());
            layoutBuilder.addSlot(RecipeIngredientRole.INPUT, Slots.BOTTOM.x, Slots.BOTTOM.y)
                    .addIngredients(blockWeightRecipe.getBottomInput());
            final var outSlot = layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, Slots.OUT.x, Slots.OUT.y)
                    .addIngredients(SpatialRiftCellItem.getIngredient());
            final var blockSlot = layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, Slots.BLOCK.x, Slots.BLOCK.y)
                    .addItemStack(blockWeightRecipe.getBlock().asItem().getDefaultInstance());

            blockSlot.addTooltipCallback((recipeSlotView, tooltip) -> {
                tooltip.add(new TextComponent("Makes this block more likely to appear")
                        .withStyle(ChatFormatting.DARK_GREEN));
            });

            // Link these two slots together so they rotate in unison
            layoutBuilder.createFocusLink(topSlot, outSlot);
        }
        else if (recipe instanceof SpatialRiftManipulatorSpecialRecipe specialRecipe) {
            final var topSlot = layoutBuilder.addSlot(RecipeIngredientRole.INPUT, Slots.TOP.x, Slots.TOP.y)
                    .addIngredients(SpatialRiftCellItem.getIngredient());
            layoutBuilder.addSlot(RecipeIngredientRole.INPUT, Slots.BOTTOM.x, Slots.BOTTOM.y)
                    .addIngredients(specialRecipe.getBottomInput());
            final var outSlot = layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, Slots.OUT.x, Slots.OUT.y)
                    .addIngredients(SpatialRiftCellItem.getIngredient());

            final String effectStr = switch (specialRecipe.getEffectType()) {
            case BOOST_PRECISION -> "Boosts precision, which improves the effectiveness of targeting specific blocks";
            case CLEAR_INPUTS -> "Erases all modifications";
            case BOOST_RICHNESS -> "Boosts richness, which adds more valuable blocks to the output";
            };
            outSlot.addTooltipCallback((recipeSlotView, tooltip) -> {
                tooltip.add(new TextComponent(effectStr)
                        .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
            });

            // Link these two slots together so they rotate in unison
            layoutBuilder.createFocusLink(topSlot, outSlot);
        }
        else if (recipe instanceof SpatialRiftManipulatorBaseBlockRecipe baseBlockRecipe) {
            final var topSlot = layoutBuilder.addSlot(RecipeIngredientRole.INPUT, Slots.TOP.x, Slots.TOP.y)
                    .addIngredients(SpatialRiftCellItem.getIngredient());
            layoutBuilder.addSlot(RecipeIngredientRole.INPUT, Slots.BOTTOM.x, Slots.BOTTOM.y)
                    .addItemStack(baseBlockRecipe.getIngredient());
            final var outSlot = layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, Slots.OUT.x, Slots.OUT.y)
                    .addIngredients(SpatialRiftCellItem.getIngredient());
            final var blockSlot = layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, Slots.BLOCK.x, Slots.BLOCK.y)
                    .addItemStack(baseBlockRecipe.getBaseBlock().asItem().getDefaultInstance());

            blockSlot.addTooltipCallback((recipeSlotView, tooltip) -> {
                tooltip.add(new TextComponent("Sets this block as the base block for the cell")
                        .withStyle(ChatFormatting.DARK_GREEN));
            });

            // Link these two slots together so they rotate in unison
            layoutBuilder.createFocusLink(topSlot, outSlot);
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
