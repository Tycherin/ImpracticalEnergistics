package com.tycherin.impen.logic.rift;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import appeng.api.networking.GridServices;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.IManagedGridNode;
import appeng.me.helpers.IGridConnectedBlockEntity;

/**
 * Grid-wide service implementing Spatial Rift Manipulator (SRM) functionality. Tracks nodes that add SRM information
 * and makes the aggregate results available to consumers.
 * 
 * There are probably other ways I could have implemented this, but using an {@link IGridService} seemed cool.
 * 
 * @author Tycherin
 *
 */
public class RiftService implements IGridService, IGridServiceProvider {

    public static final int MAX_CATALYSTS = 9;

    /** This method should be called during mod initialization */
    public static void init() {
        GridServices.register(RiftService.class, RiftService.class);
    }

    private final Map<String, RiftCatalystRecipeSource> providers = new HashMap<>();

    @Override
    public void addNode(final IGridNode node) {
        if (node.getOwner() instanceof RiftCatalystRecipeSource) {
            final var provider = (RiftCatalystRecipeSource) (node.getOwner());
            this.providers.put(provider.getId(), provider);
        }
    }

    @Override
    public void removeNode(final IGridNode node) {
        if (node.getOwner() instanceof RiftCatalystRecipeSource) {
            final var provider = (RiftCatalystRecipeSource) (node.getOwner());
            this.providers.remove(provider.getId());
        }
    }

    /**
     * Triggers an ISM cycle by consuming catalysts from all providers.
     * 
     * @param doAction True to consume ingredients; false to simulate the action
     * @return A map with the count of all recipes consumed by this operation
     */
    public RiftManipulatorInput doOperation(final boolean doAction) {
        final var recipeCounts = providers.values().stream()
                .map(source -> {
                    if (doAction) {
                        return source.consumeRecipe();
                    }
                    else {
                        return source.getRecipe();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                // Translation: count the number of occurrences of each recipe and put that in a map
                .collect(Collectors.toMap(Function.identity(), (val) -> 1, (a, b) -> a + b));
        return RiftManipulatorInput.of(recipeCounts);
    }

    /** @return True if any recipes are available on the network */
    public boolean hasRecipes() {
        return this.providers.values().stream()
                .map(RiftCatalystRecipeSource::getRecipe)
                .anyMatch(Optional::isPresent);
    }

    /** Gets the {@link RiftService} associated with a grid node, if one exists */
    public static Optional<RiftService> get(final IGridConnectedBlockEntity be) {
        return Optional.ofNullable(be.getMainNode())
                .map(IManagedGridNode::getGrid)
                .map(grid -> grid.getService(RiftService.class));
    }
}
