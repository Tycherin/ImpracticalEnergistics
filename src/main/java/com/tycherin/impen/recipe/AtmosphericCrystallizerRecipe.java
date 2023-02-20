package com.tycherin.impen.recipe;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.lang3.NotImplementedException;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;

import lombok.Getter;
import lombok.NonNull;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

@Getter
public class AtmosphericCrystallizerRecipe implements SpecialBidirectionalRecipe {

    public static final String RECIPE_TYPE_NAME = "atmospheric_crystallizer";

    private final ResourceLocation id;
    private final ResourceLocation dimensionKey;
    private final ItemStack resultItem;

    public AtmosphericCrystallizerRecipe(final ResourceLocation id, @NonNull final ResourceLocation dimensionKey,
            @NonNull final ItemStack resultItem) {
        this.id = id;
        this.dimensionKey = dimensionKey;
        this.resultItem = resultItem;
    }

    public Optional<Level> getDimension(final Level sourceLevel) {
        // The dimension registry is weird, so we have to jump through some hoops to get at it
        final var dimensionRegistry = sourceLevel.registryAccess().registry(Registry.DIMENSION_REGISTRY).get();
        if (dimensionRegistry.containsKey(this.dimensionKey)) {
            return Optional.of(dimensionRegistry.get(this.dimensionKey));
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER_RECIPE_TYPE.get();
    }

    @Override
    public String getRecipeTypeName() {
        return RECIPE_TYPE_NAME;
    }

    @Override
    public void serializeRecipeData(final JsonObject json) {
        this.getSerializer().toJson(this, json);
    }

    @Override
    public AtmosphericCrystallizerRecipe.Serializer getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements BidirectionalRecipeSerializer<AtmosphericCrystallizerRecipe> {

        public static final AtmosphericCrystallizerRecipe.Serializer INSTANCE = new AtmosphericCrystallizerRecipe.Serializer();

        private Serializer() {
        }

        @Override
        public AtmosphericCrystallizerRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
            // Ideally we would validate that the dimension exists here, but unfortunately, the dimension registry isn't
            // available at the time when recipes are loaded
            final ResourceLocation dimensionKey = new ResourceLocation(GsonHelper.getAsString(json, "dimension"));
            final ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new AtmosphericCrystallizerRecipe(recipeId, dimensionKey, result);
        }

        @Override
        public void toJson(final AtmosphericCrystallizerRecipe recipe, final JsonObject json) {
            // TODO Set up datagen for these recipes
            throw new NotImplementedException();
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
            buffer.writeResourceLocation(recipe.getDimensionKey());
            buffer.writeItemStack(recipe.getResultItem(), false);
        }
    }
}
