package com.tycherin.impen.block.rift;

import javax.annotation.Nullable;

import com.tycherin.impen.blockentity.rift.SpatialRiftSpawnerBlockEntity;
import com.tycherin.impen.client.gui.SpatialRiftSpawnerMenu;

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

public class SpatialRiftSpawnerBlock extends AEBaseEntityBlock<SpatialRiftSpawnerBlockEntity> {

    public SpatialRiftSpawnerBlock(final Properties props) {
        super(props);
        props.requiresCorrectToolForDrops();
    }
    
    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p,
            final InteractionHand hand, @Nullable final ItemStack heldItem, final BlockHitResult hit) {
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            final var be = (SpatialRiftSpawnerBlockEntity) level.getBlockEntity(pos);
            if (be != null) {
                if (!level.isClientSide()) {
                    MenuOpener.open(SpatialRiftSpawnerMenu.TYPE, p, MenuLocators.forBlockEntity(be));
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
