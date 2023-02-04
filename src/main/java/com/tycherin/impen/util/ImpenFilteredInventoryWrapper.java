package com.tycherin.impen.util;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import lombok.Getter;

@Getter
public class ImpenFilteredInventoryWrapper {

    private final InternalInventory internal;
    private final InternalInventory input;
    private final InternalInventory output;
    private final InternalInventory external;

    public ImpenFilteredInventoryWrapper(final InternalInventoryHost host, final IAEItemFilter externalFilter) {
        this(host, externalFilter, 1, 1);
    }

    public ImpenFilteredInventoryWrapper(final InternalInventoryHost host, final IAEItemFilter externalFilter,
            final int inputSize, final int outputSize) {
        this.input = new AppEngInternalInventory(host, inputSize, 1);
        this.output = new AppEngInternalInventory(host, outputSize, 64);
        this.internal = new CombinedInternalInventory(this.input, this.output);
        this.external = new FilteredInternalInventory(internal, externalFilter);
    }
}
