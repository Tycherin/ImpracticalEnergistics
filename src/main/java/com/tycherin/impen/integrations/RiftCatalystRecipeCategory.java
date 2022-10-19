package com.tycherin.impen.integrations;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.logic.rift.RiftWeight;
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
            "gui.impracticalenergistics.jei.base_block_explanation");

    public static final ResourceLocation UID = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID, "rift_catalyst");

    private final IDrawable background;
    private final IDrawable icon;

    public RiftCatalystRecipeCategory(final IGuiHelper guiHelper) {
        final ResourceLocation location = new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID,
                "textures/gui/rift_catalyst_jei.png");
        this.background = guiHelper.createDrawable(location, 24, 16, 131, 58);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_ITEM.get().getDefaultInstance());
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder layoutBuilder, final RiftCatalystRecipe recipe,
            final IFocusGroup focusGroup) {
        layoutBuilder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStack(recipe.getCatalyst().getDefaultInstance());

        layoutBuilder.addSlot(RecipeIngredientRole.RENDER_ONLY, 26, 14)
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
            // This works as long as probabilities are between 0 and 100
            // 100 will display okay if it's the only one in the list, and semantically, that's the only thing that
            // makes sense
            // Numbers smaller than 0.1 will display as 0.0 because ¯\_(ツ)_/¯
            final String percentageText = weight.probability() >= 1
                    ? String.format("%.0f%%", weight.probability())
                    : String.format("%.1f%%", weight.probability());
            // TODO It would be nice to center this string under the item box
            Minecraft.getInstance().font.draw(stack, new TextComponent(percentageText), xPos - 1, yPos + 19,
                    TEXT_COLOR);
        });
    }

    @FunctionalInterface
    private static interface OutputBoxOperator {
        void handleOutputBox(final RiftWeight weight, final int xPos, final int yPos);
    }

    private void forEachOutput(final List<RiftWeight> weights, final OutputBoxOperator func) {
        for (int i = 0; i < weights.size(); i++) {
            final int row = i / 4;
            final int col = i % 4;

            final int xPos = 51 + (col * 21);
            final int yPos = 1 + (row * 29);

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
