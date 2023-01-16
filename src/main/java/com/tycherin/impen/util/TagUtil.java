package com.tycherin.impen.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

public class TagUtil {

    public static ITag<Block> getBlockTag(final TagKey<Block> tag) {
        return ForgeRegistries.BLOCKS.tags().getTag(tag);
    }

    public static ITag<Item> getItemTag(final TagKey<Item> tag) {
        return ForgeRegistries.ITEMS.tags().getTag(tag);
    }
}
