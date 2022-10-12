package com.tycherin.impen.block;

import javax.annotation.Nullable;

import com.tycherin.impen.blockentity.ToasterDriveBlockEntity;

import appeng.block.AEBaseEntityBlock;
import appeng.core.definitions.AEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;

public class ToasterDriveBlock extends AEBaseEntityBlock<ToasterDriveBlockEntity> {

    public ToasterDriveBlock() {
        super(BlockBehaviour.Properties.copy(AEBlocks.CHEST.block()));
        this.registerDefaultState(this.defaultBlockState());
    }

    @Override
    public InteractionResult onActivated(Level level, BlockPos pos, Player p,
            InteractionHand hand,
            @Nullable ItemStack heldItem, BlockHitResult hit) {
        return AEBlocks.CHEST.block().onActivated(level, pos, p, hand, heldItem, hit);
    }
}
