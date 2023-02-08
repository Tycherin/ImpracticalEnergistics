package com.tycherin.impen.blockentity.beam;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.tycherin.impen.client.render.BeamNetworkRenderer;
import com.tycherin.impen.client.render.BeamNetworkRenderer.BeamRenderData;
import com.tycherin.impen.logic.beam.BeamNetwork;
import com.tycherin.impen.logic.beam.BeamNetworkConnection;
import com.tycherin.impen.logic.beam.BeamNetworkPhysicalConnection.BeamNetworkInWorldConnection;
import com.tycherin.impen.logic.beam.BeamNetworkPropagator;

import appeng.api.util.AEColor;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.grid.AENetworkBlockEntity;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class exists for very silly reasons, mostly because I don't feel like messing with mixins despite the fact that
 * this is a pretty good use case for it
 */
public class BeamRenderingBlockEntity {

    @RequiredArgsConstructor
    private static class BeamRenderingWrapper {
        private final BlockPos pos;

        private final Map<BeamNetworkConnection, BeamRenderData> map = new HashMap<>();
        private final Set<BeamRenderData> renderDataSet = new HashSet<>();
        private AABB renderBoundingBox = null;

        AABB getRenderBoundingBox() {
            if (this.renderBoundingBox == null) {
                this.updateRenderBoundingBox();
            }
            return this.renderBoundingBox;
        }

        void updateRenderBoundingBox() {
            int xStart = this.pos.getX();
            int yStart = this.pos.getY();
            int zStart = this.pos.getZ();
            int xEnd = xStart + 1;
            int yEnd = yStart + 1;
            int zEnd = zStart + 1;

            for (final BeamRenderData data : map.values()) {
                switch (data.direction()) {
                case UP -> yEnd += data.beamLength();
                case DOWN -> yStart -= data.beamLength();
                case NORTH -> zStart -= data.beamLength();
                case SOUTH -> zEnd += data.beamLength();
                case WEST -> xStart -= data.beamLength();
                case EAST -> xEnd += data.beamLength();
                }
            }

            this.renderBoundingBox = new AABB(xStart, yStart, zStart, xEnd, yEnd, zEnd);
        }

        void addConnection(final BeamNetworkInWorldConnection conn, final Integer color) {
            if (!map.containsKey(conn)) {
                final var data = BeamRenderData.from(conn, color);
                map.put(conn, data);
                renderDataSet.add(data);
                updateRenderBoundingBox();
            }
        }

        void removeConnection(final BeamNetworkConnection conn) {
            final var oldData = map.remove(conn);
            if (oldData != null) {
                renderDataSet.remove(oldData);
                updateRenderBoundingBox();
            }
        }

        public void writeToStream(final FriendlyByteBuf buf) {
            buf.writeByte(renderDataSet.size());
            for (final BeamRenderData data : renderDataSet) {
                data.writeToStream(buf);
            }
        }

        public boolean readFromStream(final FriendlyByteBuf buf) {
            final byte dataCount = buf.readByte();
            for (byte i = 0; i < dataCount; i++) {
                // Note that we don't update the map here because this is only ever called client-side, and the map
                // doesn't matter there anyway
                renderDataSet.add(BeamRenderData.readFromStream(buf));
            }
            // I'm just going to assume this always changes
            return true;
        }
    }

    public static abstract class BeamRenderingNetworkBlockEntity extends AENetworkBlockEntity
            implements BeamNetworkPropagator {

        private final BeamRenderingWrapper renderWrapper;
        protected Optional<BeamNetwork> network = Optional.empty();

        public BeamRenderingNetworkBlockEntity(final BlockEntityType<?> blockEntityType, final BlockPos pos,
                final BlockState blockState) {
            super(blockEntityType, pos, blockState);
            this.renderWrapper = new BeamRenderingWrapper(pos);
        }

        @Override
        public void setNetwork(final Optional<BeamNetwork> networkOpt) {
            this.network = networkOpt;
        }

        /*
         * This is kind of stupid - this method needs to be overridden in order to make the BER work when the BE is
         * off-screen, despite the fact that BER#shouldRenderOffscreen exists. This is apparently Forge behavior that may be
         * changed by 1.20.
         */
        @Override
        public AABB getRenderBoundingBox() {
            return renderWrapper.getRenderBoundingBox();
        }

        @Override
        public void renderConnection(final BeamNetworkInWorldConnection conn) {
            this.renderWrapper.addConnection(conn, this.getColor());
        }

        @Override
        public void stopRenderConnection(final BeamNetworkInWorldConnection conn) {
            this.renderWrapper.removeConnection(conn);
        }

        @Override
        public void onChunkUnloaded() {
            network.ifPresent(BeamNetwork::update);
            super.onChunkUnloaded();
        }

        @Override
        public void setRemoved() {
            network.ifPresent(BeamNetwork::update);
            super.setRemoved();
        }

        protected int getColor() {
            return network.map(BeamNetwork::getColor).orElse(AEColor.WHITE.mediumVariant);
        }

        @Override
        public void writeToStream(final FriendlyByteBuf data) {
            super.writeToStream(data);
            this.renderWrapper.writeToStream(data);
        }

        @Override
        public boolean readFromStream(final FriendlyByteBuf data) {
            final boolean superFlag = super.readFromStream(data);

            final boolean renderWrapperResult = this.renderWrapper.readFromStream(data);

            return renderWrapperResult
                    || superFlag;
        }

        @OnlyIn(Dist.CLIENT)
        public static class Renderer extends BeamNetworkRenderer<BeamRenderingNetworkBlockEntity> {

            public Renderer(final Context ignored) {
                super(ignored);
            }

            @Override
            protected Collection<BeamRenderData> getRenderData(final BeamRenderingNetworkBlockEntity be) {
                return be.renderWrapper.map.values();
            }

            @Override
            protected boolean hasRenderData(final BeamRenderingNetworkBlockEntity be) {
                return !be.renderWrapper.map.isEmpty();
            }
        }
    }

    public static abstract class BeamRenderingBaseBlockEntity extends AEBaseBlockEntity
            implements BeamNetworkPropagator {

        protected final BeamRenderingWrapper renderWrapper;
        private Optional<BeamNetwork> network = Optional.empty();

        public BeamRenderingBaseBlockEntity(final BlockEntityType<?> blockEntityType, final BlockPos pos,
                final BlockState blockState) {
            super(blockEntityType, pos, blockState);
            this.renderWrapper = new BeamRenderingWrapper(pos);
        }

        @Override
        public void setNetwork(final Optional<BeamNetwork> networkOpt) {
            this.network = networkOpt;
        }

        // See comment above
        @Override
        public AABB getRenderBoundingBox() {
            return renderWrapper.getRenderBoundingBox();
        }

        @Override
        public void renderConnection(final BeamNetworkInWorldConnection conn) {
            this.renderWrapper.addConnection(conn, this.getColor());
        }

        @Override
        public void stopRenderConnection(final BeamNetworkInWorldConnection conn) {
            this.renderWrapper.removeConnection(conn);
        }

        @Override
        public void onChunkUnloaded() {
            network.ifPresent(BeamNetwork::update);
            super.onChunkUnloaded();
        }

        @Override
        public void setRemoved() {
            network.ifPresent(BeamNetwork::update);
            super.setRemoved();
        }

        protected int getColor() {
            return network.map(BeamNetwork::getColor).orElse(AEColor.WHITE.mediumVariant);
        }

        @Override
        public void writeToStream(final FriendlyByteBuf data) {
            super.writeToStream(data);
            this.renderWrapper.writeToStream(data);
        }

        @Override
        public boolean readFromStream(final FriendlyByteBuf data) {
            final boolean superFlag = super.readFromStream(data);

            final boolean renderWrapperResult = this.renderWrapper.readFromStream(data);

            return renderWrapperResult
                    || superFlag;
        }

        @OnlyIn(Dist.CLIENT)
        public static class Renderer extends BeamNetworkRenderer<BeamRenderingBaseBlockEntity> {

            public Renderer(final Context ignored) {
                super(ignored);
            }

            @Override
            protected Collection<BeamRenderData> getRenderData(final BeamRenderingBaseBlockEntity be) {
                return be.renderWrapper.map.values();
            }

            @Override
            protected boolean hasRenderData(final BeamRenderingBaseBlockEntity be) {
                return !be.renderWrapper.map.isEmpty();
            }
        }
    }

}
