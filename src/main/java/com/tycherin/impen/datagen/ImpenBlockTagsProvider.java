package com.tycherin.impen.datagen;

import com.tycherin.impen.ImpenRegistry;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ImpenBlockTagsProvider extends BlockTagsProvider {

    public ImpenBlockTagsProvider(final DataGenerator gen, final String modId, final ExistingFileHelper efh) {
        super(gen, modId, efh);
    }

    @Override
    protected void addTags() {
        this.tag(Tags.Blocks.GLASS)
                .add(ImpenRegistry.RIFT_GLASS.block());

        addDenseOre(ImpenRegistry.RIFT_SHARD_ORE.asBlock());
        addSingularOre(ImpenRegistry.NETHER_GLOWSTONE_ORE.asBlock());
        addSingularOre(ImpenRegistry.NETHER_DEBRIS_ORE.asBlock());
        addSingularOre(ImpenRegistry.END_AMETHYST_ORE.asBlock());

        this.tag(Tags.Blocks.ORE_BEARING_GROUND_NETHERRACK)
                .add(ImpenRegistry.NETHER_GLOWSTONE_ORE.block())
                .add(ImpenRegistry.NETHER_DEBRIS_ORE.block());

        this.tag(Tags.Blocks.STORAGE_BLOCKS)
                .add(ImpenRegistry.AEROCRYSTAL_BLOCK.block())
                .add(ImpenRegistry.BLAZING_AEROCRYSTAL_BLOCK.block())
                .add(ImpenRegistry.EXOTIC_AEROCRYSTAL_BLOCK.block())
                .add(ImpenRegistry.RIFT_SHARD_BLOCK.block());
    }

    private void addSingularOre(final Block block) {
        this.tag(Tags.Blocks.ORES)
                .add(block);
        this.tag(Tags.Blocks.ORE_RATES_SINGULAR)
                .add(block);
    }

    private void addDenseOre(final Block block) {
        this.tag(Tags.Blocks.ORES)
                .add(block);
        this.tag(Tags.Blocks.ORE_RATES_DENSE)
                .add(block);
    }
}
