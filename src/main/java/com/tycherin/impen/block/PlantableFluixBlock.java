package com.tycherin.impen.block;

import com.tycherin.impen.ImpenRegistry;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BeetrootBlock;

public class PlantableFluixBlock extends BeetrootBlock {

    public PlantableFluixBlock(final Properties props) {
        super(props);
    }
    
    @Override
    protected ItemLike getBaseSeedId() {
        return ImpenRegistry.PLANTABLE_FLUIX.item();
     }
}
