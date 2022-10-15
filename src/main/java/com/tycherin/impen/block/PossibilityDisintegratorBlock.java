package com.tycherin.impen.block;

import javax.annotation.Nullable;

import com.tycherin.impen.blockentity.PossibilityDisintegratorBlockEntity;
import com.tycherin.impen.client.gui.PossibilityDisintegratorMenu;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class PossibilityDisintegratorBlock extends AEBaseEntityBlock<PossibilityDisintegratorBlockEntity> {

    public PossibilityDisintegratorBlock(final Properties props) {
        super(props);
        props.requiresCorrectToolForDrops();
    }

    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p,
            final InteractionHand hand, @Nullable final ItemStack heldItem, final BlockHitResult hit) {
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            final var be = (PossibilityDisintegratorBlockEntity) level.getBlockEntity(pos);
            if (be != null) {
                if (!level.isClientSide()) {
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
