package com.tycherin.impen.blockentity;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import com.tycherin.impen.ImpenRegistry.MachineDefinition;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MachineBlockEntity extends AENetworkInvBlockEntity implements IGridTickable {

    protected int runningTicks = -1;
    protected Optional<MachineOperation> activeOperation = Optional.empty();

    public MachineBlockEntity(final MachineDefinition<? extends Block, ? extends BlockEntity> machineDefinition,
            final BlockPos blockPos, final BlockState blockState) {
        super(machineDefinition.blockEntity(), blockPos, blockState);

        this.getMainNode()
                .addService(IGridTickable.class, this)
                .setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public void onMainNodeStateChanged(final IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.getMainNode().ifPresent((grid, node) -> {
                grid.getTickManager().alertDevice(node);
            });
        }
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot) {
        if (this.isClientSide()) {
            return;
        }

        this.getMainNode().ifPresent((grid, node) -> {
            grid.getTickManager().alertDevice(this.getGridNode());
        });
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        // TODO Consider changing this to sleep instead
        return new TickingRequest(1, 20, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (this.isRemoved()) {
            return TickRateModulation.SAME;
        }

        if (this.activeOperation.isEmpty()) {
            this.activeOperation = this.getOperation();
        }

        if (this.activeOperation.isPresent()) {
            final var op = this.activeOperation.get();

            if (!op.shouldRunFunc.getAsBoolean()) {
                this.runningTicks = -1;
                this.activeOperation = Optional.empty();
                return TickRateModulation.SAME;
            }

            if (this.runningTicks == -1) {
                this.startOperation();
                this.runningTicks = 0;
            }
            this.runningTicks += this.progressOperation();

            if (this.runningTicks >= op.ticksRequired) {
                if (op.executeOperationFunc.getAsBoolean()) {
                    this.runningTicks = -1;
                    this.activeOperation = Optional.empty();
                    return TickRateModulation.SAME;
                }
                else {
                    return TickRateModulation.SAME;
                }
            }
            else {
                return TickRateModulation.URGENT;
            }
        }
        else {
            return TickRateModulation.SLOWER;
        }
    }

    @Override
    public AECableType getCableConnectionType(final Direction dir) {
        return AECableType.SMART;
    }

    public int getProgress() {
        return this.runningTicks;
    }

    public static record MachineOperation(
            int ticksRequired,
            BooleanSupplier shouldRunFunc,
            BooleanSupplier executeOperationFunc) {
    }
    
    protected void startOperation() {
        // To be extended by implementing classes
    }

    protected abstract Optional<MachineOperation> getOperation();

    /** @return The number of ticks that the operation should advance; 0 if no progress */
    protected abstract int progressOperation();

    public abstract IAEItemFilter getInventoryFilter();
}
