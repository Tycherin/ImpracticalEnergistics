package com.tycherin.impen.recipe;

import com.google.gson.JsonObject;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.annotate.NoSerialize;
import com.tycherin.impen.util.GsonUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistryEntry;

@Getter
@AllArgsConstructor
public class SpatialRiftManipulatorSpecialRecipe implements SpatialRiftManipulatorRecipe {

    public static enum SpecialEffectType {
        CLEAR_INPUTS,
        BOOST_PRECISION,
        BOOST_RICHNESS
    }

    @NoSerialize
    private ResourceLocation id;
    private Ingredient bottomInput;
    private SpecialEffectType effectType;

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_SPECIAL_RECIPE_TYPE.get();
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements RecipeSerializer<SpatialRiftManipulatorSpecialRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public SpatialRiftManipulatorSpecialRecipe fromJson(final ResourceLocation recipeId,
                final JsonObject json) {
            final var recipe = GsonUtil.getStandardGson().fromJson(json, SpatialRiftManipulatorSpecialRecipe.class);
            recipe.id = recipeId;
            return recipe;
        }

        @Override
        public void toNetwork(final FriendlyByteBuf buffer, final SpatialRiftManipulatorSpecialRecipe recipe) {
            recipe.bottomInput.toNetwork(buffer);
            buffer.writeEnum(recipe.effectType);
        }

        @Override
        public SpatialRiftManipulatorSpecialRecipe fromNetwork(final ResourceLocation recipeId,
                final FriendlyByteBuf buffer) {
            final Ingredient bottomInput = Ingredient.fromNetwork(buffer);
            final SpecialEffectType effectType = buffer.readEnum(SpecialEffectType.class);
            return new SpatialRiftManipulatorSpecialRecipe(recipeId, bottomInput, effectType);
        }
    }
}
