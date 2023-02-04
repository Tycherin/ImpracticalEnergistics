package com.tycherin.impen.recipe;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;

import appeng.datagen.providers.recipes.AE2RecipeProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SpatialRiftCollapserRecipe implements BidirectionalRecipe<Container> {

    public static final String RECIPE_TYPE_NAME = "spatial_rift_collapser";

    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;

    public SpatialRiftCollapserRecipe(final ResourceLocation id, final Ingredient input, final ItemStack output) {
        this.id = id;
        if (input == null) {
            throw new IllegalArgumentException("Input must not be null");
        }
        this.input = input;
        if (output == null) {
            throw new IllegalArgumentException("Output must not be null");
        }
        this.output = output;
    }

    @Override
    public ItemStack getResultItem() {
        return output;
    }

    @Override
    public boolean matches(final Container container, final Level level) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (input.test(container.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    public Ingredient getInput() {
        return input;
    }

    @Override
    public void serializeRecipeData(final JsonObject json) {
        this.getSerializer().toJson(this, json);
    }

    // ***
    // Recipe boilerplate
    // ***

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack assemble(final Container container) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public SpatialRiftCollapserRecipe.Serializer getSerializer() {
        return SpatialRiftCollapserRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.SPATIAL_RIFT_COLLAPSER_RECIPE_TYPE.get();
    }

    @Override
    public String getRecipeTypeName() {
        return RECIPE_TYPE_NAME;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements BidirectionalRecipeSerializer<SpatialRiftCollapserRecipe> {

        public static final SpatialRiftCollapserRecipe.Serializer INSTANCE = new SpatialRiftCollapserRecipe.Serializer();

        private Serializer() {
        }

        @Override
        public SpatialRiftCollapserRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
            final Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            return new SpatialRiftCollapserRecipe(recipeId, input, output);
        }

        @Override
        public void toJson(final SpatialRiftCollapserRecipe recipe, final JsonObject json) {
            json.add("input", recipe.input.toJson());
            json.add("output", AE2RecipeProvider.toJson(recipe.output));
        }

        @Nullable
        @Override
        public SpatialRiftCollapserRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
            final Ingredient input = Ingredient.fromNetwork(buffer);
            final ItemStack output = buffer.readItem();
            return new SpatialRiftCollapserRecipe(recipeId, input, output);
        }

        @Override
        public void toNetwork(final FriendlyByteBuf buffer, final SpatialRiftCollapserRecipe recipe) {
            recipe.getInput().toNetwork(buffer);
            buffer.writeItemStack(recipe.getResultItem(), true);
        }
    }
}
