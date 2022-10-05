package com.tycherin.impen.logic.ism;

import java.util.Optional;

import net.minecraft.world.item.Item;

public interface IsmCatalystProvider {

    /** @return an ID that is unique across an instance of the game */
    String getId();

    /** @return an Item that matches a catalyst, or empty if no catalyst is provided */
    Optional<Item> getCatalyst();
}
