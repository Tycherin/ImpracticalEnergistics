package com.tycherin.impen.block;

import javax.annotation.Nullable;

import com.tycherin.impen.blockentity.BEBeamedNetworkLink;

import appeng.block.AEBaseEntityBlock;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BlockBeamedNetworkLink extends AEBaseEntityBlock<BEBeamedNetworkLink> {
    private static final BooleanProperty PROP_IS_ACTIVE = BooleanProperty.create("is_active");
    private static final EnumProperty<Direction> PROP_FACING = EnumProperty.create("facing", Direction.class);

    public BlockBeamedNetworkLink(final BlockBehaviour.Properties props) {
        super(props);
        props.requiresCorrectToolForDrops();

        this.registerDefaultState(this.defaultBlockState()
                .setValue(PROP_IS_ACTIVE, false)
                .setValue(PROP_FACING, Direction.NORTH));
    }

    @Override
    public void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder
                .add(PROP_IS_ACTIVE)
                .add(PROP_FACING);
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(final BlockState currentState, final BEBeamedNetworkLink be) {
        return currentState
                .setValue(PROP_IS_ACTIVE, be.isActive())
                .setValue(PROP_FACING, be.getForward());
    }

    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player player,
            final InteractionHand hand, @Nullable final ItemStack heldItem, final BlockHitResult hit) {
        return super.onActivated(level, pos, player, hand, heldItem, hit);
    }
}
