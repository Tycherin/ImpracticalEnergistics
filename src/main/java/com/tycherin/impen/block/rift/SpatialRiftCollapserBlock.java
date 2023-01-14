package com.tycherin.impen.block.rift;

import javax.annotation.Nullable;

import com.tycherin.impen.blockentity.rift.SpatialRiftCollapserBlockEntity;
import com.tycherin.impen.client.gui.PossibilityDisintegratorMenu;

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
import net.minecraft.world.phys.BlockHitResult;

public class SpatialRiftCollapserBlock extends AEBaseEntityBlock<SpatialRiftCollapserBlockEntity> {

    public SpatialRiftCollapserBlock(final Properties props) {
        super(props);
        props.requiresCorrectToolForDrops();
    }
    
    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p,
            final InteractionHand hand, @Nullable final ItemStack heldItem, final BlockHitResult hit) {
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            final var be = (SpatialRiftCollapserBlockEntity) level.getBlockEntity(pos);
            if (be != null) {
                if (!level.isClientSide()) {
                    // TODO Use correct types here
                    MenuOpener.open(PossibilityDisintegratorMenu.TYPE, p, MenuLocators.forBlockEntity(be));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            else {
                return InteractionResult.PASS;
            }
        }

        return InteractionResult.PASS;
    }
}
