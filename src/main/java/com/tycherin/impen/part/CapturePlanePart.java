package com.tycherin.impen.part;

import java.util.List;

import com.tycherin.impen.util.MobUtil;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.api.util.AECableType;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.parts.BasicStatePart;
import appeng.parts.automation.PlaneConnectionHelper;
import appeng.parts.automation.PlaneModelData;
import appeng.parts.automation.PlaneModels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.ProjectileImpactEvent;

public class CapturePlanePart extends BasicStatePart {

    private static final PlaneModels MODELS = new PlaneModels("part/capture_plane",
            "part/capture_plane_on");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final PlaneConnectionHelper connectionHelper = new PlaneConnectionHelper(this);
    private final IActionSource actionSource = new MachineSource(this);

    public CapturePlanePart(final IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        // Same behavior here as in AnnihilationPlanePart
        if (bch.isBBCollision()) {
            bch.addBox(0, 0, 14, 16, 16, 15.5);
            return;
        }
        connectionHelper.getBoxes(bch);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Override
    public IModelData getModelData() {
        return new PlaneModelData(connectionHelper.getConnections());
    }

    @Override
    public void onNeighborChanged(final BlockGetter level, final BlockPos pos, final BlockPos neighbor) {
        connectionHelper.updateConnections();
    }

    @Override
    public float getCableConnectionLength(final AECableType cable) {
        return 1;
    }

    @Override
    public void onEntityCollision(final Entity entity) {
        if (!entity.isAlive() || isClientSide() || !this.getMainNode().isActive()) {
            return;
        }

        var grid = getMainNode().getGrid();
        if (grid == null) {
            return;
        }

        if (entity instanceof Mob mob && MobUtil.canBeCaptured(mob)) {
            final var spawnEgg = ForgeSpawnEggItem.fromEntityType(mob.getType());
            if (spawnEgg != null
                    && this.insertIntoGrid(AEItemKey.of(spawnEgg), 1, Actionable.MODULATE) > 0) {
                // This check is technically unnecessary - in order for insertIntoGrid() to work, we must be on the
                // server anyway, since the AE network doesn't exist on the client
                if (this.getLevel() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL, mob.getX(), mob.getY(), mob.getZ(),
                            // Particle count - spawn more particles for bigger mobs
                            // This doesn't actually work very well because all the particles spawn in the same place
                            (int)Math.ceil(mob.getBoundingBox().getSize() * 3),
                            // These params are ignored
                            0, 0, 0, 0);
                    serverLevel.playSound(null, this.getBlockEntity().getBlockPos(), SoundEvents.ENDERMAN_TELEPORT,
                            SoundSource.BLOCKS, 1.0f, 1.0f);
                }
                mob.remove(RemovalReason.DISCARDED);
            }
        }
    }

    public void onHit(final ProjectileImpactEvent event) {
        if (!this.getMainNode().isActive()) {
            return;
        }

        final Projectile projectile = event.getProjectile();
        final ItemStack projectileItem;
        if (projectile instanceof AbstractArrow arrowProjectile) {
            final var pickupItem = arrowProjectile.getPickupItem();
            if (pickupItem == null || pickupItem.isEmpty()) {
                // Some arrows (e.g. arrows shot by skeletons) are marked as not pick up...able, but we want to capture
                // those as best we can anyway. So if the previous step failed, fall back on spawning a default
                // projectile.
                if (arrowProjectile instanceof Arrow) {
                    projectileItem = Items.ARROW.getDefaultInstance();
                }
                else if (arrowProjectile instanceof SpectralArrow) {
                    projectileItem = Items.SPECTRAL_ARROW.getDefaultInstance();
                }
                else {
                    projectileItem = ItemStack.EMPTY;
                }
            }
            else {
                projectileItem = pickupItem;
            }
        }
        else if (projectile instanceof ThrowableItemProjectile itemProjectile) {
            projectileItem = itemProjectile.getItem();
        }
        // These next ones are just for fun
        else if (projectile instanceof WitherSkull) {
            projectileItem = Items.WITHER_SKELETON_SKULL.getDefaultInstance();
        }
        else if (projectile instanceof ShulkerBullet) {
            projectileItem = Items.SHULKER_SHELL.getDefaultInstance();
        }
        else if (projectile instanceof SmallFireball sf && sf.getEffectSource() instanceof Blaze) {
            projectileItem = Items.BLAZE_POWDER.getDefaultInstance();
        }
        else if (projectile instanceof LargeFireball lf && lf.getEffectSource() instanceof Ghast) {
            projectileItem = Items.GHAST_TEAR.getDefaultInstance();
        }
        else if (projectile instanceof FireworkRocketEntity) {
            projectileItem = Items.FIREWORK_ROCKET.getDefaultInstance();
        }
        else {
            projectileItem = ItemStack.EMPTY;
        }

        if (!projectileItem.isEmpty()) {
            final IGrid grid = this.getGridNode().getGrid();
            final var insertCount = StorageHelper.poweredInsert(grid.getEnergyService(),
                    grid.getStorageService().getInventory(), AEItemKey.of(projectileItem), projectileItem.getCount(),
                    this.actionSource);
            if (insertCount > 0) {
                projectile.discard();
                event.setCanceled(true);
            }
        }
    }

    public static void handleProjectileEvent(final ProjectileImpactEvent event) {
        if (!event.getProjectile().level.isClientSide() && event.getRayTraceResult() instanceof BlockHitResult bhr) {
            final var be = event.getProjectile().level.getBlockEntity(bhr.getBlockPos());
            if (be != null && be instanceof CableBusBlockEntity cbbe
                    && cbbe.getPart(bhr.getDirection()) instanceof CapturePlanePart cpp) {
                cpp.onHit(event);
            }
        }
    }

    private long insertIntoGrid(final AEKey what, final long amount, final Actionable mode) {
        var grid = getMainNode().getGrid();
        if (grid == null) {
            return 0;
        }
        return StorageHelper.poweredInsert(grid.getEnergyService(), grid.getStorageService().getInventory(),
                what, amount, this.actionSource, mode);
    }
}
