package com.tycherin.impen.logic.rift;

import java.util.Optional;

import com.tycherin.impen.recipe.RiftCatalystRecipe;

public interface RiftCatalystRecipeSource {

    /** @return an ID that is unique across an instance of the game */
    String getId();

    /** @return A list of recipes that this provider can supply; empty if none are available */
    Optional<RiftCatalystRecipe> getRecipe();

    /**
     * @param desiredAmount the maximum count of recipes that should be consumed
     * @return A list of recipes that were consumed; empty if none were consumed
     */
    Optional<RiftCatalystRecipe> consumeRecipe();
}
