package com.tycherin.impen.util;

import com.tycherin.impen.ImpenRegistry.RegistryIdProvider;
import com.tycherin.impen.ImpracticalEnergisticsMod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ImpenIdUtil {
    public static ResourceLocation makeId(final String key) {
        return new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID, key);
    }

    public static ResourceLocation makeId(final RegistryIdProvider obj, final String suffix) {
        return makeId(obj.getKey() + "_" + suffix);
    }

    public static TagKey<Item> getItemTag(final String name) {
        return ItemTags.create(ImpenIdUtil.makeId(name));
    }
}
