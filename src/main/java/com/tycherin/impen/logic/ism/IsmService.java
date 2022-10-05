package com.tycherin.impen.logic.ism;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.tycherin.impen.logic.ism.IsmWeightTracker.IsmWeightWrapper;

import appeng.api.networking.GridServices;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.IManagedGridNode;
import appeng.me.helpers.IGridConnectedBlockEntity;

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

    /** This method should be called during mod initialization */
    public static void init() {
        GridServices.register(IsmService.class, IsmService.class);
    }

    private final Map<String, IsmCatalystProvider> providers = new HashMap<>();
    private final IsmWeightTracker weightTracker = new IsmWeightTracker();

    @Override
    public void addNode(final IGridNode node) {
        if (node.getOwner() instanceof IsmCatalystProvider) {
            final var provider = (IsmCatalystProvider) (node.getOwner());
            this.providers.put(provider.getId(), provider);
            this.weightTracker.addProvider(provider);
        }
    }

    @Override
    public void removeNode(final IGridNode node) {
        if (node.getOwner() instanceof IsmCatalystProvider) {
            final var provider = (IsmCatalystProvider) (node.getOwner());
            this.providers.remove(provider.getId());
            this.weightTracker.removeProvider(provider);
        }
    }

    /** Called by {@link IsmCatalystProvider} when the provider has been updated */
    public void updateProvider(final IsmCatalystProvider provider) {
        this.weightTracker.updateProvider(provider);
    }

    public IsmWeightWrapper getWeights() {
        return this.weightTracker.getWeights();
    }

    /** Gets the {@link IsmService} associated with a grid node, if one exists */
    public static Optional<IsmService> get(final IGridConnectedBlockEntity be) {
        return Optional.ofNullable(be.getMainNode())
                .map(IManagedGridNode::getGrid)
                .map(grid -> grid.getService(IsmService.class));
    }
}
