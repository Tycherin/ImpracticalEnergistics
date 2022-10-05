package com.tycherin.impen.blockentity;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.logic.ism.IsmWeight;
import com.tycherin.impen.logic.ism.IsmWeightProvider;

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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ImaginarySpaceStabilizerBlockEntity extends AENetworkInvBlockEntity implements IsmWeightProvider {

    private static final Logger LOGGER = LogUtils.getLogger();
    
    private static final Map<Item, Collection<IsmWeight>> CATALYSTS;
    static {
        // TODO Move this to a recipe or something
        CATALYSTS = ImmutableMap.<Item, Collection<IsmWeight>>builder()
                .put(Items.APPLE, Lists.newArrayList(new IsmWeight(Blocks.ACACIA_WOOD, 300), new IsmWeight(Blocks.ACACIA_LOG, 100)))
                .put(Items.IRON_INGOT, Lists.newArrayList(new IsmWeight(Blocks.IRON_ORE, 100)))
                .build();
    }
    
    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new InventoryItemFilter());
    
    private Optional<Collection<IsmWeight>> weightsOpt = Optional.empty();
    private String id; // See {@link getId} for an explanation of why we lazy load this
    private boolean weightsHaveChanged = false;
    
    public ImaginarySpaceStabilizerBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpracticalEnergisticsMod.IMAGINARY_SPACE_STABILIZER_BE.get(), pos, blockState);

        this.getMainNode()
                .setFlags();
    }
    
    public void updateWeights() {
        if (this.isClientSide()) {
            return;
        }
        
        final var oldWeightsOpt = this.weightsOpt;
        
        final ItemStack input = inv.getStackInSlot(0);
        
        if (!input.isEmpty()) {
            this.weightsOpt = Optional.of(CATALYSTS.get(input.getItem()));
        }
        else {
            this.weightsOpt = Optional.empty();
        }
        
        if (!oldWeightsOpt.equals(this.weightsOpt)) {
            this.weightsHaveChanged = true;
        }
    }
    
    public boolean isCatalyst(final ItemStack is) {
        if (is.isEmpty()) {
            return false;
        }
        else {
            return CATALYSTS.containsKey(is.getItem());
        }
    }
    
    private class InventoryItemFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(final InternalInventory inv, final int slot, final int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(final InternalInventory inv, final int slot, final ItemStack stack) {
            return ImaginarySpaceStabilizerBlockEntity.this.isCatalyst(stack);
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
        
        this.updateWeights();

        return ret;
    }
    
    @Override
    public void onMainNodeStateChanged(final IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.updateWeights();
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
        this.updateWeights();
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
    public Collection<IsmWeight> getWeights() {
        return this.weightsOpt.isPresent() ? this.weightsOpt.get() : Collections.emptyList();
    }

    @Override
    public boolean hasUpdate() {
        return this.weightsHaveChanged;
    }

    @Override
    public void markUpdateSuccessful() {
        this.weightsHaveChanged = false;
    }
}
