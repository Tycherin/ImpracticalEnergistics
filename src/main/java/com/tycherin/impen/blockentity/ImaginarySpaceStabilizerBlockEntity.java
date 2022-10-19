package com.tycherin.impen.blockentity;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.logic.ism.IsmCatalystProvider;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNodeListener;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ImaginarySpaceStabilizerBlockEntity extends AENetworkInvBlockEntity implements IsmCatalystProvider {

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new InventoryItemFilter());
    
    private String id; // See getId() for an explanation of why we lazy load this
    
    public ImaginarySpaceStabilizerBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.IMAGINARY_SPACE_STABILIZER_BE.get(), pos, blockState);

        this.getMainNode()
                .setFlags();
    }
    
    private class InventoryItemFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(final InternalInventory inv, final int slot, final int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(final InternalInventory inv, final int slot, final ItemStack stack) {
            return ImaginarySpaceStabilizerBlockEntity.this.level.getRecipeManager()
                    .getRecipeFor(ImpenRegistry.ISM_CATALYST_RECIPE_TYPE.get(), inv.toContainer(), level)
                    .isPresent();
        }
    }
    
    @Override
    protected void writeToStream(final FriendlyByteBuf data) {
        super.writeToStream(data);

        for (int i = 0; i < this.inv.size(); i++) {
            data.writeItem(inv.getStackInSlot(i));
        }
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);

        for (int i = 0; i < this.inv.size(); i++) {
            this.inv.setItemDirect(i, data.readItem());
        }
        
        return ret;
    }
    
    @Override
    public void onMainNodeStateChanged(final IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.markForUpdate();
        }
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
    public void onChangeInventory(final InternalInventory inv, final int slot) {
    }

    @Override
    public String getId() {
        // Normally we'd build this string when the BE is constructed, but the level isn't available then, so we defer
        // it until the first invocation
        if (this.id == null) {
            this.id = this.level.dimension().location().toString() + "@" + this.getBlockPos().toShortString();
        }
        return this.id;
    }

    @Override
    public ItemStack getCatalyst() {
        return this.inv.getStackInSlot(0);
    }

    @Override
    public ItemStack consumeCatalyst(final int desiredAmount) {
        final ItemStack is = this.inv.getStackInSlot(0);
        
        if (desiredAmount >= is.getCount()) {
            this.inv.setItemDirect(0, ItemStack.EMPTY);
            return is;
        }
        else {
            is.setCount(is.getCount() - desiredAmount);
            final ItemStack returnedItems = is.copy();
            returnedItems.setCount(desiredAmount);
            return returnedItems;
        }
        // TODO What if the catalyst is something in a container, e.g. a bucket of water?
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return ImpenRegistry.IMAGINARY_SPACE_STABILIZER_ITEM.get();
    }
}
