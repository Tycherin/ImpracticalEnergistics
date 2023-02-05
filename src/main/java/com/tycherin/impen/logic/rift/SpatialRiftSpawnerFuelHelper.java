package com.tycherin.impen.logic.rift;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.tycherin.impen.ImpenRegistry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class SpatialRiftSpawnerFuelHelper {

    private static final Map<ItemLike, Integer> FUEL_TYPES_REGISTRY = ImmutableMap.of(
            ImpenRegistry.AEROCRYSTAL, 8,
            ImpenRegistry.AEROCRYSTAL_BLOCK, 8 * 4,
            ImpenRegistry.BLAZING_AEROCRYSTAL, 11,
            ImpenRegistry.BLAZING_AEROCRYSTAL_BLOCK, 11 * 4,
            ImpenRegistry.EXOTIC_AEROCRYSTAL, 15,
            ImpenRegistry.EXOTIC_AEROCRYSTAL_BLOCK, 15 * 4);

    private final Map<Item, Integer> fuelTypes;

    public SpatialRiftSpawnerFuelHelper() {
        this.fuelTypes = new HashMap<>();
        // This should only be done at runtime, or else asItem() will blow up because of registry timing
        FUEL_TYPES_REGISTRY.forEach((itemLike, fuelValue) -> {
            fuelTypes.put(itemLike.asItem(), fuelValue);
        });
    }

    public boolean isFuel(final ItemStack is) {
        return isFuel(is.getItem());
    }

    public boolean isFuel(final Item item) {
        return fuelTypes.containsKey(item);
    }

    public int getValue(final Item item) {
        if (!isFuel(item)) {
            throw new RuntimeException("Item " + item + " is not a valid fuel type");
        }
        return fuelTypes.get(item);
    }

    public int getValue(final ItemStack is) {
        return getValue(is.getItem()) * is.getCount();
    }
}
