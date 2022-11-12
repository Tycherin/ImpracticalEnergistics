package com.tycherin.impen.util;

import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.ImpenRegistry.RegistryIdProvider;

import net.minecraft.resources.ResourceLocation;

public class ImpenIdUtil {
    public static ResourceLocation makeId(final String key) {
        return new ResourceLocation(ImpracticalEnergisticsMod.MOD_ID, key);
    }

    public static ResourceLocation makeId(final RegistryIdProvider obj, final String suffix) {
        return makeId(obj.getKey() + "_" + suffix);
    }
}
