package com.tycherin.impen.entity;

import java.util.List;
import java.util.Random;

import com.tycherin.impen.ImpracticalEnergisticsMod;

import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

public class FluixCatalystEntity extends ItemEntity {

    private static final Random RAND = new Random();
    
    private int transformTime = 0;
    
    public FluixCatalystEntity(final EntityType<FluixCatalystEntity> entityType, final Level level) {
        super(entityType, level);
    }
    
    public FluixCatalystEntity(final Level level, final double x, final double y, final double z, final ItemStack stack) {
        this(ImpracticalEnergisticsMod.FLUIX_CATALYST_ENTITY.get(), level);
        this.setPos(x, y, z);
        this.setYRot(this.random.nextFloat() * 360f);
        this.setDeltaMovement((this.random.nextDouble() * 0.2) - 0.1, 0.2, (this.random.nextDouble() * 0.2) - 0.1);
        this.setItem(stack);
        this.lifespan = stack.getEntityLifespan(level);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
    @Override
    public void tick() {
        super.tick();

        if (this.isRemoved() || !AEConfig.instance().isInWorldFluixEnabled()) {
            return;
        }

        final int j = Mth.floor(this.getX());
        final int i = Mth.floor((this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D);
        final int k = Mth.floor(this.getZ());

        BlockState state = this.level.getBlockState(new BlockPos(j, i, k));
        final Material mat = state.getMaterial();

        if (!level.isClientSide() && mat.isLiquid()) {
            this.transformTime++;
            if (this.transformTime > 60 && !this.transform()) {
                this.transformTime = 0;
            }
        } else {
            this.transformTime = 0;
        }
    }

    private boolean transform() {

        final AABB region = new AABB(
                this.getX() - 1, this.getY() - 1, this.getZ() - 1,
                this.getX() + 1, this.getY() + 1, this.getZ() + 1);
        final List<Entity> l = this.level.getEntities(this, region);

        ItemEntity redstone = null;
        ItemEntity chargedCertus = null;

        for (Entity e : l) {
            if (e instanceof ItemEntity && !e.isRemoved()) {
                final ItemStack other = ((ItemEntity) e).getItem();
                if (!other.isEmpty()) {
                    if (ItemStack.isSame(other, new ItemStack(Items.REDSTONE))) {
                        redstone = (ItemEntity) e;
                    }

                    if (ItemStack.isSame(other, new ItemStack(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED))) {
                        chargedCertus = (ItemEntity) e;
                    }
                }
            }
        }

        if (redstone != null && chargedCertus != null) {
            this.getItem().grow(-1);
            redstone.getItem().grow(-1);
            chargedCertus.getItem().grow(-1);

            if (this.getItem().getCount() <= 0) {
                this.discard();
            }

            if (redstone.getItem().getCount() <= 0) {
                redstone.discard();
            }

            if (chargedCertus.getItem().getCount()  <= 0) {
                chargedCertus.discard();
            }

            final double x = Math.floor(this.getX()) + .25d + RAND.nextDouble() * .5;
            final double y = Math.floor(this.getY()) + .25d + RAND.nextDouble() * .5;
            final double z = Math.floor(this.getZ()) + .25d + RAND.nextDouble() * .5;
            final double xSpeed = RAND.nextDouble() * .25 - 0.125;
            final double ySpeed = RAND.nextDouble() * .25 - 0.125;
            final double zSpeed = RAND.nextDouble() * .25 - 0.125;

            final ItemEntity entity = new ItemEntity(this.level, x, y, z, AEItems.FLUIX_DUST.stack(4));
            entity.setDeltaMovement(xSpeed, ySpeed, zSpeed);
            this.level.addFreshEntity(entity);

            return true;
        }
        else {
            return false;
        }
    }

}
