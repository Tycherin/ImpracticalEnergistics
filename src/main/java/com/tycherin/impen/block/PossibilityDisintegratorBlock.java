package com.tycherin.impen.block;

import com.tycherin.impen.blockentity.PossibilityDisintegratorBlockEntity;

import appeng.block.AEBaseEntityBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class PossibilityDisintegratorBlock extends AEBaseEntityBlock<PossibilityDisintegratorBlockEntity> {

    public PossibilityDisintegratorBlock(final Properties props) {
        super(props);
        props.requiresCorrectToolForDrops();
    }

    @Override
    public void updateEntityAfterFallOn(final BlockGetter blockGetter, final Entity entity) {
        // updateEntityAfterFallOn() and stepOn() seem to be pretty similar in practice. I'm using
        // updateEntityAfterFallOn() simply because stepOn() doesn't trigger if the entity is crouching, which seems
        // like a weird limitation for some sort of high-powered reality distortion device
        super.updateEntityAfterFallOn(blockGetter, entity);
        
        // As near as I can tell, blockGetter will always be a ServerLevel for this call, but the check is here just to
        // be on the safe side
        if (blockGetter instanceof Level level && !level.isClientSide()) {
            final var be = level.getBlockEntity(entity.getOnPos());
            if (be != null && be instanceof PossibilityDisintegratorBlockEntity pdbe) {
                pdbe.handleEntity(entity);
            }
        }
    }
}
