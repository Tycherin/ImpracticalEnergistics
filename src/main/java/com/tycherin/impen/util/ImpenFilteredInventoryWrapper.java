package com.tycherin.impen.util;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;

public class ImpenFilteredInventoryWrapper {

    private final InternalInventory internalInv;
    private final InternalInventory inputInv;
    private final InternalInventory outputInv;
    private final InternalInventory invExt;

    public ImpenFilteredInventoryWrapper(final InternalInventoryHost host, final IAEItemFilter externalFilter) {
        this(host, externalFilter, 1, 1);
    }

    public ImpenFilteredInventoryWrapper(final InternalInventoryHost host, final IAEItemFilter externalFilter,
            final int inputSize, final int outputSize) {
        this.inputInv = new AppEngInternalInventory(host, inputSize, 1);
        this.outputInv = new AppEngInternalInventory(host, outputSize, 64);
        this.internalInv = new CombinedInternalInventory(this.inputInv, this.outputInv);
        this.invExt = new FilteredInternalInventory(internalInv, externalFilter);
    }

    public InternalInventory getInput() {
        return inputInv;
    }

    public InternalInventory getOutput() {
        return outputInv;
    }

    public InternalInventory getExternal() {
        return invExt;
    }

    public InternalInventory getInternal() {
        return internalInv;
    }
}
