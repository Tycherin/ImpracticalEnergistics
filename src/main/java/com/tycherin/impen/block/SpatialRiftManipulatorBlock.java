package com.tycherin.impen.block;

import javax.annotation.Nullable;

import com.tycherin.impen.blockentity.SpatialRiftManipulatorBlockEntity;
import com.tycherin.impen.client.gui.SpatialRiftManipulatorMenu;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SpatialRiftManipulatorBlock extends AEBaseEntityBlock<SpatialRiftManipulatorBlockEntity> {

    public SpatialRiftManipulatorBlock(final Properties props) {
        super(props);
    }

    @Override
    public void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block blockIn,
            final BlockPos fromPos, final boolean isMoving) {
        final SpatialRiftManipulatorBlockEntity be = this.getBlockEntity(level, pos);
        if (be != null) {
            be.updateRedstoneState();
        }
    }

    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player player,
            final InteractionHand hand, @Nullable final ItemStack heldItem, final BlockHitResult hit) {
        if (!InteractionUtil.isInAlternateUseMode(player)) {
            final SpatialRiftManipulatorBlockEntity be = this.getBlockEntity(level, pos);
            if (be != null) {
                if (!level.isClientSide()) {
                    MenuOpener.open(SpatialRiftManipulatorMenu.TYPE, player, MenuLocators.forBlockEntity(be));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }
}
