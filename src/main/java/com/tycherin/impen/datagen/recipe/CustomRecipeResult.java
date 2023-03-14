package com.tycherin.impen.datagen.recipe;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.tycherin.impen.util.GsonUtil;
import com.tycherin.impen.util.ImpenIdUtil;

import lombok.NonNull;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

public abstract class CustomRecipeResult<T> implements FinishedRecipe {

    private final String recipeName;
    private final T data;

    public CustomRecipeResult(@NonNull final String recipeName, @NonNull final T data) {
        this.recipeName = recipeName;
        this.data = data;
    }

    @SuppressWarnings("deprecation")
    @Override
    public JsonObject serializeRecipe() {
        final JsonObject json = (JsonObject)GsonUtil.getStandardGson().toJsonTree(data);
        json.addProperty("type", Registry.RECIPE_SERIALIZER.getKey(this.getType()).toString());
        return json;
    }

    @Override
    public void serializeRecipeData(final JsonObject inputJson) {
        // This one is kinda weird. The interface requires us to implement this method, but we override
        // serializeRecipe() rather than calling this, because it's inefficient. Nothing in the vanilla code calls this
        // other than the default implementation of serializeRecipe(), but it's technically part of the interface. I'd
        // rather not break things if I can avoid it, so this implementation is here for that reason.
        final JsonObject json = serializeRecipe();
        json.entrySet().forEach(entry -> {
            inputJson.add(entry.getKey(), entry.getValue());
        });
    }

    @Override
    public ResourceLocation getId() {
        return ImpenIdUtil.makeId(getRecipeHolder().getId().getPath() + "/" + recipeName);
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return null;
    }

    // I'd rather have this as RegistryObject<RecipeType<?>> for added safety, but Java generics doesn't like that
    protected abstract RegistryObject<?> getRecipeHolder();
}