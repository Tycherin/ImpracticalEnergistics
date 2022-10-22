package com.tycherin.impen;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ImpenCreativeModeTab extends CreativeModeTab {

    public static final CreativeModeTab TAB = new ImpenCreativeModeTab();
    
    private ImpenCreativeModeTab() {
        super("impracticalenergistics.main");
    }

    @Override
    public ItemStack makeIcon() {
        return ImpenRegistry.RIFT_PRISM.asItem().getDefaultInstance();
    }
}
