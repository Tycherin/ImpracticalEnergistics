package com.tycherin.impen.block;

import com.tycherin.impen.ImpracticalEnergisticsMod;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BeetrootBlock;

public class PlantableCertusBlock extends BeetrootBlock {

    public PlantableCertusBlock(final Properties props) {
        super(props);
    }
    
    @Override
    protected ItemLike getBaseSeedId() {
        return ImpracticalEnergisticsMod.PLANTABLE_CERTUS_SEEDS_ITEM.get();
     }
}
