package com.tycherin.impen.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.annotate.NoSerialize;
import com.tycherin.impen.util.GsonUtil;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

@Getter
@AllArgsConstructor
public class AtmosphericCrystallizerRecipe implements SpecialRecipe {

    private static final Map<Level, Optional<AtmosphericCrystallizerRecipe>> CACHE = new HashMap<>();

    public static Optional<AtmosphericCrystallizerRecipe> getRecipe(final Level level) {
        if (!CACHE.containsKey(level)) {
            final var recipeOpt = level.getRecipeManager()
                    .getAllRecipesFor(ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE.get()).stream()
                    .filter(recipe -> level.dimension().location().equals(recipe.dimension))
                    .findFirst();
            CACHE.put(level, recipeOpt);
        }

        return CACHE.get(level);
    }

    @NoSerialize
    private ResourceLocation id;
    private ResourceLocation dimension;
    private ItemStack result;

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements RecipeSerializer<AtmosphericCrystallizerRecipe> {

        public static final AtmosphericCrystallizerRecipe.Serializer INSTANCE = new AtmosphericCrystallizerRecipe.Serializer();

        @Override
        public AtmosphericCrystallizerRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
            final var recipe = GsonUtil.getStandardGson().fromJson(json, AtmosphericCrystallizerRecipe.class);
            recipe.id = recipeId;
            return recipe;
        }

        @Nullable
        @Override
        public AtmosphericCrystallizerRecipe fromNetwork(final ResourceLocation recipeId,
                final FriendlyByteBuf buffer) {
            final ResourceLocation dimensionKey = buffer.readResourceLocation();
            final ItemStack result = buffer.readItem();
            return new AtmosphericCrystallizerRecipe(recipeId, dimensionKey, result);
        }

        @Override
        public void toNetwork(final FriendlyByteBuf buffer, final AtmosphericCrystallizerRecipe recipe) {
            buffer.writeResourceLocation(recipe.getDimension());
            buffer.writeItemStack(recipe.getResult(), false);
        }
    }
}
