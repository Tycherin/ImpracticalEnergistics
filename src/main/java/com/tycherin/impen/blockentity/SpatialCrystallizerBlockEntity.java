package com.tycherin.impen.blockentity;

import java.util.EnumSet;
import java.util.Optional;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.recipe.SpatialCrystallizerRecipe;
import com.tycherin.impen.recipe.SpatialCrystallizerRecipeManager;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SpatialCrystallizerBlockEntity extends AENetworkInvBlockEntity implements IGridTickable {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int PROGRESS_TICKS = 60 * 20;

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new InventoryItemFilter());

    private int progress = 0;

    public SpatialCrystallizerBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpracticalEnergisticsMod.SPATIAL_CRYSTALLIZER_BE.get(), pos, blockState);

        this.getMainNode()
                .setIdlePowerUsage(10.0)
                .addService(IGridTickable.class, this);
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getMainNode().setExposedOnSides(EnumSet.of(this.getForward().getOpposite()));
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        this.getMainNode().setExposedOnSides(EnumSet.of(inForward.getOpposite()));
    }

    @Override
    public InternalInventory getInternalInventory() {
        return inv;
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(final Direction side) {
        return this.invExt;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        this.markForUpdate();
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(1, 10, !this.hasRecipe(), false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (!this.hasRecipe()) {
            return TickRateModulation.SLEEP;
        }

        this.progress += ticksSinceLastCall;

        if (this.progress > PROGRESS_TICKS) {
            final ItemStack leftover = this.inv.addItems(this.getRecipe().get().getResultItem());
            if (leftover.equals(ItemStack.EMPTY)) {
                this.progress = 0;
            }
            else {
                // We attempted to add the item to the output, but it didn't work. Most likely, the output is full, so
                // we should sleep until the inventory changes.
                return TickRateModulation.SLEEP;
            }
        }

        return TickRateModulation.SAME;
    }

    public boolean hasRecipe() {
        return this.getRecipe().isPresent();
    }

    public Optional<SpatialCrystallizerRecipe> getRecipe() {
        return SpatialCrystallizerRecipeManager.getRecipe(this.getLevel());
    }

    private class InventoryItemFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(final InternalInventory inv, final int slot, final int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(final InternalInventory inv, final int slot, final ItemStack stack) {
            return false;
        }
    }
}
