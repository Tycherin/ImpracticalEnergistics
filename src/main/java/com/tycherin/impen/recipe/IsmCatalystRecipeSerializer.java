package com.tycherin.impen.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.tycherin.impen.logic.ism.IsmWeight;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class IsmCatalystRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<IsmCatalystRecipe> {

    public static final IsmCatalystRecipeSerializer INSTANCE = new IsmCatalystRecipeSerializer();

    private static final int MAX_OUTPUTS = 8;

    private IsmCatalystRecipeSerializer() {
    }

    @Override
    public IsmCatalystRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {

        final Item catalyst = ShapedRecipe.itemFromJson(GsonHelper.getAsJsonObject(json, "catalyst_item"));
        final Block baseBlock = this.getAsBlock(GsonHelper.getAsJsonObject(json, "base_block"));
        final List<IsmWeight> weights = new ArrayList<>();
        // TODO Add dimension restrictions... somehow?
        final JsonArray blockWeightsJson = GsonHelper.getAsJsonArray(json, "block_weights");
        if (blockWeightsJson.size() > MAX_OUTPUTS) {
            throw new JsonSyntaxException(String.format("Too many outputs for '%s'", recipeId));
        }
        blockWeightsJson.forEach(weightJson -> {
            if (!weightJson.isJsonObject()) {
                throw new JsonSyntaxException(String.format("Invalid weight '%s'", weightJson));
            }
            final JsonObject weightObj = weightJson.getAsJsonObject();
            final Block weightBlock = this.getAsBlock(weightObj);
            final Double weightValue = GsonHelper.getAsDouble(weightObj, "value");
            if ((weightValue > 100) || (weightValue < 0)) {
                throw new JsonSyntaxException(String.format("Weights must be between 100 and 0 '%s'", weightValue));
            }
            weights.add(new IsmWeight(weightBlock, weightValue));
        });

        return new IsmCatalystRecipe(recipeId, catalyst, baseBlock, weights);
    }

    @Nullable
    @Override
    public IsmCatalystRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
        final Item catalyst = buffer.readItem().getItem();
        final Block baseBlock = ForgeRegistries.BLOCKS.getValue(buffer.readRegistryId());
        final int weightCount = buffer.readInt();
        final List<IsmWeight> weights = new ArrayList<>();
        for (int i = 0; i < weightCount; i++) {
            final Block weightBlock = ForgeRegistries.BLOCKS.getValue(buffer.readRegistryId());
            final Double weightValue = buffer.readDouble();
            weights.add(new IsmWeight(weightBlock, weightValue));
        }

        return new IsmCatalystRecipe(recipeId, catalyst, baseBlock, weights);
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buffer, final IsmCatalystRecipe recipe) {
        buffer.writeItem(recipe.getCatalyst().getDefaultInstance());
        buffer.writeRegistryId(recipe.getBaseBlock());
        buffer.writeInt(recipe.getWeights().size());
        recipe.getWeights().forEach(weight -> {
            buffer.writeRegistryId(weight.block());
            buffer.writeDouble(weight.probability());
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
