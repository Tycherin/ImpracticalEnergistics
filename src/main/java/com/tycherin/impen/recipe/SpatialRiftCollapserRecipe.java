package com.tycherin.impen.recipe;

import javax.annotation.Nullable;

import com.google.gson.Gson;
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
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

@Getter
@AllArgsConstructor
public class SpatialRiftCollapserRecipe implements SpecialRecipe {

    @NoSerialize
    private ResourceLocation id;
    private Ingredient input;
    private ItemStack resultItem;

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
    public SpatialRiftCollapserRecipe.Serializer getSerializer() {
        return SpatialRiftCollapserRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ImpenRegistry.SPATIAL_RIFT_COLLAPSER_RECIPE_TYPE.get();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements RecipeSerializer<SpatialRiftCollapserRecipe> {

        public static final SpatialRiftCollapserRecipe.Serializer INSTANCE = new SpatialRiftCollapserRecipe.Serializer();

        private final Gson gson = GsonUtil.getStandardGson();

        @Override
        public SpatialRiftCollapserRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
            final var recipe = gson.fromJson(json, SpatialRiftCollapserRecipe.class);
            recipe.id = recipeId;
            return recipe;
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
