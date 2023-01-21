package com.tycherin.impen.datagen;

import java.util.Set;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpenRegistry.BlockLike;

import appeng.datagen.providers.models.AE2BlockStateProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ImpenBlockStateProvider extends AE2BlockStateProvider {

    // These blocks have custom blockstates AND models, so we're going to ignore them
    private static final Set<BlockLike> BLOCKS_WITH_CUSTOM_EVERYTHING = Set.of(
            ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER,
            ImpenRegistry.BEAMED_NETWORK_LINK,
            ImpenRegistry.EJECTION_DRIVE);
    // These blocks have custom models but not blockstates
    private static final Set<BlockLike> BLOCKS_WITH_CUSTOM_MODEL = Set.of(
            ImpenRegistry.POSSIBILITY_DISINTEGRATOR,
            ImpenRegistry.SPATIAL_RIFT_SPAWNER,
            ImpenRegistry.SPATIAL_RIFT_MANIPULATOR,
            ImpenRegistry.SPATIAL_RIFT_COLLAPSER);

    public ImpenBlockStateProvider(final DataGenerator generator, final String modid,
            final ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ImpenRegistry.getRegisteredBlocks().stream()
                .filter(blockLike -> !BLOCKS_WITH_CUSTOM_EVERYTHING.contains(blockLike))
                .forEach(blockLike -> {
                    final Block block = blockLike.asBlock();
                    if (BLOCKS_WITH_CUSTOM_MODEL.contains(blockLike)) {
                        simpleBlock(block, models().getExistingFile(block.getRegistryName()));
                    }
                    else {
                        simpleBlock(block, cubeAll(block));
                    }
                });
    }
}
