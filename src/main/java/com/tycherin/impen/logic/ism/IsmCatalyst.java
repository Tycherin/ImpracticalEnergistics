package com.tycherin.impen.logic.ism;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class IsmCatalyst {

    // TODO Make configurable?
    public static final double DIMINISHING_RETURNS_RATE = 0.75;
    
    private static final Map<Item, Collection<IsmWeight>> CATALYSTS;
    static {
        // TODO Move this to a recipe or something
        CATALYSTS = ImmutableMap.<Item, Collection<IsmWeight>>builder()
                .put(Items.APPLE,
                        Lists.newArrayList(new IsmWeight(Blocks.ACACIA_WOOD, 300),
                                new IsmWeight(Blocks.ACACIA_LOG, 100)))
                .put(Items.IRON_INGOT, Lists.newArrayList(new IsmWeight(Blocks.IRON_ORE, 100)))
                .build();
    }

    public static boolean isCatalyst(final ItemStack is) {
        if (is.isEmpty()) {
            return false;
        }
        else {
            return CATALYSTS.containsKey(is.getItem());
        }
    }

    public static Collection<IsmWeight> getWeights(final Item item) {
        if (!CATALYSTS.containsKey(item)) {
            throw new IllegalArgumentException(String.format("Item %s is not a registered catalyst!", item));
        }
        return CATALYSTS.get(item);
    }
}
