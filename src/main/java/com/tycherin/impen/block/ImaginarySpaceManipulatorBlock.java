package com.tycherin.impen.block;

import java.util.Optional;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.blockentity.ImaginarySpaceManipulatorBlockEntity;

import appeng.block.AEBaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

// TODO Should we subclass SpatialIOPortBlock directly?
public class ImaginarySpaceManipulatorBlock extends AEBaseEntityBlock<ImaginarySpaceManipulatorBlockEntity> {

    private static final Logger LOGGER = LogUtils.getLogger();
    
    public ImaginarySpaceManipulatorBlock(final Properties props) {
        super(props);
    }
    
    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p, final InteractionHand hand, @Nullable ItemStack heldItem, final BlockHitResult hit) {
        LOGGER.info("ISM activated!");
        final var beOpt = Optional.of(level.getBlockEntity(pos));
        beOpt.ifPresent(be -> {
            if (!be.isRemoved()) {
                ((ImaginarySpaceManipulatorBlockEntity)be).oreifySpatialCell();
            }
        });
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
