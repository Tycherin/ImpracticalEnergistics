package com.tycherin.impen.recipe;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;

import appeng.datagen.providers.recipes.AE2RecipeProvider;
import lombok.Getter;
import lombok.NonNull;
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

@Getter
public class SpatialRiftCollapserRecipe implements SpecialBidirectionalRecipe {

    public static final String RECIPE_TYPE_NAME = "spatial_rift_collapser";

    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack resultItem;

    public SpatialRiftCollapserRecipe(final ResourceLocation id, @NonNull final Ingredient input,
            @NonNull final ItemStack resultItem) {
        this.id = id;
        this.input = input;
        this.resultItem = resultItem;
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

    @Override
    public void serializeRecipeData(final JsonObject json) {
        this.getSerializer().toJson(this, json);
    }

    // ***
    // Recipe boilerplate
    // ***

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
            json.add("output", AE2RecipeProvider.toJson(recipe.resultItem));
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
