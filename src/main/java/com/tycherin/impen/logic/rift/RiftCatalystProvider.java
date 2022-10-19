package com.tycherin.impen.logic.rift;

import net.minecraft.world.item.ItemStack;

public interface RiftCatalystProvider {

    /** @return an ID that is unique across an instance of the game */
    String getId();

    /** @return an Item that matches a catalyst, or empty if no catalyst is provided */
    ItemStack getCatalyst();

    /**
     * @param desiredAmount the maximum number of the catalyst that should be consumed
     * @return a catalyst item, if consumed, or empty if no catalyst was consumed
     */
    ItemStack consumeCatalyst(int desiredAmount);
}
