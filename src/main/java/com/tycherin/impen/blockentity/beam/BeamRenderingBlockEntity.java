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

import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.grid.AENetworkBlockEntity;
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

    private static class BeamRenderingWrapper {
        private final BlockPos pos;

        private BeamRenderingWrapper(final BlockPos pos) {
            // Copy the input parameter to make sure it doesn't get mutated out from under us
            this.pos = new BlockPos(pos);
        }

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

            // Note that this doesn't work if you have multiple beams pointing the same direction, which we don't 
            for (final BeamRenderData data : renderDataSet) {
                switch (data.getDirection()) {
                case UP -> yEnd += data.getBeamLength();
                case DOWN -> yStart -= data.getBeamLength();
                case NORTH -> zStart -= data.getBeamLength();
                case SOUTH -> zEnd += data.getBeamLength();
                case WEST -> xStart -= data.getBeamLength();
                case EAST -> xEnd += data.getBeamLength();
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
            final Set<BeamRenderData> oldSet = this.renderDataSet;
            final Set<BeamRenderData> newSet = new HashSet<>();
            final byte dataCount = buf.readByte();
            for (byte i = 0; i < dataCount; i++) {
                // Note that we don't update the map here because this is only ever called client-side, and the map
                // doesn't matter there anyway
                newSet.add(BeamRenderData.readFromStream(buf));
            }
            
            if (newSet.equals(oldSet)) {
                return false;
            }
            else {
                this.renderDataSet.clear();
                this.renderDataSet.addAll(newSet);
                this.updateRenderBoundingBox();
                return true;
            }
        }
        
        public void setColor(final int color) {
            this.renderDataSet.forEach(data -> data.setBeamColor(color));
        }

        public void clear() {
            this.renderDataSet.clear();
            this.map.clear();
            this.renderBoundingBox = null;
        }
    }

    public static abstract class BeamRenderingNetworkBlockEntity extends AENetworkBlockEntity
            implements BeamNetworkPropagator {

        private final BeamRenderingWrapper renderWrapper;
        protected Optional<BeamNetwork> network = Optional.empty();
        protected int beamColor = 0;

        public BeamRenderingNetworkBlockEntity(final BlockEntityType<?> blockEntityType, final BlockPos pos,
                final BlockState blockState) {
            super(blockEntityType, pos, blockState);
            this.renderWrapper = new BeamRenderingWrapper(pos);
        }

        @Override
        public void setNetwork(final Optional<BeamNetwork> networkOpt) {
            this.network = networkOpt;
            this.renderWrapper.clear();
            if (this.network.isPresent()) {
                this.setColor(this.network.get().getColor());
            }
            this.markForUpdate();
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
            this.renderWrapper.addConnection(conn, this.beamColor);
            this.markForUpdate();
        }

        @Override
        public void stopRenderConnection(final BeamNetworkInWorldConnection conn) {
            this.renderWrapper.removeConnection(conn);
            this.markForUpdate();
        }

        @Override
        public void onChunkUnloaded() {
            super.onChunkUnloaded();
            network.ifPresent(BeamNetwork::forceUpdate);
        }

        @Override
        public void setRemoved() {
            super.setRemoved();
            network.ifPresent(BeamNetwork::forceUpdate);
        }

        @Override
        public void setColor(final int color) {
            this.beamColor = color;
            this.renderWrapper.setColor(color);
            this.markForUpdate();
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
                return be.renderWrapper.renderDataSet;
            }

            @Override
            protected boolean hasRenderData(final BeamRenderingNetworkBlockEntity be) {
                return !be.renderWrapper.renderDataSet.isEmpty();
            }
        }
    }

    public static abstract class BeamRenderingBaseBlockEntity extends AEBaseBlockEntity
            implements BeamNetworkPropagator {

        protected final BeamRenderingWrapper renderWrapper;
        private Optional<BeamNetwork> network = Optional.empty();
        protected int beamColor = 0;

        public BeamRenderingBaseBlockEntity(final BlockEntityType<?> blockEntityType, final BlockPos pos,
                final BlockState blockState) {
            super(blockEntityType, pos, blockState);
            this.renderWrapper = new BeamRenderingWrapper(pos);
        }

        @Override
        public void setNetwork(final Optional<BeamNetwork> networkOpt) {
            this.network = networkOpt;
            this.renderWrapper.clear();
            if (this.network.isPresent()) {
                this.setColor(this.network.get().getColor());
            }
            this.markForUpdate();
        }

        // See comment above
        @Override
        public AABB getRenderBoundingBox() {
            return renderWrapper.getRenderBoundingBox();
        }

        @Override
        public void renderConnection(final BeamNetworkInWorldConnection conn) {
            this.renderWrapper.addConnection(conn, this.beamColor);
            this.markForUpdate();
        }

        @Override
        public void stopRenderConnection(final BeamNetworkInWorldConnection conn) {
            this.renderWrapper.removeConnection(conn);
            this.markForUpdate();
        }

        @Override
        public void onChunkUnloaded() {
            super.onChunkUnloaded();
            network.ifPresent(BeamNetwork::forceUpdate);
        }

        @Override
        public void setRemoved() {
            super.setRemoved();
            network.ifPresent(BeamNetwork::forceUpdate);
        }
        
        @Override
        public void setColor(final int color) {
            this.beamColor = color;
            this.renderWrapper.setColor(color);
            this.markForUpdate();
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
                return be.renderWrapper.renderDataSet;
            }

            @Override
            protected boolean hasRenderData(final BeamRenderingBaseBlockEntity be) {
                return !be.renderWrapper.renderDataSet.isEmpty();
            }
        }
    }

}
