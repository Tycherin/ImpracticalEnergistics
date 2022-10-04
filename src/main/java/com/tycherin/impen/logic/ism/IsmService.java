package com.tycherin.impen.logic.ism;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import appeng.api.networking.GridServices;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

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
    
    private static final Logger LOGGER = LogUtils.getLogger();

    /** This method should be called during mod initialization */
    public static void init() {
        GridServices.register(IsmService.class, IsmService.class);
    }

    private final IsmWeightTracker weightTracker;

    public IsmService() {
        // TODO Make the tag configurable
        // TODO I'm not sure how this interacts with registry initialization timing?
        final TagKey<Block> oreTagKey = Tags.Blocks.ORES;
        final ITag<Block> oreTag = ForgeRegistries.BLOCKS.tags().getTag(oreTagKey);
        final List<Block> blocks = oreTag.stream().collect(Collectors.toList());
        this.weightTracker = new IsmWeightTracker(Blocks.STONE, blocks);
    }

    @Override
    public void onServerStartTick() {
        this.weightTracker.updateIfNeeded();
    }

    @Override
    public void addNode(final IGridNode node) {
        if (node.getOwner() instanceof IsmWeightProvider) {
            LOGGER.info("Adding IsmWeightProvider: {}", ((IsmWeightProvider) (node.getOwner())).getId());
            this.weightTracker.add((IsmWeightProvider) (node.getOwner()));
        }
    }

    @Override
    public void removeNode(final IGridNode node) {
        if (node.getOwner() instanceof IsmWeightProvider) {
            LOGGER.info("Removing IsmWeightProvider: {}", ((IsmWeightProvider) (node.getOwner())).getId());
            this.weightTracker.remove((IsmWeightProvider) (node.getOwner()));
        }
    }

    public Map<Block, Integer> getweights() {
        return this.weightTracker.getWeights();
    }

    public Supplier<Block> getBlockSupplier() {
        return this.weightTracker.getSupplier();
    }
}
