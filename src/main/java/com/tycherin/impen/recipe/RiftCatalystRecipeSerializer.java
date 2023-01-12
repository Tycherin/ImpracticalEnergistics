package com.tycherin.impen.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.tycherin.impen.logic.SpatialRiftWeight;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class RiftCatalystRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<RiftCatalystRecipe> {

    public static final RiftCatalystRecipeSerializer INSTANCE = new RiftCatalystRecipeSerializer();

    private static final int MAX_OUTPUTS = 8;

    private RiftCatalystRecipeSerializer() {
    }

    @Override
    public RiftCatalystRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {

        final Block baseBlock = this.getAsBlock(GsonHelper.getAsJsonObject(json, "base_block"));

        final List<Ingredient> consumedItems = new ArrayList<>();
        final JsonArray consumedItemsJson = GsonHelper.getAsJsonArray(json, "consumed_items");
        consumedItemsJson.forEach(ingredientJson -> {
            consumedItems.add(Ingredient.fromJson(ingredientJson));
        });
        
        final List<SpatialRiftWeight> weights = new ArrayList<>();
        final JsonArray blockWeightsJson = GsonHelper.getAsJsonArray(json, "weights");
        if (blockWeightsJson.size() > MAX_OUTPUTS) {
            throw new JsonSyntaxException(String.format("Too many outputs for '%s'", recipeId));
        }
        blockWeightsJson.forEach(weightJson -> {
            if (!weightJson.isJsonObject()) {
                throw new JsonSyntaxException(String.format("Invalid weight '%s'", weightJson));
            }
            final JsonObject weightObj = weightJson.getAsJsonObject();
            final Block weightBlock = this.getAsBlock(weightObj);
            final Integer weightValue = GsonHelper.getAsInt(weightObj, "value");
            if (weightValue < 0 || weightValue > 100) {
                throw new JsonSyntaxException(String.format("Weight values must be between 100 and 0 '%s'", weightValue));
            }
            weights.add(new SpatialRiftWeight(weightBlock, weightValue));
        });
        
        return new RiftCatalystRecipe(recipeId, baseBlock, consumedItems, weights);
    }

    @Nullable
    @Override
    public RiftCatalystRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
        final Block baseBlock = ForgeRegistries.BLOCKS.getValue(buffer.readRegistryId());

        final int consumedItemCount = buffer.readInt();
        final List<Ingredient> consumedItems = new ArrayList<>();
        for (int i = 0; i < consumedItemCount; i++) {
            consumedItems.add(Ingredient.fromNetwork(buffer));
        }

        final int weightCount = buffer.readInt();
        final List<SpatialRiftWeight> weights = new ArrayList<>();
        for (int i = 0; i < weightCount; i++) {
            final Block weightBlock = ForgeRegistries.BLOCKS.getValue(buffer.readRegistryId());
            final Integer weightValue = buffer.readInt();
            weights.add(new SpatialRiftWeight(weightBlock, weightValue));
        }

        return new RiftCatalystRecipe(recipeId, baseBlock, consumedItems, weights);
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buffer, final RiftCatalystRecipe recipe) {
        buffer.writeRegistryId(recipe.getBaseBlock());

        buffer.writeInt(recipe.getConsumedItems().size());
        recipe.getConsumedItems().forEach(ingredient -> ingredient.toNetwork(buffer));

        buffer.writeInt(recipe.getWeights().size());
        recipe.getWeights().forEach(weight -> {
            buffer.writeRegistryId(weight.block());
            buffer.writeInt(weight.blockCount());
        });
    }

    private Block getAsBlock(final JsonObject json) {
        final String name = GsonHelper.getAsString(json, "block");
        final ResourceLocation key = new ResourceLocation(name);
        if (!ForgeRegistries.BLOCKS.containsKey(key)) {
            throw new JsonSyntaxException(String.format("Unknown block '%s'", key));
        }

        final Block block = ForgeRegistries.BLOCKS.getValue(key);
        if (block == Blocks.AIR) {
            throw new JsonSyntaxException(String.format("Invalid block '%s'", key));
        }

        return Objects.requireNonNull(block);
    }
}
