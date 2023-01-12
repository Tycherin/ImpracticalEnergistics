package com.tycherin.impen.integrations.jei;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.logic.SpatialRiftWeight;
import com.tycherin.impen.recipe.RiftCatalystRecipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class RiftCatalystRecipeCategory implements IRecipeCategory<RiftCatalystRecipe> {

    private static final String TITLE_TRANSLATION_KEY = "gui.impracticalenergistics.jei.rift_recipe_title";
    private static final int TEXT_COLOR = 0x000000;
    private static final Component BASE_BLOCK_TEXT = new TranslatableComponent(
            "gui.impracticalenergistics.jei.rift_catalyst.base_block_explanation");

    public static final ResourceLocation UID = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID, "rift_catalyst");

    private final IDrawable background;
    private final IDrawable icon;

    public RiftCatalystRecipeCategory(final IGuiHelper guiHelper) {
        final ResourceLocation location = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID,
                "textures/gui/rift_catalyst_jei.png");
        this.background = guiHelper.createDrawable(location, 16, 7, 143, 58);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                ImpenRegistry.RIFTED_SPATIAL_CELL_ITEM.asItem().getDefaultInstance());
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder layoutBuilder, final RiftCatalystRecipe recipe,
            final IFocusGroup focusGroup) {
        // Mmm, delicious kludge
        for (int i = 0; i < recipe.getConsumedItems().size(); i++) {
            final var ingredient = recipe.getConsumedItems().get(i);
            var xPos = 1;
            var yPos = 12;
            if (i == 1 || i == 3) {
                xPos += 18;
            }
            if (i == 2 || i == 3) {
                yPos += 18;
            }
            layoutBuilder.addSlot(RecipeIngredientRole.INPUT, xPos, yPos)
                    .addIngredients(ingredient);
        }

        layoutBuilder.addSlot(RecipeIngredientRole.RENDER_ONLY, 51, 32)
                .addItemStack(getBlockItemStack(recipe.getBaseBlock()))
                .addTooltipCallback((recipeSlotView, tooltip) -> tooltip.add(BASE_BLOCK_TEXT));

        forEachOutput(recipe.getWeights(), (weight, xPos, yPos) -> {
            layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, xPos, yPos)
                    .addItemStack(getBlockItemStack(weight.block()));
        });
    }

    @SuppressWarnings("resource")
    @Override
    public void draw(final RiftCatalystRecipe recipe, final IRecipeSlotsView recipeSlotsView, final PoseStack stack,
            final double mouseX, final double mouseY) {
        forEachOutput(recipe.getWeights(), (weight, xPos, yPos) -> {
            // TODO It would be nice to center this string under the item box
            Minecraft.getInstance().font.draw(stack, new TextComponent(Integer.toString(weight.blockCount())),
                    xPos - 1, yPos + 19, TEXT_COLOR);
        });
    }

    @FunctionalInterface
    private static interface OutputBoxOperator {
        void handleOutputBox(final SpatialRiftWeight weight, final int xPos, final int yPos);
    }

    private void forEachOutput(final List<SpatialRiftWeight> weights, final OutputBoxOperator func) {
        for (int i = 0; i < weights.size(); i++) {
            final int row = i / 3;
            final int col = i % 3;

            final int xPos = 84 + (col * 21);
            final int yPos = 2 + (row * 29);

            func.handleOutputBox(weights.get(i), xPos, yPos);
        }
    }

    private ItemStack getBlockItemStack(final Block block) {
        return Optional.ofNullable(block.asItem()).orElse(Items.AIR).getDefaultInstance();
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
    public Class<? extends RiftCatalystRecipe> getRecipeClass() {
        return RiftCatalystRecipe.class;
    }
}
