package com.tycherin.impen.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class DisintegratorDamageParticle extends TextureSheetParticle {

    /*
     * If at some point I want to add more options to how these particles are created and/or behave (e.g. setting the
     * particle's lifespan based on the PD's speed), I'd need to implement a custom ParticleOptions subclass
     */
    public static final SimpleParticleType TYPE = new SimpleParticleType(false);

    private static final int LIFESPAN_TICKS = 5;

    protected DisintegratorDamageParticle(final ClientLevel level,
            final double xPos, final double yPos, final double zPos) {
        super(level, xPos, yPos, zPos);
        this.hasPhysics = false;
        this.lifetime = LIFESPAN_TICKS;
        this.quadSize = 0.2f + (float)this.random.nextDouble(0.2);
        this.yd = 1.0 / LIFESPAN_TICKS;
        this.setPos(
                this.x + this.random.nextDouble(0.4) - 0.2,
                this.y,
                this.z + this.random.nextDouble(0.4) - 0.2);
    }

    @Override
    public void tick() {
        // Deliberately NOT calling super.tick() to avoid all the movement shenanigans it does
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        else {
            this.move(this.xd, this.yd, this.zd);
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
            final var particle = new DisintegratorDamageParticle(level, xPos, yPos, zPos);
            particle.pickSprite(sprites);
            return particle;
        }
    }

    public static class ProviderFactory implements SpriteParticleRegistration<SimpleParticleType> {
        @Override
        public ParticleProvider<SimpleParticleType> create(final SpriteSet sprites) {
            return new DisintegratorDamageParticle.Provider(sprites);
        }
    }
}
