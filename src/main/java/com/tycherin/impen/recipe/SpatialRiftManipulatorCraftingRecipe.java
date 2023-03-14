package com.tycherin.impen.recipe;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.annotate.NoSerialize;
import com.tycherin.impen.util.GsonUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistryEntry;

@Getter
@AllArgsConstructor
public class SpatialRiftManipulatorCraftingRecipe implements SpatialRiftManipulatorRecipe {

    @NoSerialize
    private ResourceLocation id;
    private Ingredient topInput;
    private Ingredient bottomInput;
    private ItemStack output;

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_CRAFTING_RECIPE_TYPE.get();
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements RecipeSerializer<SpatialRiftManipulatorCraftingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public SpatialRiftManipulatorCraftingRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
            final var recipe = GsonUtil.getStandardGson().fromJson(json, SpatialRiftManipulatorCraftingRecipe.class);
            recipe.id = recipeId;
            return recipe;
        }

        @Override
        public void toNetwork(final FriendlyByteBuf buffer, final SpatialRiftManipulatorCraftingRecipe recipe) {
            recipe.topInput.toNetwork(buffer);
            recipe.bottomInput.toNetwork(buffer);
            buffer.writeItemStack(recipe.output, false);
        }

        @Override
        public SpatialRiftManipulatorCraftingRecipe fromNetwork(final ResourceLocation recipeId,
                final FriendlyByteBuf buffer) {
            final Ingredient topInput = Ingredient.fromNetwork(buffer);
            final Ingredient bottomInput = Ingredient.fromNetwork(buffer);
            final ItemStack output = buffer.readItem();
            return new SpatialRiftManipulatorCraftingRecipe(recipeId, topInput, bottomInput, output);
        }
    }
}
