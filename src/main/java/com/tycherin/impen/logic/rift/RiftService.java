package com.tycherin.impen.logic.rift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import appeng.api.networking.GridServices;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.IManagedGridNode;
import appeng.me.helpers.IGridConnectedBlockEntity;
import net.minecraft.world.item.ItemStack;

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

    private final Map<String, RiftCatalystProvider> providers = new HashMap<>();

    @Override
    public void addNode(final IGridNode node) {
        if (node.getOwner() instanceof RiftCatalystProvider) {
            final var provider = (RiftCatalystProvider) (node.getOwner());
            this.providers.put(provider.getId(), provider);
        }
    }

    @Override
    public void removeNode(final IGridNode node) {
        if (node.getOwner() instanceof RiftCatalystProvider) {
            final var provider = (RiftCatalystProvider) (node.getOwner());
            this.providers.remove(provider.getId());
        }
    }

    /**
     * Triggers an ISM cycle by consuming catalysts from all providers.
     * 
     * @param desiredAmount the maximum amount to be consumed from each provider
     * @return A list of all catalysts that were consumed by this operation
     */
    public List<ItemStack> triggerOperation(final int desiredAmount) {
        final List<ItemStack> items = new ArrayList<>();

        for (final var provider : providers.values()) {
            if (items.size() >= MAX_CATALYSTS) {
                break;
            }

            final ItemStack catalyst = provider.consumeCatalyst(desiredAmount);
            if (!catalyst.isEmpty()) {
                items.add(catalyst);
            }
        }
        return items;
    }

    /** @return True if any catalysts are provided to the network */
    public boolean hasCatalysts() {
        return this.providers.values().stream().anyMatch(provider -> !provider.getCatalyst().isEmpty());
    }

    /** Gets the {@link RiftService} associated with a grid node, if one exists */
    public static Optional<RiftService> get(final IGridConnectedBlockEntity be) {
        return Optional.ofNullable(be.getMainNode())
                .map(IManagedGridNode::getGrid)
                .map(grid -> grid.getService(RiftService.class));
    }
}
