package com.tycherin.impen.blockentity;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.config.ImpenConfig;
import com.tycherin.impen.recipe.AtmosphericCrystallizerRecipe;
import com.tycherin.impen.util.AEPowerUtil;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AtmosphericCrystallizerBlockEntity extends AENetworkPowerBlockEntity
        implements IGridTickable, IUpgradeableObject {

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new InventoryItemFilter());
    private final IUpgradeInventory upgrades;
    private final int baseProgressTicks;
    private final double basePowerDraw;

    private int progress = 0;

    public AtmosphericCrystallizerBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER.blockEntity(), pos, blockState);

        this.getMainNode()
                .addService(IGridTickable.class, this)
                .setExposedOnSides(EnumSet.complementOf(EnumSet.of(Direction.UP)));
        this.upgrades = UpgradeInventories.forMachine(ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER.item(), 3,
                this::saveChanges);
        this.baseProgressTicks = ImpenConfig.SETTINGS.atmosphericCrystallizerWorkRate();
        this.basePowerDraw = ImpenConfig.POWER.atmosphericCrystallizerCost();
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
        this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(1, 1, !this.hasRecipe(), false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (!this.hasRecipe()) {
            return TickRateModulation.SLEEP;
        }

        // Attempt to extract power
        final var workTicks = ticksSinceLastCall * this.getWorkRate();
        final var powerDesired = this.basePowerDraw * workTicks;
        if (AEPowerUtil.drawPower(this, powerDesired)) {
            this.progress += workTicks;
        }

        if (this.progress > this.baseProgressTicks) {
            final ItemStack leftover = this.inv.addItems(this.getRecipe().get().getResult());
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

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.baseProgressTicks;
    }

    public Optional<AtmosphericCrystallizerRecipe> getRecipe() {
        return AtmosphericCrystallizerRecipe.getRecipe(this.getLevel());
    }

    public int getWorkRate() {
        return 1 + this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD);
    }

    @Override
    public void saveAdditional(final CompoundTag data) {
        super.saveAdditional(data);
        this.upgrades.writeToNBT(data, "upgrades");
    }

    @Override
    public void loadTag(final CompoundTag data) {
        super.loadTag(data);
        this.upgrades.readFromNBT(data, "upgrades");
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeInt(progress);

        for (int i = 0; i < this.inv.size(); i++) {
            data.writeItem(inv.getStackInSlot(i));
        }
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);

        final int prevProgress = this.progress;
        this.progress = data.readInt();
        ret |= (prevProgress != this.progress);

        for (int i = 0; i < this.inv.size(); i++) {
            this.inv.setItemDirect(i, data.readItem());
        }

        return ret;
    }

    @Override
    public void addAdditionalDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        upgrades.forEach(drops::add);
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
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
