package com.tycherin.impen.blockentity.beam;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.beam.BeamRenderingBlockEntity.BeamRenderingNetworkBlockEntity;
import com.tycherin.impen.config.ImpenConfig;
import com.tycherin.impen.logic.beam.BeamNetwork;
import com.tycherin.impen.logic.beam.BeamNetworkConnectionHelper;
import com.tycherin.impen.logic.beam.BeamNetworkGridPropagator;
import com.tycherin.impen.logic.beam.BeamNetworkPhysicalConnection;
import com.tycherin.impen.util.AEPowerUtil;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AEColor;
import appeng.me.helpers.BlockEntityNodeListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

public class BeamNetworkEmitterBlockEntity extends BeamRenderingNetworkBlockEntity
        implements BeamNetworkGridPropagator, IPowerChannelState, IGridTickable {

    private static final int MAX_DISTANCE = 16;

    private final double basePower;

    private boolean hasPower = false;
    private boolean receivedPowerLastCycle = false;

    public BeamNetworkEmitterBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.BEAM_NETWORK_EMITTER.blockEntity(), pos, blockState);

        this.getMainNode()
                .addService(IGridTickable.class, this)
                .setExposedOnSides(EnumSet.noneOf(Direction.class))
                .setFlags(); // force to not require a channel

        this.basePower = ImpenConfig.POWER.beamedNetworkLinkCost();
    }

    @Override
    public boolean isPowered() {
        if (isClientSide()) {
            return this.hasPower;
        }
        else {
            return this.getMainNode().isPowered() && this.receivedPowerLastCycle;
        }
    }

    @Override
    public boolean isActive() {
        return this.isPowered();
    }

    @Override
    public void onMainNodeStateChanged(final IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.updateBeamColor();
        }
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, new BlockEntityNodeListener<BeamNetworkEmitterBlockEntity>() {
            @Override
            public void onInWorldConnectionChanged(final BeamNetworkEmitterBlockEntity nodeOwner,
                    final IGridNode node) {
                super.onInWorldConnectionChanged(nodeOwner, node);
                nodeOwner.updateBeamColor();
            }
        });
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        if (!this.getForward().equals(inForward)) {
            this.deactivate();
        }
        super.setOrientation(inForward, inUp);
        this.getMainNode().setExposedOnSides(EnumSet.of(inForward.getOpposite()));
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getMainNode().setExposedOnSides(EnumSet.of(this.getForward().getOpposite()));
        this.network = Optional.of(new BeamNetwork(this));
        this.updateBeamColor();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.deactivate();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.deactivate();
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        // TODO If this is going to consume power every tick, it needs a buffer, or else it's going to get really ornery
        // when starving for power
        return new TickingRequest(1, 1, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (!this.getMainNode().isPowered()) {
            this.deactivate();
            return TickRateModulation.IDLE;
        }

        this.receivedPowerLastCycle = AEPowerUtil.drawPower(this, this.getPowerDraw());
        if (!this.receivedPowerLastCycle) {
            this.deactivate();
            return TickRateModulation.IDLE;
        }
        else {
            // The network will decide whether it needs to run logic or not
            this.network.ifPresent(BeamNetwork::update);
            return TickRateModulation.URGENT;
        }
    }

    private void deactivate() {
        this.network.ifPresent(BeamNetwork::reset);
    }

    public double getPowerDraw() {
        return network.map(BeamNetwork::getPowerCost).orElse(0) * this.basePower;
    }

    @Override
    public List<BeamNetworkPhysicalConnection> propagate() {
        return BeamNetworkConnectionHelper.findVisualConnection(this, getBlockPos(), getForward(), MAX_DISTANCE, level)
                .map(List::of)
                .orElseGet(Collections::emptyList);
    }

    @Override
    public void setNetwork(final Optional<BeamNetwork> networkOpt) {
        // Network is already populated because this class created it, so this is a no-op
    }

    @Override
    public void setColor(final int color) {
        // We know what the beam color is because it originates here, so this is a no-op
    }

    @Override
    public IGridNode getSourceGridNode() {
        return this.getGridNode();
    }

    @Override
    public boolean readFromStream(final FriendlyByteBuf data) {
        final boolean superFlag = super.readFromStream(data);

        final boolean isPoweredOld = this.isPowered();
        this.hasPower = data.readBoolean();

        return (this.hasPower != isPoweredOld)
                || superFlag;
    }

    @Override
    public void writeToStream(final FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.isPowered());
    }

    @Override
    public int getBeamColor() {
        return this.beamColor;
    }

    /**
     * The color of the beam is based on the color of AE2 blocks that are connected directly to this one.
     * This method handles that logic and updates the beam color accordingly.
     */
    protected void updateBeamColor() {
        int newBeamColor = AEColor.WHITE.mediumVariant;
        if (this.getMainNode() != null && this.getMainNode().getNode() != null) {
            // Find the colors of all AE network entities linked to this one
            final List<AEColor> linkedColors = this.getMainNode().getNode().getConnections().stream()
                    // Special case: ignore non-physical grid connections, which could include beam link connections
                    .filter(IGridConnection::isInWorld)
                    .map(connection -> connection.getOtherSide(getGridNode()).getGridColor())
                    .filter(color -> !color.equals(AEColor.TRANSPARENT))
                    .distinct()
                    .collect(Collectors.toList());

            if (linkedColors.size() == 1) {
                final AEColor color = linkedColors.get(0);

                // Transparent is the default, but its associated color is actually Fluix-y, so default to white there
                if (color != AEColor.TRANSPARENT) {
                    newBeamColor = color.mediumVariant;
                }
            }
        }

        if (this.beamColor != newBeamColor) {
            this.beamColor = newBeamColor;
            this.markForUpdate();
            this.network.ifPresent(BeamNetwork::forceUpdate);
        }
    }
}
