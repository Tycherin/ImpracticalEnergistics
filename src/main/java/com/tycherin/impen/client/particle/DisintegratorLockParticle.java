package com.tycherin.impen.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class DisintegratorLockParticle extends TextureSheetParticle {

    public static final SimpleParticleType TYPE = new SimpleParticleType(false);

    private static final int LIFESPAN_TICKS = 20; // Should be a multiple of the spawn rate
    private static final double RADIANS_PER_TICK = 360.0 / LIFESPAN_TICKS
            * 0.01745329 /* degrees to radians conversion */;

    // If I want to customize this to the size of the mob being locked, I'd need to implement a custom ParticleOptions
    // and use that instead of SimpleParticleType
    private static final double RADIUS = 1.5;

    private final double xOrigin;
    private final double zOrigin;

    protected DisintegratorLockParticle(final ClientLevel level,
            final double xPos, final double yPos, final double zPos) {
        super(level, xPos + RADIUS, yPos, zPos);
        this.hasPhysics = false;
        this.lifetime = LIFESPAN_TICKS + 1; // +1 to reset from the fake tick below
        this.gravity = 0f;
        this.friction = 0f;
        this.quadSize = 0.3f;
        this.yd = 0;

        this.xOrigin = xPos;
        this.zOrigin = zPos;

        // Force a tick so the particle renders properly when created
        this.tick();
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        else {
            /*
             * Oh boy, maths!
             * 
             * We want the particle to orbit in a circle around the spawn point. Particles are weird about movement &
             * position tracking, so we set the xd & zd params equal to the movement we want to see in the next tick.
             * 
             * To get that, we take the difference between the position that the particle should be in this tick and the
             * position it should be in next tick. This involves doing trigonometry because the alternative was doing
             * calculus and that sounded messier.
             * 
             * As for the xo/x shenanigans, the tick() and move() logic in superclasses does a bunch of stuff we don't
             * care about here. Rendering the particle only really cares about the difference between the old & current
             * positions, so we set those and ignore the rest.
             */
            this.xo = (Math.cos(RADIANS_PER_TICK * this.age) * RADIUS) + this.xOrigin;
            this.x = (Math.cos(RADIANS_PER_TICK * (this.age + 1)) * RADIUS) + this.xOrigin;
            this.zo = (Math.sin(RADIANS_PER_TICK * this.age) * RADIUS) + this.zOrigin;
            this.z = (Math.sin(RADIANS_PER_TICK * (this.age + 1)) * RADIUS) + this.zOrigin;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(final SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(final SimpleParticleType type, final ClientLevel level,
                final double xPos, final double yPos, final double zPos,
                final double xSpd, final double ySpd, final double zSpd) {
            final var particle = new DisintegratorLockParticle(level, xPos, yPos, zPos);
            particle.pickSprite(sprites);
            return particle;
        }
    }

    public static class ProviderFactory implements SpriteParticleRegistration<SimpleParticleType> {
        @Override
        public ParticleProvider<SimpleParticleType> create(final SpriteSet sprites) {
            return new DisintegratorLockParticle.Provider(sprites);
        }
    }
}
