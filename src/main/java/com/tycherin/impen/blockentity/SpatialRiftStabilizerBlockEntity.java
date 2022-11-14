package com.tycherin.impen.blockentity;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.logic.rift.RiftCatalystRecipeSource;
import com.tycherin.impen.recipe.RiftCatalystRecipe;
import com.tycherin.impen.recipe.RiftCatalystRecipeManager;
import com.tycherin.impen.util.ImpenIdUtil;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

public class SpatialRiftStabilizerBlockEntity extends AENetworkInvBlockEntity implements RiftCatalystRecipeSource {

    private final AppEngInternalInventory catalystInv = new AppEngInternalInventory(this, 1);
    private final InternalInventory catalystInvFiltered = new FilteredInternalInventory(this.catalystInv,
            new CatalystItemFilter());
    // TODO It would be nice to filter input ingredients based on the catalyst & gray out the UI elements until a
    // catalyst is inserted
    private final AppEngInternalInventory ingredientInv = new AppEngInternalInventory(this, 4);
    private final InternalInventory combinedInv = new CombinedInternalInventory(catalystInv, ingredientInv);
    
    private String id; // See getId() for an explanation of why we lazy load this
    
    public SpatialRiftStabilizerBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.SPATIAL_RIFT_STABILIZER.blockEntity(), pos, blockState);

        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL);
    }
    
    private class CatalystItemFilter implements IAEItemFilter {
        private final @NotNull ITag<Item> catalystTag;
        
        public CatalystItemFilter() {
            catalystTag = ForgeRegistries.ITEMS.tags().getTag(ImpenIdUtil.getItemTag("rift_catalyst"));
        }
        
        @Override
        public boolean allowInsert(final InternalInventory inv, final int slot, final ItemStack stack) {
            // TODO Add this tag filtering stuff
            //return catalystTag.contains(stack.getItem());
            return true;
        }
    }
    
    @Override
    protected void writeToStream(final FriendlyByteBuf data) {
        super.writeToStream(data);

        for (int i = 0; i < this.combinedInv.size(); i++) {
            data.writeItem(combinedInv.getStackInSlot(i));
        }
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);

        for (int i = 0; i < this.combinedInv.size(); i++) {
            this.combinedInv.setItemDirect(i, data.readItem());
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
        if (side == Direction.UP || side == Direction.DOWN) {
            return catalystInvFiltered;
        }
        else {
            return ingredientInv;
        }
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.combinedInv;
    }

    public InternalInventory getInputInventory() {
        return this.ingredientInv;
    }

    public InternalInventory getCatalystInventory() {
        return this.catalystInvFiltered;
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
    public Optional<RiftCatalystRecipe> getRecipe() {
        if (!this.getMainNode().isActive()) {
            return Optional.empty();
        }
        
        return RiftCatalystRecipeManager.getRecipe(getLevel(), this.catalystInv.getStackInSlot(0),
                this.ingredientInv.toContainer());
    }

    @Override
    public Optional<RiftCatalystRecipe> consumeRecipe() {
        if (!this.getMainNode().isActive()) {
            return Optional.empty();
        }

        final Optional<RiftCatalystRecipe> recipeOpt = RiftCatalystRecipeManager.getRecipe(getLevel(),
                this.catalystInv.getStackInSlot(0), this.ingredientInv.toContainer());

        if (recipeOpt.isPresent()) {
            recipeOpt.get().getConsumedItems().forEach(ingredient -> {
                for (final var is : this.ingredientInv) {
                    if (ingredient.test(is)) {
                        is.setCount(is.getCount() - 1);
                        return;
                    }
                }
                throw new RuntimeException("Expected to find ingredient " + ingredient + ", but was missing");
            });
        }

        return recipeOpt;
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return ImpenRegistry.SPATIAL_RIFT_STABILIZER.item();
    }
}
