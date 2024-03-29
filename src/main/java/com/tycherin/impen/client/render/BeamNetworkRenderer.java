package com.tycherin.impen.client.render;

import java.awt.Color;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.tycherin.impen.logic.beam.BeamNetworkPhysicalConnection.BeamNetworkInWorldConnection;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class BeamNetworkRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    public static final ResourceLocation BEAM_TEXTURE_LOCATION = new ResourceLocation(
            "textures/entity/beacon_beam.png");

    /** Width of the core beam */
    private static final float BEAM_WIDTH = 0.16f;
    /** Width of the beam outline */
    private static final float OUTLINE_WIDTH = 0.21f;
    /** How deep the beam will penetrate into the source & destination blocks */
    private static final float BEAM_PENETRATION_DEPTH = 0.3f;

    private static final Map<Direction, Quaternion> ROTATION_MAP = ImmutableMap.of(
            Direction.UP, Quaternion.ONE, // Up is the default rendering direction
            Direction.DOWN, new Quaternion(new Vector3f(1f, 0f, 0f), 180, true),
            Direction.NORTH, new Quaternion(new Vector3f(1f, 0f, 0f), 270, true),
            Direction.EAST, new Quaternion(new Vector3f(0f, 0f, 1f), 270, true),
            Direction.SOUTH, new Quaternion(new Vector3f(1f, 0f, 0f), 90, true),
            Direction.WEST, new Quaternion(new Vector3f(0f, 0f, 1f), 90, true));

    public BeamNetworkRenderer(final BlockEntityRendererProvider.Context ignored) {
    }

    protected abstract Collection<BeamRenderData> getRenderData(T be);

    protected abstract boolean hasRenderData(T be);

    @EqualsAndHashCode
    @Getter
    @AllArgsConstructor
    public static class BeamRenderData {
        private final Direction direction;
        private final byte beamLength;
        @Setter
        private int beamColor;

        public static BeamRenderData from(final BeamNetworkInWorldConnection conn, final Integer beamColor) {
            // The byte cast isn't technically safe, but in practice, the distance will never be more than 127
            // The -1 offset is to prevent the beam from going through the target block
            final byte beamLength = (byte)(conn.getDistance() - 1);
            return new BeamRenderData(conn.getDirection(), beamLength, beamColor);
        }

        public void writeToStream(final FriendlyByteBuf buf) {
            buf.writeByte(this.direction.ordinal());
            buf.writeByte(this.beamLength);
            buf.writeInt(this.beamColor);
        }

        public static BeamRenderData readFromStream(final FriendlyByteBuf buf) {
            final Direction direction = Direction.values()[buf.readByte()];
            final byte beamLength = buf.readByte();
            final int beamColor = buf.readInt();
            return new BeamRenderData(direction, beamLength, beamColor);
        }
    }

    @Override
    public void render(final T be, final float partialTicks, final PoseStack poseStack,
            final MultiBufferSource bufferIn, final int lightIn, final int overlayIn) {
        if (!this.hasRenderData(be)) {
            return;
        }

        getRenderData(be).stream()
                // There isn't much point to rendering zero-length beams, so we skip them
                .filter(data -> data.beamLength > 0)
                .forEach(data -> {
                    final long gameTime = be.getLevel().getGameTime();

                    poseStack.pushPose();

                    // Move the render point to the center of the block
                    poseStack.translate(0.5, 0.5, 0.5);

                    // Rotate so that we're facing the right direction
                    poseStack.mulPose(ROTATION_MAP.get(data.direction));

                    // Move the render point "up" (which is now forward)
                    poseStack.translate(0.0, 0.5 - BEAM_PENETRATION_DEPTH, 0.0);

                    renderBeaconBeam(poseStack, bufferIn, partialTicks, gameTime,
                            data.beamLength + (BEAM_PENETRATION_DEPTH * 2), data.beamColor);

                    poseStack.popPose();
                });
    }

    public static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource bufferIn, float partialTicks,
            long gameTime,
            float beamLength, int colorHex) {

        poseStack.pushPose();

        final Color color = new Color(colorHex);
        final float colorR = color.getRed() / 256f;
        final float colorG = color.getGreen() / 256f;
        final float colorB = color.getBlue() / 256f;

        final float animationFrame = (float)Math.floorMod(gameTime, 40) + partialTicks;
        final float animationFrameAdjusted = beamLength < 0 ? animationFrame : -animationFrame;
        final float textureStep = Mth
                .frac(animationFrameAdjusted * 0.2F - (float)Mth.floor(animationFrameAdjusted * 0.1F));

        // ***
        // Render core beam
        poseStack.pushPose();
        // Vanilla code - continuously rotates the beam
        // poseStack.mulPose(Vector3f.YP.rotationDegrees((animationFrame * 2.25F) - 45.0F));
        // I think it looks better to not do that, though
        // Rotate 45 degrees in order to line up with the BNL block model
        poseStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));

        final float textureEndBeam = -1.0F + textureStep;
        final float textureStartBeam = ((beamLength) * (0.5F / BEAM_WIDTH)) + textureEndBeam;

        renderPart(poseStack,
                bufferIn.getBuffer(RenderType.beaconBeam(BEAM_TEXTURE_LOCATION, false)),
                colorR, colorG, colorB, 1.0F,
                0, beamLength,
                0f, BEAM_WIDTH, BEAM_WIDTH, 0f, -BEAM_WIDTH, 0f, 0f, -BEAM_WIDTH,
                0f, 1f, textureStartBeam, textureEndBeam);

        poseStack.popPose();
        // ***

        // ***
        // Render beam outline
        final float textureEndOutline = -1.0F + textureStep;
        final float textureStartOutline = ((float)beamLength) + textureEndBeam;

        renderPart(poseStack,
                bufferIn.getBuffer(RenderType.beaconBeam(BEAM_TEXTURE_LOCATION, true)),
                colorR, colorG, colorB, 0.125F,
                0, beamLength,
                -OUTLINE_WIDTH, -OUTLINE_WIDTH, OUTLINE_WIDTH, -OUTLINE_WIDTH, -OUTLINE_WIDTH, OUTLINE_WIDTH,
                OUTLINE_WIDTH, OUTLINE_WIDTH,
                0f, 1f, textureStartOutline, textureEndOutline);
        // ***

        poseStack.popPose();
    }

    private static void renderPart(PoseStack poseStack, VertexConsumer buffer,
            float colorR, float colorG, float colorB, float colorAlpha,
            float yStart, float yEnd,
            /* Computer graphics things are happening here, don't ask me to explain them */
            float p_112164_, float p_112165_, float p_112166_, float p_112167_, float p_112168_, float p_112169_,
            float p_112170_, float p_112171_,
            float uStart, float uEnd, float vStart, float vEnd) {
        PoseStack.Pose posestack$pose = poseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();
        renderQuad(matrix4f, matrix3f, buffer, colorR, colorG, colorB, colorAlpha,
                yStart, yEnd, p_112164_, p_112165_, p_112166_, p_112167_,
                uStart, uEnd, vStart, vEnd);
        renderQuad(matrix4f, matrix3f, buffer, colorR, colorG, colorB, colorAlpha,
                yStart, yEnd, p_112170_, p_112171_, p_112168_, p_112169_,
                uStart, uEnd, vStart, vEnd);
        renderQuad(matrix4f, matrix3f, buffer, colorR, colorG, colorB, colorAlpha,
                yStart, yEnd, p_112166_, p_112167_, p_112170_, p_112171_,
                uStart, uEnd, vStart, vEnd);
        renderQuad(matrix4f, matrix3f, buffer, colorR, colorG, colorB, colorAlpha,
                yStart, yEnd, p_112168_, p_112169_, p_112164_, p_112165_,
                uStart, uEnd, vStart, vEnd);
    }

    private static void renderQuad(Matrix4f poseMatrix, Matrix3f normalMatrix, VertexConsumer buffer,
            float colorR, float colorG, float colorB, float colorAlpha,
            float yStart, float yEnd, float xStart, float zStart, float xEnd, float zEnd,
            float uStart, float uEnd, float vStart, float vEnd) {
        addVertex(poseMatrix, normalMatrix, buffer, colorR, colorG, colorB, colorAlpha,
                yEnd, xStart, zStart, uEnd, vStart);
        addVertex(poseMatrix, normalMatrix, buffer, colorR, colorG, colorB, colorAlpha,
                yStart, xStart, zStart, uEnd, vEnd);
        addVertex(poseMatrix, normalMatrix, buffer, colorR, colorG, colorB, colorAlpha,
                yStart, xEnd, zEnd, uStart, vEnd);
        addVertex(poseMatrix, normalMatrix, buffer, colorR, colorG, colorB, colorAlpha,
                yEnd, xEnd, zEnd, uStart, vStart);
    }

    private static void addVertex(Matrix4f poseMatrix, Matrix3f normalMatrix, VertexConsumer buffer,
            float colorR, float colorG, float colorB, float colorAlpha,
            float yVal, float xVal, float zVal,
            float uVal, float vVal) {
        buffer.vertex(poseMatrix, xVal, yVal, zVal)
                .color(colorR, colorG, colorB, colorAlpha)
                .uv(uVal, vVal)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880) // It Just Works (TM)
                .normal(normalMatrix, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(final T ignored) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(final T be, final Vec3 pos) {
        if (!this.hasRenderData(be)) {
            return false;
        }

        return Vec3.atCenterOf(be.getBlockPos()).multiply(1.0D, 0.0D, 1.0D)
                .closerThan(pos.multiply(1.0D, 0.0D, 1.0D), (double)this.getViewDistance());
    }
}
