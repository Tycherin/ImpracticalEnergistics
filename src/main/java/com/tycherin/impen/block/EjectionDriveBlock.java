package com.tycherin.impen.block;

import javax.annotation.Nullable;

import com.tycherin.impen.blockentity.EjectionDriveBlockEntity;

import appeng.block.AEBaseEntityBlock;
import appeng.core.definitions.AEBlocks;
import appeng.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class EjectionDriveBlock extends AEBaseEntityBlock<EjectionDriveBlockEntity> {

    private static final EnumProperty<Direction> PROP_FACING = EnumProperty.create("facing", Direction.class);

    public EjectionDriveBlock(final BlockBehaviour.Properties ignored) {
        super(BlockBehaviour.Properties.copy(AEBlocks.CHEST.block()));
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
            final EjectionDriveBlockEntity be) {
        return currentState
                .setValue(PROP_FACING, be.getForward());
    }

    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p,
            final InteractionHand hand, @Nullable final ItemStack heldItem, final BlockHitResult hit) {
        final var be = this.getBlockEntity(level, pos);
        if (be != null && !InteractionUtil.isInAlternateUseMode(p)
                && hit.getDirection().equals(be.getForward().getOpposite())) {
            if (!level.isClientSide()) {
                be.openCellInventoryMenu(p);
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }
}
