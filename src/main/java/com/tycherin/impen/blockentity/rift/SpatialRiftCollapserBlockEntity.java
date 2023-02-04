package com.tycherin.impen.blockentity.rift;

import java.util.Optional;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.MachineBlockEntity;
import com.tycherin.impen.config.ImpenConfig;
import com.tycherin.impen.item.SpatialRiftCellItem;
import com.tycherin.impen.logic.SpatialRiftCellDataManager;
import com.tycherin.impen.logic.SpatialRiftCellDataManager.SpatialRiftCellData;
import com.tycherin.impen.logic.SpatialRiftCollapserLogic;
import com.tycherin.impen.recipe.SpatialRiftCollapserRecipe;
import com.tycherin.impen.util.ImpenFilteredInventoryWrapper;
import com.tycherin.impen.util.ManagedOutputInventory;

import appeng.api.inventories.InternalInventory;
import appeng.core.definitions.AEItems;
import appeng.items.storage.SpatialStorageCellItem;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SpatialRiftCollapserBlockEntity extends MachineBlockEntity {

    private static final Logger LOGGER = LogUtils.getLogger();

    // TODO Change the time required for an operation based on the size of the plot
    private static final int DEFAULT_SPEED_TICKS = 20 * 5; // 5s

    private final ImpenFilteredInventoryWrapper invWrapper;
    private final MachineOperation op;
    private final SpatialRiftCollapserLogic logic;
    private final IAEItemFilter filter;
    private final double powerPerTick;
    /** Convenience field just so we don't have to build this every time we want to scan it */
    private final Container inputContainer;
    private final ManagedOutputInventory outSlot;

    public SpatialRiftCollapserBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        super(ImpenRegistry.SPATIAL_RIFT_COLLAPSER, blockPos, blockState);
        this.op = new MachineOperation(
                DEFAULT_SPEED_TICKS,
                this::doOperation);
        this.logic = new SpatialRiftCollapserLogic();
        this.filter = new InventoryItemFilter();
        this.invWrapper = new ImpenFilteredInventoryWrapper(this, this.filter);
        this.inputContainer = this.invWrapper.getInput().toContainer();
        this.powerPerTick = ImpenConfig.POWER.spatialRiftCollapserCost();
        this.outSlot = new ManagedOutputInventory(this.invWrapper.getOutput());
    }

    @Override
    protected Optional<MachineOperation> getOperation() {
        if (!this.enableOperation()) {
            return Optional.empty();
        }
        else {
            return Optional.of(op);
        }
    }

    private boolean enableOperation() {
        final ItemStack input = this.invWrapper.getInput().getStackInSlot(0);
        if (input.isEmpty()) {
            // No input - nothing to do
            return false;
        }

        final var recipeOpt = getRecipeForInput(this.inputContainer);
        if (recipeOpt.isEmpty()) {
            // No recipe for input - weird, but nothing to do
            return false;
        }
        else {
            // Recipe found - check if there's room for the result item
            return outSlot.canAdd(recipeOpt.get().getResultItem());
        }
    }

    protected boolean doOperation() {
        final ItemStack input = this.invWrapper.getInput().getStackInSlot(0);
        if (input.getItem() instanceof SpatialRiftCellItem item) {
            // TODO Switch this to progress the operation gradually rather than doing it all at once
            // (mainly to reduce performance impact)

            final int plotId = item.getPlotId(input);
            final Optional<SpatialRiftCellData> dataOpt = SpatialRiftCellDataManager.INSTANCE.getDataForPlot(plotId);
            if (dataOpt.isEmpty()) {
                LOGGER.warn("Rift cell data not found for {}", plotId);
                return false;
            }

            if (!this.invWrapper.getOutput().getStackInSlot(0).isEmpty()) {
                // Spatial cell recipes produce unique outputs, so if there's anything there already, we can't proceed
                return false;
            }

            final SpatialRiftCellData data = dataOpt.get();

            final ItemStack output = item.getOriginalItem().asItem().getDefaultInstance();
            final SpatialStoragePlot plot = SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
            ((SpatialStorageCellItem)AEItems.SPATIAL_CELL2.asItem()).setStoredDimension(output, plotId, plot.getSize());

            logic.addBlocksToPlot(plot, data);

            this.invWrapper.getInput().setItemDirect(0, ItemStack.EMPTY);
            this.invWrapper.getOutput().setItemDirect(0, output);
            return true;
        }
        else {
            // Regular crafting recipe
            final var recipeOpt = getRecipeForInput(this.inputContainer);
            if (recipeOpt.isEmpty()) {
                LOGGER.warn("No recipe found for input item {}", input);
                this.invWrapper.getInput().setItemDirect(0, ItemStack.EMPTY);
                this.invWrapper.getOutput().setItemDirect(0, input);
                return true;
            }
            else {
                if (this.outSlot.tryAdd(recipeOpt.get().getResultItem())) {
                    input.setCount(input.getCount() - 1);
                    return true;
                }
                else {
                    // For some reason, we couldn't insert into the output
                    return false;
                }
            }
        }
    }

    @Override
    protected int progressOperation() {
        return 1;
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(final Direction side) {
        return this.invWrapper.getExternal();
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.invWrapper.getInternal();
    }

    public int getMaxProgress() {
        return DEFAULT_SPEED_TICKS;
    }

    private class InventoryItemFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(final InternalInventory inv, final int slot, final int amount) {
            return slot == 1;
        }

        @Override
        public boolean allowInsert(final InternalInventory inv, final int slot, final ItemStack stack) {
            if (slot == 0 && invWrapper.getInput().isEmpty()) {
                // We don't check if there's room for the output here, only that an output is possible
                return getRecipeForInput(new SimpleContainer(stack)).isPresent();
            }
            else {
                return false;
            }
        }
    }

    @Override
    public IAEItemFilter getInventoryFilter() {
        return this.filter;
    }

    @Override
    protected double getPowerDraw() {
        return this.powerPerTick;
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot) {
        super.onChangeInventory(inv, slot);
        if (inv.equals(this.invWrapper.getInput())) {
            this.resetOperation();
        }
    }

    private Optional<SpatialRiftCollapserRecipe> getRecipeForInput(final Container c) {
        return level.getRecipeManager().getRecipeFor(
                ImpenRegistry.SPATIAL_RIFT_COLLAPSER_RECIPE_TYPE.get(), c, this.level);
    }
}
