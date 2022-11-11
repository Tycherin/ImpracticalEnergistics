package com.tycherin.impen.datagen;

import com.tycherin.impen.ImpenRegistry;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
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
        
        this.tag(Tags.Blocks.ORES)
            .add(ImpenRegistry.RIFT_SHARD_ORE.block());
        this.tag(Tags.Blocks.ORE_RATES_DENSE)
            .add(ImpenRegistry.RIFT_SHARD_ORE.block());
        
        this.tag(Tags.Blocks.STORAGE_BLOCKS)
            .add(ImpenRegistry.AEROCRYSTAL_BLOCK.block())
            .add(ImpenRegistry.BLAZING_AEROCRYSTAL_BLOCK.block())
            .add(ImpenRegistry.EXOTIC_AEROCRYSTAL_BLOCK.block())
            .add(ImpenRegistry.RIFT_SHARD_BLOCK.block());
    }
}
