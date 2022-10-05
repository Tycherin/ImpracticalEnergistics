package com.tycherin.impen.block;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.blockentity.ImaginarySpaceManipulatorBlockEntity;
import com.tycherin.impen.client.gui.ImaginarySpaceManipulatorMenu;

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

public class ImaginarySpaceManipulatorBlock extends AEBaseEntityBlock<ImaginarySpaceManipulatorBlockEntity> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public ImaginarySpaceManipulatorBlock(final Properties props) {
        super(props);
    }

    @Override
    public void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block blockIn,
            final BlockPos fromPos, final boolean isMoving) {
        final ImaginarySpaceManipulatorBlockEntity be = this.getBlockEntity(level, pos);
        if (be != null) {
            be.updateRedstoneState();
        }
    }

    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p,
            final InteractionHand hand, @Nullable final ItemStack heldItem, final BlockHitResult hit) {
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            final ImaginarySpaceManipulatorBlockEntity be = this.getBlockEntity(level, pos);
            if (be != null) {
                if (!level.isClientSide()) {
                    MenuOpener.open(ImaginarySpaceManipulatorMenu.TYPE, p, MenuLocators.forBlockEntity(be));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }
}
