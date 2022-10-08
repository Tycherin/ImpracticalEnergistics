package com.tycherin.impen.block;

import javax.annotation.Nullable;

import com.tycherin.impen.blockentity.SpatialCrystallizerBlockEntity;
import com.tycherin.impen.client.gui.SpatialCrystallizerMenu;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class SpatialCrystallizerBlock extends AEBaseEntityBlock<SpatialCrystallizerBlockEntity>{

    private static final EnumProperty<Direction> PROP_FACING = EnumProperty.create("facing", Direction.class);
    
    public SpatialCrystallizerBlock(Properties props) {
        super(props);
        
        props.requiresCorrectToolForDrops();

        this.registerDefaultState(this.defaultBlockState()
                .setValue(PROP_FACING, Direction.NORTH));
    }
    
    @Override
    public void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder
                .add(PROP_FACING);
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(final BlockState currentState,
            final SpatialCrystallizerBlockEntity be) {
        return currentState
                .setValue(PROP_FACING, be.getForward());
    }

    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p,
            final InteractionHand hand, @Nullable final ItemStack heldItem, final BlockHitResult hit) {
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            final var be = (SpatialCrystallizerBlockEntity) level.getBlockEntity(pos);
            if (be != null) {
                if (!level.isClientSide()) {
                    MenuOpener.open(SpatialCrystallizerMenu.TYPE, p, MenuLocators.forBlockEntity(be));
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
