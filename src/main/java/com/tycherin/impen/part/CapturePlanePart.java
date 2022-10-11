package com.tycherin.impen.part;

import java.util.List;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
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
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
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
import net.minecraftforge.event.entity.ProjectileImpactEvent;

public class CapturePlanePart extends BasicStatePart {

    // TODO Have an actual model for this
    private static final PlaneModels MODELS = new PlaneModels("part/annihilation_plane",
            "part/annihilation_plane_on");

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
                // TODO It would be helpful if there was a visual effect here
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
}
