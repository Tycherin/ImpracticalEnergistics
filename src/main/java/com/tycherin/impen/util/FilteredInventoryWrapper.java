package com.tycherin.impen.util;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;

public class FilteredInventoryWrapper {

    private final AppEngInternalInventory internalInv;
    private final InternalInventory inputInv;
    private final InternalInventory outputInv;
    private final InternalInventory invExt;

    public FilteredInventoryWrapper(final InternalInventoryHost host, final IAEItemFilter externalFilter) {
        this(host, externalFilter, 1, 1);
    }

    public FilteredInventoryWrapper(final InternalInventoryHost host, final IAEItemFilter externalFilter,
            final int inputSize, final int outputSize) {
        final int totalSize = inputSize + outputSize;
        this.internalInv = new AppEngInternalInventory(host, totalSize);
        this.inputInv = this.internalInv.getSubInventory(0, inputSize);
        this.outputInv = this.internalInv.getSubInventory(inputSize, totalSize);
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
