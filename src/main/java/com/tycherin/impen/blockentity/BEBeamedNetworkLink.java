package com.tycherin.impen.blockentity;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpracticalEnergisticsMod;

import appeng.api.exceptions.ExistingConnectionException;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.exceptions.SecurityConnectionException;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AEColor;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.me.helpers.BlockEntityNodeListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class BEBeamedNetworkLink extends AENetworkBlockEntity implements IPowerChannelState, ServerTickingBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int MAX_DISTANCE = 16;
    private static final int CHECK_INTERVAL = 20;

    private boolean dirtyBit = false;
    private int ticksUntilNextCheck = 0;
    private boolean hasPower = false;
    private int lengthOfBeam = -1;

    private int beamColor = AEColor.WHITE.mediumVariant;
    private AABB renderBoundingBox;

    /** Note that this will always be empty on the client side. */
    private Optional<BnlConnection> bnlConnection = Optional.empty();

    record BnlPair(/** BNL on the other side of the pair */
    BEBeamedNetworkLink otherBnl,
            /** Distance between the two BNLs, including the other end */
            int distanceBetween) {
    }

    record BnlConnection(BnlPair pair,
            /**
             * If the two BNLs are connected, this is the connection; if the two BNLs failed to connect for whatever
             * reason, then this is empty <br>
             * Empty signals that there should be a connection here but it isn't possible, so don't try again unless
             * something changes
             */
            Optional<IGridConnection> gridConnection,
            /**
             * true if this is the BNL that initiated the connection; false otherwise
             * <br>
             * This value is kinda meaningless if there's no gridConnection present
             */
            boolean isPrimarySide) {
    }

    // TODO Figure out how chunk loading/unloading works

    public BEBeamedNetworkLink(final BlockPos pos, final BlockState blockState) {
        super(ImpracticalEnergisticsMod.BEAMED_NETWORK_LINK_BE.get(), pos, blockState);

        this.getMainNode()
                .setExposedOnSides(EnumSet.noneOf(Direction.class))
                .setIdlePowerUsage(10) // TODO This should only be drained if the BNL is active
                .setFlags(); // force to not require a channel
    }

    // ***
    // Getters
    // ***

    @Override
    public boolean isPowered() {
        if (isClientSide()) {
            return this.hasPower;
        }
        else {
            return this.getMainNode().isPowered();
        }
    }

    @Override
    public boolean isActive() {
        return this.isPowered();
    }

    // Primarily used on the client side

    public int getLengthOfBeam() {
        return this.lengthOfBeam;
    }

    public int getBeamColor() {
        return this.beamColor;
    }

    public void setDirtyBit() {
        this.dirtyBit = true;
    }
    
    public void forceUpdate() {
        this.setDirtyBit();
        this.ticksUntilNextCheck = 0;
    }

    // ***
    // Methods related to Forge & AE2
    // ***

    @Override
    public void onMainNodeStateChanged(final IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            if (this.hasPower != this.getMainNode().isPowered()) {
                this.hasPower = this.getMainNode().isPowered();
            }
            this.forceUpdate();
        }
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, new BlockEntityNodeListener<BEBeamedNetworkLink>() {
            @Override
            public void onInWorldConnectionChanged(final BEBeamedNetworkLink nodeOwner, final IGridNode node) {
                super.onInWorldConnectionChanged(nodeOwner, node);
                nodeOwner.setDirtyBit();
            }
        });
    }

    @Override
    public boolean readFromStream(final FriendlyByteBuf data) {
        final boolean superFlag = super.readFromStream(data);

        final boolean isPoweredOld = this.isPowered();
        this.hasPower = data.readBoolean();

        final int lengthOfBeamOld = this.getLengthOfBeam();
        this.lengthOfBeam = data.readInt();

        final int beamColorOld = this.getBeamColor();
        this.beamColor = data.readInt();

        this.updateVisualState();

        return (this.hasPower != isPoweredOld)
                || (this.lengthOfBeam != lengthOfBeamOld)
                || (this.beamColor != beamColorOld)
                || superFlag;
    }

    @Override
    public void writeToStream(final FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.isPowered());
        data.writeInt(this.lengthOfBeam);
        data.writeInt(this.beamColor);
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        this.getMainNode().setExposedOnSides(EnumSet.complementOf(EnumSet.of(inForward)));
        this.forceUpdate();
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getMainNode().setExposedOnSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
        this.forceUpdate();
    }

    @Override
    public void onChunkUnloaded() {
        this.removeConnectionIfPresent();
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        this.removeConnectionIfPresent();
        super.setRemoved();
    }

    // TODO It would be nice to get updates instantly, but without running the full updateTarget() search every time.
    // Caching opportunities?
    // TODO Check power status here
    @Override
    public void serverTick() {
        if (this.isRemoved()) {
            return;
        }

        this.ticksUntilNextCheck--;
        if (ticksUntilNextCheck <= 0) {
            ticksUntilNextCheck = CHECK_INTERVAL;
            this.updateTarget();
        }

        if (this.dirtyBit) {
            this.updateVisualState();
            this.markForUpdate();
            this.dirtyBit = false;
        }
    }

    // ***
    // Methods related to BNL connections
    // ***

    /** @return true if this BNL is actively linked to another one; false otherwise */
    public boolean hasActiveConnection() {
        if (this.level.isClientSide) {
            return this.lengthOfBeam > 0;
        }
        else {
            return this.bnlConnection.isPresent() && this.bnlConnection.get().gridConnection.isPresent();
        }
    }

    /** @return true if the target has changed; false otherwise */
    public void updateTarget() {
        if (this.level.isClientSide) {
            return;
        }

        if (this.bnlConnection.isPresent() && !this.bnlConnection.get().isPrimarySide
                && !this.bnlConnection.get().pair.otherBnl.isRemoved()) {
            // This isn't the primary side, so skip update & let the primary side to handle it
            // TODO How does chunkloading interact with this?
            return;
        }

        if (this.getGridNode() == null || !this.getGridNode().hasGridBooted()) {
            // Grid is in a weird state, don't bother checking things
            // TODO The second clause here might mess things up on loading?
            return;
        }

        final Optional<BnlPair> latestBnlPair = this.findMatchingBnl();
        if ((latestBnlPair.isEmpty() && this.bnlConnection.isEmpty())
                || (latestBnlPair.isPresent() && this.bnlConnection.isPresent()
                        && latestBnlPair.get().equals(this.bnlConnection.get().pair))) {
            // New target is the same as the old one, so don't change anything
            return;
        }
        else {
            if (this.bnlConnection.isPresent()) {
                this.removeConnectionIfPresent();
            }

            if (latestBnlPair.isPresent() && latestBnlPair.get().otherBnl.getGridNode() != null) {
                // We need to determine which side of the connection is primary and which is secondary. The BNL calling
                // createConnection isn't necessarily the one on the active side.
                final boolean canThisBePrimary = this.getGridNode().isActive();
                final boolean canOtherBePrimary = latestBnlPair.get().otherBnl.getGridNode().isActive();
                boolean thisPrimary;
                if (canThisBePrimary && !canOtherBePrimary) {
                    thisPrimary = true;
                }
                else if (!canThisBePrimary && canOtherBePrimary) {
                    thisPrimary = false;
                }
                else if (canThisBePrimary && canOtherBePrimary) {
                    // This is weird - either side could be primary. In order to be deterministic, I'm following a
                    // spatial heuristic because I'm not sure how to do "First one placed wins" without storing extra
                    // state somewhere.
                    thisPrimary = this.getBlockPos().compareTo(latestBnlPair.get().otherBnl.getBlockPos()) > -1;
                }
                else {
                    // Neither one can be primary, and I'm very confused right now
                    thisPrimary = false;
                }

                // Only create a connection if this is primary. This is to avoid weird situations where both BNLs
                // attempt to create a connection at the same time and we wind up with duplicate connections.
                if (thisPrimary) {
                    this.createConnection(latestBnlPair.get());
                    this.setDirtyBit();
                    latestBnlPair.get().otherBnl.forceUpdate();
                }
                else {
                    // Kinda hacky, but we want to force the update ASAP to avoid an awkward pause while we wait for the
                    // other BNL to tick
                    latestBnlPair.get().otherBnl.forceUpdate();
                }
            }
        }
    }
    
    /** @return A BnlPair for the closest BNL facing this one within range; empty if there isn't a suitable one */
    private Optional<BnlPair> findMatchingBnl() {

        BlockPos checkPos = this.getBlockPos();
        int checkDistance = 0;
        BEBeamedNetworkLink targetBnl = null;

        for (; checkDistance < MAX_DISTANCE; checkDistance++) {
            checkPos = switch (this.getForward()) {
            case UP -> checkPos.above();
            case DOWN -> checkPos.below();
            case NORTH -> checkPos.north();
            case SOUTH -> checkPos.south();
            case WEST -> checkPos.west();
            case EAST -> checkPos.east();
            };

            final BlockState bs = this.level.getBlockState(checkPos);
            if (bs.getBlock().equals(ImpracticalEnergisticsMod.BEAMED_NETWORK_LINK_BLOCK.get())) {
                final BlockEntity be = this.level.getBlockEntity(checkPos);
                if (be != null && be instanceof BEBeamedNetworkLink) {
                    targetBnl = (BEBeamedNetworkLink) be;
                    boolean facingMatches = switch (this.getForward()) {
                    case UP -> targetBnl.getForward().equals(Direction.DOWN);
                    case DOWN -> targetBnl.getForward().equals(Direction.UP);
                    case NORTH -> targetBnl.getForward().equals(Direction.SOUTH);
                    case SOUTH -> targetBnl.getForward().equals(Direction.NORTH);
                    case EAST -> targetBnl.getForward().equals(Direction.WEST);
                    case WEST -> targetBnl.getForward().equals(Direction.EAST);
                    };

                    if (facingMatches) {
                        // Found a match, so we can stop searching
                        break;
                    }
                    else {
                        // We could keep searching at this point in case there's a correctly oriented BNL further down
                        // the line, but that sounds like potentially very confusing and unintuitive behavior
                        checkDistance = -1;
                        break;
                    }
                }
                else {
                    LOGGER.info("Encountered BNL block without associated BlockEntity at {}", checkPos.toShortString());
                    checkDistance = -1;
                    break;
                }
            }
            else {
                if (bs.canOcclude()) {
                    // Solid block - this blocks the beam
                    checkDistance = -1;
                    break;
                }
                else {
                    // Non-occluding block - keep going
                    continue;
                }
            }
        }

        if (checkDistance == MAX_DISTANCE) {
            checkDistance = -1;
        }

        if (checkDistance == -1) {
            return Optional.empty();
        }
        else {
            return Optional.of(new BnlPair(targetBnl, checkDistance));
        }
    }

    private void removeConnectionIfPresent() {
        if (this.bnlConnection.isPresent()) {
            this.bnlConnection.get().gridConnection.ifPresent(IGridConnection::destroy);

            this.bnlConnection.get().pair.otherBnl.setConnection(Optional.empty());
            this.bnlConnection.get().pair.otherBnl.forceUpdate();

            this.setConnection(Optional.empty());
            this.setDirtyBit();
        }
    }

    /**
     * Creates a connection between two BNLs
     * <br>
     * Note that this method assumes that the invoking BNL is the primary one, while the parameter is the secondary
     * 
     * @param newPair BNL to connect with this one
     * @return true if the connection was created successfully
     */
    private boolean createConnection(final BnlPair newPair) {
        if (this.bnlConnection.isPresent()) {
            throw new RuntimeException(String.format(
                    "Cannot create BNL connection because one already exists; call removeLink() first (current connection: {}; requested connection: {})",
                    this.bnlConnection.get(), newPair));
        }

        if (this.getGridNode() == null || newPair.otherBnl.getGridNode() == null) {
            // This is something that can happen when one side or the other is still booting
            return false;
        }

        try {
            final var gridConnection = GridHelper.createGridConnection(
                    this.getGridNode(), newPair.otherBnl.getGridNode());
            this.setConnection(Optional.of(new BnlConnection(newPair, Optional.of(gridConnection), true)));
            newPair.otherBnl.setConnection(Optional.of(new BnlConnection(
                    new BnlPair(this, newPair.distanceBetween), Optional.of(gridConnection), false)));

            return true;
        }
        catch (final ExistingConnectionException | SecurityConnectionException e) {
            // Either:
            // 1) The two BNLs are already connected, e.g. by being adjacent to each other
            // 2) The two BNLs are both part of a network already, and those networks have different security settings
            // TODO What happens here if one side is primary and then gets removed for whatever reason?
            this.bnlConnection = Optional.of(new BnlConnection(newPair, Optional.empty(), true));
            newPair.otherBnl.setConnection(Optional.of(new BnlConnection(newPair, Optional.empty(), false)));

            return false;
        }
        catch (final FailedConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    private void setConnection(final Optional<BnlConnection> connection) {
        this.bnlConnection = connection;
        if (connection.isEmpty() || !connection.get().isPrimarySide) {
            this.lengthOfBeam = -1;
        }
        else {
            this.lengthOfBeam = connection.get().pair.distanceBetween;
        }
        this.setDirtyBit();
    }

    // ***
    // Methods related to rendering
    // ***

    /*
     * This is kind of stupid - this method needs to be overridden in order to make the BER work when the BE is
     * off-screen, despite the fact that BER#shouldRenderOffscreen exists. This is apparently Forge behavior that may be
     * changed by 1.20.
     */
    @Override
    public AABB getRenderBoundingBox() {
        if (this.renderBoundingBox == null) {
            this.updateVisualState();
        }
        return this.renderBoundingBox;
    }

    public void updateVisualState() {
        this.updateBeamColor();
        this.updateRenderBoundingBox();
    }

    /** Updates the render bounding box to match the current length & direction of the connection (if any) */
    protected void updateRenderBoundingBox() {
        int xStart = this.getBlockPos().getX();
        int yStart = this.getBlockPos().getY();
        int zStart = this.getBlockPos().getZ();
        int xEnd = xStart + 1;
        int yEnd = yStart + 1;
        int zEnd = zStart + 1;

        if (this.hasActiveConnection()) {
            switch (this.getForward()) {
            case UP -> yEnd += this.getLengthOfBeam();
            case DOWN -> yStart -= this.getLengthOfBeam();
            case NORTH -> zStart -= this.getLengthOfBeam();
            case SOUTH -> zEnd += this.getLengthOfBeam();
            case WEST -> xStart -= this.getLengthOfBeam();
            case EAST -> xEnd += this.getLengthOfBeam();
            }
        }

        this.renderBoundingBox = new AABB(xStart, yStart, zStart, xEnd, yEnd, zEnd);
    }

    /**
     * The color of the beam is based on the color of AE2 blocks that are connected directly to this one.
     * This method handles that logic and updates the beam color accordingly.
     * 
     * @return true if the color changed; false otherwise
     */
    protected boolean updateBeamColor() {
        if (this.isClientSide()) {
            return false;
        }

        int newBeamColor = AEColor.WHITE.mediumVariant;
        if (this.getMainNode() != null && this.getMainNode().getNode() != null) {
            // Find the colors of all AE network entities linked to this one
            final List<AEColor> linkedColors = this.getMainNode().getNode().getConnections().stream()
                    .filter(connection -> {
                        // Special case: ignore the BNL connected to this one, if any
                        return !(this.bnlConnection.isPresent()
                                && this.bnlConnection.get().gridConnection.isPresent()
                                && this.bnlConnection.get().gridConnection.get().equals(connection));
                    })
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

        if (newBeamColor != this.beamColor) {
            this.beamColor = newBeamColor;
            return true;
        }
        else {
            return false;
        }
    }
}
