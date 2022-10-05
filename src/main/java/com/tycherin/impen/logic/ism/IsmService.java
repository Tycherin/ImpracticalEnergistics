package com.tycherin.impen.logic.ism;

import java.util.ArrayList;
import java.util.Collection;
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
import net.minecraft.world.item.Item;

/**
 * Grid-wide service implementing Imaginary Space Manipulator (ISM) functionality. Tracks nodes that add ISM information
 * and makes the aggregate results available to consumers.
 * 
 * There are probably other ways I could have implemented this, but using an {@link IGridService} seemed cool.
 * 
 * @author Tycherin
 *
 */
public class IsmService implements IGridService, IGridServiceProvider {

    public static final int MAX_CATALYSTS = 9;
    
    /** This method should be called during mod initialization */
    public static void init() {
        GridServices.register(IsmService.class, IsmService.class);
    }

    private final Map<String, IsmCatalystProvider> providers = new HashMap<>();

    @Override
    public void addNode(final IGridNode node) {
        if (node.getOwner() instanceof IsmCatalystProvider) {
            final var provider = (IsmCatalystProvider) (node.getOwner());
            this.providers.put(provider.getId(), provider);
        }
    }

    @Override
    public void removeNode(final IGridNode node) {
        if (node.getOwner() instanceof IsmCatalystProvider) {
            final var provider = (IsmCatalystProvider) (node.getOwner());
            this.providers.remove(provider.getId());
        }
    }

    /**
     * Triggers an ISM cycle by consuming catalysts from all providers.
     * 
     * @return A list of all catalysts that were consumed by this operation
     */
    public Collection<Item> triggerOperation() {
        final List<Item> items = new ArrayList<>();
        
        for (final IsmCatalystProvider provider : providers.values()) {
            if (items.size() >= MAX_CATALYSTS) {
                break;
            }
            
            provider.consumeCatalyst().ifPresent(catalyst -> {
                items.add(catalyst);
            });
        }
        return items;
    }

    /** Gets the {@link IsmService} associated with a grid node, if one exists */
    public static Optional<IsmService> get(final IGridConnectedBlockEntity be) {
        return Optional.ofNullable(be.getMainNode())
                .map(IManagedGridNode::getGrid)
                .map(grid -> grid.getService(IsmService.class));
    }
}
