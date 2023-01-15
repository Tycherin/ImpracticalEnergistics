package com.tycherin.impen.blockentity.rift;

import java.util.Optional;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.MachineBlockEntity;
import com.tycherin.impen.item.RiftedSpatialCellItem;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipe.SpatialStorageRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorRecipeManager;
import com.tycherin.impen.util.FilteredInventoryWrapper;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class SpatialRiftManipulatorBlockEntity extends MachineBlockEntity {

    private static final int DEFAULT_SPEED_TICKS = 20 * 1; // 1s

    private final FilteredInventoryWrapper invWrapper = new FilteredInventoryWrapper(this, new InventoryItemFilter());
    private final MachineOperation op;

    public SpatialRiftManipulatorBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR, pos, blockState);
        this.op = new MachineOperation(
                DEFAULT_SPEED_TICKS,
                this::enableOperation,
                this::doOperation);
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
        return !this.invWrapper.getInput().isEmpty() && this.invWrapper.getOutput().isEmpty();
    }

    protected boolean doOperation() {
        final ItemStack riftCellIs = this.invWrapper.getInput().getStackInSlot(0);

        // TODO This should actually be using a whole recipe system & pulling from another input slot... which this
        // should also have

        final SpatialRiftManipulatorRecipe recipe = SpatialRiftManipulatorRecipeManager
                .getRecipe(getLevel(), riftCellIs, Items.IRON_ORE.asItem().getDefaultInstance()).get();

        final boolean didUpdate;
        final ItemStack output;
        if (recipe instanceof SpatialStorageRecipe storageRecipe) {
            didUpdate = ((RiftedSpatialCellItem)riftCellIs.getItem()).addRecipe(riftCellIs, storageRecipe);
            output = riftCellIs;
        }
        else {
            didUpdate = true;
            output = recipe.getOutput();
        }

        if (didUpdate) {
            this.invWrapper.getInput().setItemDirect(0, ItemStack.EMPTY);
            this.invWrapper.getOutput().setItemDirect(0, output);
        }
        else {
            // At the moment this does the same thing, but that will change once the happy path consumes items
            this.invWrapper.getInput().setItemDirect(0, ItemStack.EMPTY);
            this.invWrapper.getOutput().setItemDirect(0, output);
        }

        // Note that even if we fail to add the ingredient, we still mark the operation as successful. That's because if
        // we left it unsuccessful, it would keep retrying it, which is pointless because the operation won't succeed
        // without modification.
        return true;
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(final Direction side) {
        return this.invWrapper.getExternal();
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.invWrapper.getInternal();
    }

    // TODO Ideally this shouldn't hardcode slot numbers & should integrate with FilteredInventoryWrapper instead...
    // somehow
    private static class InventoryItemFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(final InternalInventory inv, final int slot, final int amount) {
            return slot == 1;
        }

        @Override
        public boolean allowInsert(final InternalInventory inv, final int slot, final ItemStack stack) {
            return slot == 0 && stack.getItem().equals(ImpenRegistry.RIFTED_SPATIAL_CELL_ITEM.asItem());
        }
    }

    @Override
    protected int progressOperation() {
        return 1;
    }
}
