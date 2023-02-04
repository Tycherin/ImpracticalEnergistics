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
public class SpatialRiftSpawnerRecipe implements BidirectionalRecipe<Container> {

    public static final String RECIPE_TYPE_NAME = "spatial_rift_spawner";

    private final ResourceLocation id;

    private final Ingredient input;
    private final ItemStack output;
    private final int fuelCost;

    public SpatialRiftSpawnerRecipe(final ResourceLocation id, @NonNull final Ingredient input,
            @NonNull final ItemStack output, final int fuelCost) {
        this.id = id;
        this.input = input;
        this.output = output;
        if (fuelCost < 0) {
            throw new IllegalArgumentException("Fuel cost must not be negative");
        }
        this.fuelCost = fuelCost;
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
    public SpatialRiftSpawnerRecipe.Serializer getSerializer() {
        return SpatialRiftSpawnerRecipe.Serializer.INSTANCE;
    }

    @Override
    public String getRecipeTypeName() {
        return RECIPE_TYPE_NAME;
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.SPATIAL_RIFT_SPAWNER_RECIPE_TYPE.get();
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements BidirectionalRecipeSerializer<SpatialRiftSpawnerRecipe> {

        public static final SpatialRiftSpawnerRecipe.Serializer INSTANCE = new SpatialRiftSpawnerRecipe.Serializer();

        private Serializer() {
        }

        @Override
        public SpatialRiftSpawnerRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
            final Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            final int fuelCost = GsonHelper.getAsInt(json, "fuel_cost");
            return new SpatialRiftSpawnerRecipe(recipeId, input, output, fuelCost);
        }

        @Override
        public void toJson(final SpatialRiftSpawnerRecipe recipe, final JsonObject json) {
            json.add("input", recipe.input.toJson());
            json.add("output", AE2RecipeProvider.toJson(recipe.output));
            json.addProperty("fuel_cost", recipe.fuelCost);
        }

        @Nullable
        @Override
        public SpatialRiftSpawnerRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
            final Ingredient input = Ingredient.fromNetwork(buffer);
            final ItemStack output = buffer.readItem();
            final int fuelCost = buffer.readInt();
            return new SpatialRiftSpawnerRecipe(recipeId, input, output, fuelCost);
        }

        @Override
        public void toNetwork(final FriendlyByteBuf buffer, final SpatialRiftSpawnerRecipe recipe) {
            recipe.getInput().toNetwork(buffer);
            buffer.writeItemStack(recipe.getResultItem(), true);
            buffer.writeInt(recipe.getFuelCost());
        }
    }

}
