package com.tycherin.impen.block;

import java.util.Optional;

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
import net.minecraft.world.phys.BlockHitResult;

public class ImaginarySpaceManipulatorBlock extends AEBaseEntityBlock<ImaginarySpaceManipulatorBlockEntity> {

    private static final Logger LOGGER = LogUtils.getLogger();
    
    public ImaginarySpaceManipulatorBlock(final Properties props) {
        super(props);
    }
    
    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p, final InteractionHand hand, @Nullable final ItemStack heldItem, final BlockHitResult hit) {
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            final var be = (ImaginarySpaceManipulatorBlockEntity) level.getBlockEntity(pos);
            if (be != null) {
                if (!level.isClientSide()) {
                    MenuOpener.open(ImaginarySpaceManipulatorMenu.TYPE, p, MenuLocators.forBlockEntity(be));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            else {
                return InteractionResult.PASS;
            }
        }
        else {
          final var beOpt = Optional.of(level.getBlockEntity(pos));
          beOpt.ifPresent(be -> {
              if (!be.isRemoved()) {
                  ((ImaginarySpaceManipulatorBlockEntity) be).triggerOreify();
              }
          });
          return InteractionResult.sidedSuccess(level.isClientSide());
          
          
          // When the above stuff gets removed
          // return InteractionResult.PASS;
        }
    }
}
