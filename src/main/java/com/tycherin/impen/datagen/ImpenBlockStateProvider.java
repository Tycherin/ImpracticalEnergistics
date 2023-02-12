package com.tycherin.impen.datagen;

import java.util.Set;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpenRegistry.BlockDefinition;
import com.tycherin.impen.ImpenRegistry.BlockLike;
import com.tycherin.impen.util.ImpenIdUtil;

import appeng.datagen.providers.models.AE2BlockStateProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ImpenBlockStateProvider extends AE2BlockStateProvider {

    // These blocks have custom blockstates AND models, so we're going to ignore them
    private static final Set<BlockLike> BLOCKS_WITH_CUSTOM_EVERYTHING = Set.of(
            ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER,
            ImpenRegistry.BEAM_NETWORK_AMPLIFIER,
            ImpenRegistry.BEAM_NETWORK_EMITTER,
            ImpenRegistry.BEAM_NETWORK_MIRROR,
            ImpenRegistry.BEAM_NETWORK_RECEIVER,
            ImpenRegistry.BEAM_NETWORK_SPLITTER,
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
                .filter(blockLike -> !(blockLike.asBlock() instanceof StairBlock
                        || blockLike.asBlock() instanceof SlabBlock))
                .forEach(blockLike -> {
                    final Block block = blockLike.asBlock();
                    if (BLOCKS_WITH_CUSTOM_MODEL.contains(blockLike)) {
                        simpleBlock(block, models().getExistingFile(block.getRegistryName()));
                    }
                    else {
                        simpleBlock(block, cubeAll(block));
                    }
                });

        stairsBlock(ImpenRegistry.RIFTSTONE_STAIRS, ImpenRegistry.RIFTSTONE);
        slabBlock(ImpenRegistry.RIFTSTONE_SLAB, ImpenRegistry.RIFTSTONE);
        stairsBlock(ImpenRegistry.SMOOTH_RIFTSTONE_STAIRS, ImpenRegistry.SMOOTH_RIFTSTONE);
        slabBlock(ImpenRegistry.SMOOTH_RIFTSTONE_SLAB, ImpenRegistry.SMOOTH_RIFTSTONE);
        stairsBlock(ImpenRegistry.RIFTSTONE_BRICK_STAIRS, ImpenRegistry.RIFTSTONE_BRICK);
        slabBlock(ImpenRegistry.RIFTSTONE_BRICK_SLAB, ImpenRegistry.RIFTSTONE_BRICK);
    }

    private void slabBlock(final BlockDefinition slab, final BlockDefinition base) {
        final var texture = blockTexture(base.block()).getPath();
        slabBlock(slab, base, texture, texture, texture);
    }

    private void slabBlock(final BlockDefinition slab, final BlockDefinition base, final String bottomTexture,
            final String sideTexture, final String topTexture) {
        final var sideTexturePath = ImpenIdUtil.makeId(sideTexture);
        final var bottomTexturePath = ImpenIdUtil.makeId(bottomTexture);
        final var topTexturePath = ImpenIdUtil.makeId(topTexture);

        final var bottomModel = models().slab(slab.getKey(), sideTexturePath, bottomTexturePath, topTexturePath);
        slabBlock(
                (SlabBlock)slab.block(),
                bottomModel,
                models().slabTop(slab.getKey() + "_top", sideTexturePath, bottomTexturePath, topTexturePath),
                models().getExistingFile(base.asBlock().getRegistryName()));
    }

    private void stairsBlock(final BlockDefinition stairs, final BlockDefinition base) {
        final var texture = "block/" + base.getKey();
        stairsBlock(stairs, texture, texture, texture);
    }

    private void stairsBlock(final BlockDefinition stairs, final String bottomTexture, final String sideTexture,
            final String topTexture) {
        final var baseName = stairs.getKey();

        final var sideTexturePath = ImpenIdUtil.makeId(sideTexture);
        final var bottomTexturePath = ImpenIdUtil.makeId(bottomTexture);
        final var topTexturePath = ImpenIdUtil.makeId(topTexture);

        final ModelFile stairsModel = models().stairs(baseName, sideTexturePath, bottomTexturePath, topTexturePath);
        final ModelFile stairsInner = models().stairsInner(baseName + "_inner", sideTexturePath, bottomTexturePath,
                topTexturePath);
        final ModelFile stairsOuter = models().stairsOuter(baseName + "_outer", sideTexturePath, bottomTexturePath,
                topTexturePath);
        stairsBlock((StairBlock)stairs.block(), stairsModel, stairsInner, stairsOuter);
    }
}
