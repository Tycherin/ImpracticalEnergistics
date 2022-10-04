package com.tycherin.impen.block;

import javax.annotation.Nullable;

import com.tycherin.impen.blockentity.ImaginarySpaceStabilizerBlockEntity;

import appeng.block.AEBaseEntityBlock;
import appeng.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class ImaginarySpaceStabilizerBlock extends AEBaseEntityBlock<ImaginarySpaceStabilizerBlockEntity>{

    public ImaginarySpaceStabilizerBlock(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p,
            final InteractionHand hand, @Nullable final ItemStack heldItem, final BlockHitResult hit) {
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            // TODO Add this stuff in once the menu is implemented
//            final var be = (ImaginarySpaceManipulatorBlockEntity) level.getBlockEntity(pos);
//            if (be != null) {
//                if (!level.isClientSide()) {
//                    MenuOpener.open(ImaginarySpaceManipulatorMenu.TYPE, p, MenuLocators.forBlockEntity(be));
//                }
//                return InteractionResult.sidedSuccess(level.isClientSide());
//            }
//            else {
//                return InteractionResult.PASS;
//            }
        }

        return InteractionResult.PASS;
    }
}
