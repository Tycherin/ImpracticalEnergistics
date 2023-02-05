package com.tycherin.impen.blockentity.rift;

import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.MachineBlockEntity;
import com.tycherin.impen.config.ImpenConfig;
import com.tycherin.impen.item.SpatialRiftCellItem;
import com.tycherin.impen.logic.rift.SpatialRiftCellDataManager;
import com.tycherin.impen.logic.rift.SpatialRiftSpawnerFuelHelper;
import com.tycherin.impen.logic.rift.SpatialRiftCellDataManager.SpatialRiftCellData;
import com.tycherin.impen.recipe.SpatialRiftSpawnerRecipe;
import com.tycherin.impen.util.ManagedOutputInventory;
import com.tycherin.impen.util.SpatialRiftUtil;

import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.items.storage.SpatialStorageCellItem;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SpatialRiftSpawnerBlockEntity extends MachineBlockEntity {

    private static final int DEFAULT_SPEED_TICKS = 20 * 5; // 5s
    private static final int MAX_STORED_FUEL_AMOUNT = 1000;
    private static final String TAG_FUEL = "fuel";

    private static class Slots {
        public static final int INPUT = 0;
        public static final int OUTPUT = 1;
        public static final int FUEL = 2;
    }

    private final InternalInventory inputInv = new AppEngInternalInventory(this, 1, 1);
    private final InternalInventory fuelInv = new AppEngInternalInventory(this, 1, 64);
    private final InternalInventory outputInv = new AppEngInternalInventory(this, 1, 64);
    private final InternalInventory invExt;
    private final InternalInventory inv = new CombinedInternalInventory(inputInv, fuelInv, outputInv);
    private final int baseSpeedTicks;
    private final MachineOperation op;
    private final IAEItemFilter filter;
    private final SpatialRiftSpawnerFuelHelper fuelHelper = new SpatialRiftSpawnerFuelHelper();
    /** Convenience field just so we don't have to build this every time we want to scan it */
    private final Container inputContainer;
    private final double powerPerTick;
    private final ManagedOutputInventory outSlot;

    private int storedFuel = 0;

    public SpatialRiftSpawnerBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.SPATIAL_RIFT_SPAWNER, pos, blockState);
        this.baseSpeedTicks = DEFAULT_SPEED_TICKS;
        this.op = new MachineOperation(
                this.baseSpeedTicks,
                this::doOperation);
        this.filter = new InventoryItemFilter();
        this.invExt = new FilteredInternalInventory(inv, this.filter);
        this.inputContainer = this.inputInv.toContainer();
        this.powerPerTick = ImpenConfig.POWER.spatialRiftSpawnerCost();
        this.outSlot = new ManagedOutputInventory(this.inv.getSlotInv(Slots.OUTPUT));
    }

    // ***
    // Interesting stuff
    // ***

    private boolean enableOperation() {
        final var recipeOpt = this.getRecipe(this.inputContainer);
        return recipeOpt.isPresent()
                && recipeOpt.get().getFuelCost() <= this.storedFuel
                && this.outSlot.canAdd(recipeOpt.get().getResultItem());
    }

    private void recheckFuel() {
        // If we're not full on fuel...
        if (this.storedFuel < MAX_STORED_FUEL_AMOUNT) {
            final ItemStack fuelIs = this.inv.getStackInSlot(Slots.FUEL);

            // And there is a fuel item present...
            if (!fuelIs.isEmpty()) {

                // ...consume as much as is available, or as much as we can take
                final int currentFuelCount = fuelIs.getCount();
                final int fuelValue = fuelHelper.getValue(fuelIs.getItem());
                final int numToConsume = Math.min(
                        (MAX_STORED_FUEL_AMOUNT - this.storedFuel) / fuelValue,
                        currentFuelCount);

                fuelIs.setCount(currentFuelCount - numToConsume);
                this.storedFuel += fuelValue * numToConsume;
            }
        }
    }

    protected boolean doOperation() {
        final Optional<SpatialRiftSpawnerRecipe> recipe = this.getRecipe(this.inputContainer);
        this.storedFuel -= recipe.get().getFuelCost();
        this.recheckFuel();
        
        final ItemStack input = this.inv.getStackInSlot(Slots.INPUT);
        final ItemStack output = this.getOutputForItem(input);

        if (this.outSlot.tryAdd(output)) {
            input.setCount(input.getCount() - 1);
            return true;
        }
        else {
            // Failed to insert into the output for some reason
            return false;
        }
    }

    private ItemStack getOutputForItem(final ItemStack input) {
        final var spatialCellOpt = SpatialRiftUtil.getSpatialCell(input);
        if (spatialCellOpt.isPresent()) {
            return getOutputForSpatialCell(input, spatialCellOpt.get());
        }
        else {
            return getRecipe(this.inputContainer)
                    .map(SpatialRiftSpawnerRecipe::getResultItem)
                    .orElse(ItemStack.EMPTY);
        }
    }

    private ItemStack getOutputForSpatialCell(final ItemStack inputItem, final ISpatialStorageCell cell) {
        final int plotId = cell.getAllocatedPlotId(inputItem);
        if (plotId == -1) {
            // No plot allocated. This shouldn't happen normally because of the input filter, but in case it does, we
            // fall back on returning the cell as-is as a safety measure.
            return inputItem;
        }

        final ItemStack is = new ItemStack(
                SpatialRiftCellItem.getMatchingCell((SpatialStorageCellItem)inputItem.getItem()));
        ((SpatialRiftCellItem)is.getItem()).setPlotId(is, plotId);
        SpatialRiftCellDataManager.INSTANCE.putDataForPlot(new SpatialRiftCellData(plotId));

        return is;
    }

    private class InventoryItemFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(final InternalInventory inv, final int slot, final int amount) {
            return slot == Slots.OUTPUT;
        }

        @Override
        public boolean allowInsert(final InternalInventory inv, final int slot, final ItemStack stack) {
            if (slot == Slots.INPUT) {
                final var recipeOpt = getRecipe(new SimpleContainer(stack));
                if (recipeOpt.isPresent()) {
                    if (stack.getItem() instanceof SpatialRiftCellItem) {
                        // Only allow spatial cells that have allocated plots already
                        return SpatialRiftUtil.getPlotId(stack).isPresent();
                    }
                    else {
                        // Normal recipe - ok to insert
                        return true;
                    }
                }
                else {
                    // No recipe - don't insert
                    return false;
                }
            }
            else if (slot == Slots.FUEL) {
                return fuelHelper.isFuel(stack);
            }
            else {
                return false;
            }
        }
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot) {
        super.onChangeInventory(inv, slot);
        if (slot == Slots.INPUT) {
            this.resetOperation();
        }
        // Changing fuel or output doesn't change an in-progress operation
    }

    // ***
    // Boring stuff
    // ***

    @Override
    protected Optional<MachineOperation> getOperation() {
        if (!this.enableOperation()) {
            return Optional.empty();
        }
        else {
            return Optional.of(op);
        }
    }

    private Optional<SpatialRiftSpawnerRecipe> getRecipe(final Container c) {
        return this.level.getRecipeManager()
                .getRecipeFor(ImpenRegistry.SPATIAL_RIFT_SPAWNER_RECIPE_TYPE.get(), c, level);
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(final Direction side) {
        return this.invExt;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    protected int progressOperation() {
        return 1;
    }

    public int getMaxProgress() {
        return baseSpeedTicks;
    }

    @Override
    public IAEItemFilter getInventoryFilter() {
        return this.filter;
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        // Recheck fuel FIRST, in case it allows us to start a new operation
        this.recheckFuel();
        final var modulationRate = super.tickingRequest(node, ticksSinceLastCall);
        return modulationRate;
    }

    @Override
    public void saveAdditional(final CompoundTag data) {
        super.saveAdditional(data);
        data.putInt(TAG_FUEL, this.storedFuel);
    }

    @Override
    public void loadTag(final CompoundTag data) {
        super.loadTag(data);
        if (data.contains(TAG_FUEL)) {
            this.storedFuel = data.getInt(TAG_FUEL);
        }
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeInt(this.storedFuel);
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);

        final int prevStoredFuel = this.storedFuel;
        this.storedFuel = data.readInt();
        ret |= (prevStoredFuel != this.storedFuel);

        return ret;
    }

    public int getMaxFuel() {
        return MAX_STORED_FUEL_AMOUNT;
    }

    public int getStoredFuel() {
        return this.storedFuel;
    }

    @Override
    protected double getPowerDraw() {
        return this.powerPerTick;
    }
}
