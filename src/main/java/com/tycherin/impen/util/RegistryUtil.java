package com.tycherin.impen.util;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class RegistryUtil {

    public static Block getBlock(final JsonObject jsonObject) {
        return RegistryUtil.getBlock(jsonObject, "block");
    }

    public static Block getBlock(final JsonObject jsonObject, final String jsonKey) {
        return RegistryUtil.getBlockOptional(jsonObject, jsonKey)
                .orElseThrow(() -> new JsonSyntaxException(String.format("Unknown block '%s'", jsonKey)));
    }

    public static Optional<Block> getBlockOptional(final JsonObject jsonObject) {
        return RegistryUtil.getBlockOptional(jsonObject, "block");
    }

    public static Optional<Block> getBlockOptional(final JsonObject jsonObject, final String jsonKey) {
        return RegistryUtil.getBlockOptional(GsonHelper.getAsString(jsonObject, jsonKey));
    }

    public static Optional<Block> getBlockOptional(final String registryId) {
        final ResourceLocation key = new ResourceLocation(registryId);
        if (!ForgeRegistries.BLOCKS.containsKey(key)) {
            return Optional.empty();
        }

        final Block block = ForgeRegistries.BLOCKS.getValue(key);
        if (block == Blocks.AIR) {
            return Optional.empty();
        }

        return Optional.of(block);
    }

    public static Item getItem(final JsonObject jsonObject) {
        return RegistryUtil.getItem(jsonObject, "item");
    }

    public static Item getItem(final JsonObject jsonObject, final String jsonKey) {
        return RegistryUtil.getItemOptional(GsonHelper.getAsString(jsonObject, jsonKey))
                .orElseThrow(() -> new JsonSyntaxException(String.format("Unknown item '%s'", jsonKey)));
    }

    public static Optional<Item> getItemOptional(final String registryId) {
        final ResourceLocation key = new ResourceLocation(registryId);
        if (!ForgeRegistries.ITEMS.containsKey(key)) {
            return Optional.empty();
        }

        return Optional.of(ForgeRegistries.ITEMS.getValue(key));
    }
}
