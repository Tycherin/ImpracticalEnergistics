package com.tycherin.impen.recipe;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.annotate.NoSerialize;
import com.tycherin.impen.util.GsonUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

@Getter
@AllArgsConstructor
public class SpatialRiftSpawnerRecipe implements SpecialRecipe {

    public static final String RECIPE_TYPE_NAME = "spatial_rift_spawner";

    @NoSerialize
    private ResourceLocation id;

    private Ingredient input;
    private ItemStack resultItem;
    private int fuelCost;

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
    public SpatialRiftSpawnerRecipe.Serializer getSerializer() {
        return SpatialRiftSpawnerRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.SPATIAL_RIFT_SPAWNER_RECIPE_TYPE.get();
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements RecipeSerializer<SpatialRiftSpawnerRecipe> {

        public static final SpatialRiftSpawnerRecipe.Serializer INSTANCE = new SpatialRiftSpawnerRecipe.Serializer();

        private Serializer() {
        }

        @Override
        public SpatialRiftSpawnerRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
            final var recipe = GsonUtil.getStandardGson().fromJson(json, SpatialRiftSpawnerRecipe.class);
            recipe.id = recipeId;
            return recipe;
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
