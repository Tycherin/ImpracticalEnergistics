package com.tycherin.impen.util;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tycherin.impen.annotate.NoSerialize;

import appeng.datagen.providers.recipes.AE2RecipeProvider;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.JsonUtils;
import net.minecraftforge.registries.ForgeRegistries;

public class GsonUtil {

    private static final Gson STANDARD_INSTANCE = getStandardGsonBuilder().create();

    public static Gson getStandardGson() {
        return STANDARD_INSTANCE;
    }

    public static GsonBuilder getStandardGsonBuilder() {
        return new GsonBuilder()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(ImmutableList.class, JsonUtils.ImmutableListTypeAdapter.INSTANCE)
                .registerTypeAdapter(ImmutableMap.class, JsonUtils.ImmutableMapTypeAdapter.INSTANCE)
                .registerTypeAdapter(ItemStack.class, new ItemStackSerDe())
                .registerTypeHierarchyAdapter(Ingredient.class, new IngredientSerDe())
                .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocationSerDe())
                .registerTypeHierarchyAdapter(Block.class, new BlockSerDe())
                .registerTypeAdapter(MockBlock.class, new MockBlockSerializer())
                .registerTypeAdapter(MockIngredient.class, new MockIngredientSerializer())
                .addSerializationExclusionStrategy(new NoSerializeFilter())
                .addDeserializationExclusionStrategy(new NoSerializeFilter());
    }

    private static class NoSerializeFilter implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(NoSerialize.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz.isAnnotationPresent(NoSerialize.class);
        }
    }

    public static interface GsonSerDe<T> extends JsonSerializer<T>, JsonDeserializer<T> {
        // Just reducing some boilerplate by grouping these together
    }

    private static class ItemStackSerDe implements GsonSerDe<ItemStack> {
        @Override
        public ItemStack deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context)
                throws JsonParseException {
            return ShapedRecipe.itemStackFromJson((JsonObject)json);
        }

        @Override
        public JsonElement serialize(final ItemStack src, final Type type, final JsonSerializationContext context) {
            return AE2RecipeProvider.toJson(src);
        }
    }

    private static class IngredientSerDe implements GsonSerDe<Ingredient> {
        @Override
        public Ingredient deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context)
                throws JsonParseException {
            return Ingredient.fromJson(json);
        }

        @Override
        public JsonElement serialize(final Ingredient src, final Type type, final JsonSerializationContext context) {
            return src.toJson();
        }
    }

    private static class ResourceLocationSerDe implements GsonSerDe<ResourceLocation> {
        @Override
        public JsonElement serialize(final ResourceLocation src, final Type typeOfSrc,
                final JsonSerializationContext context) {
            return context.serialize(src.toString());
        }

        @Override
        public ResourceLocation deserialize(final JsonElement json, final Type type,
                final JsonDeserializationContext context)
                throws JsonParseException {
            return new ResourceLocation(json.getAsString());
        }
    }
    
    private static class BlockSerDe implements GsonSerDe<Block> {
        @Override
        public JsonElement serialize(final Block src, final Type typeOfSrc, final JsonSerializationContext context) {
            return new JsonPrimitive(src.getRegistryName().toString());
        }

        @Override
        public Block deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context)
                throws JsonParseException {
            return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(json.getAsString()));
        }
    }

    @RequiredArgsConstructor
    public static class MockBlock {
        private final String blockId;
    }

    private static class MockBlockSerializer implements JsonSerializer<MockBlock> {
        @Override
        public JsonElement serialize(final MockBlock src, final Type typeOfSrc,
                final JsonSerializationContext context) {
            return new JsonPrimitive(src.blockId);
        }
    }

    @RequiredArgsConstructor
    public static class MockIngredient {
        private final List<String> ingredientIds;

        public MockIngredient(final String ingredientId) {
            this.ingredientIds = Collections.singletonList(ingredientId);
        }
    }

    private static class MockIngredientSerializer implements JsonSerializer<MockIngredient> {
        @Override
        public JsonElement serialize(final MockIngredient src, final Type typeOfSrc,
                final JsonSerializationContext context) {
            if (src.ingredientIds.size() == 1) {
                return serializeItem(src.ingredientIds.get(0));
            }
            else {
                final JsonArray jsonarray = new JsonArray();
                for (final String ingredientId : src.ingredientIds) {
                    jsonarray.add(serializeItem(ingredientId));
                }
                return jsonarray;
            }
        }

        private JsonElement serializeItem(final String ingredientId) {
            final JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("item", ingredientId);
            return jsonobject;
        }
    }
}
