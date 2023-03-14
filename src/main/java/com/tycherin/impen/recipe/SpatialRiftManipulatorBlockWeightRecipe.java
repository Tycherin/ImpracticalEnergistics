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
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistryEntry;

@Getter
@AllArgsConstructor
public class SpatialRiftManipulatorBlockWeightRecipe implements SpatialRiftManipulatorRecipe {

    @NoSerialize
    private ResourceLocation id;
    private Ingredient bottomInput;
    private Block block;

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.SPATIAL_RIFT_MANIPULATOR_BLOCK_WEIGHT_RECIPE_TYPE.get();
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements RecipeSerializer<SpatialRiftManipulatorBlockWeightRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public SpatialRiftManipulatorBlockWeightRecipe fromJson(final ResourceLocation recipeId,
                final JsonObject json) {
            final var recipe = GsonUtil.getStandardGson().fromJson(json, SpatialRiftManipulatorBlockWeightRecipe.class);
            recipe.id = recipeId;
            return recipe;
        }

        @Override
        public void toNetwork(final FriendlyByteBuf buffer, final SpatialRiftManipulatorBlockWeightRecipe recipe) {
            recipe.bottomInput.toNetwork(buffer);
            buffer.writeRegistryId(recipe.block);
        }

        @Override
        public SpatialRiftManipulatorBlockWeightRecipe fromNetwork(final ResourceLocation recipeId,
                final FriendlyByteBuf buffer) {
            final Ingredient bottomInput = Ingredient.fromNetwork(buffer);
            final Block block = buffer.readRegistryIdSafe(Block.class);
            return new SpatialRiftManipulatorBlockWeightRecipe(recipeId, bottomInput, block);
        }
    }
}
