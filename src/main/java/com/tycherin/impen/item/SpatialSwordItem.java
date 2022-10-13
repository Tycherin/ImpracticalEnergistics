package com.tycherin.impen.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class SpatialSwordItem extends SwordItem implements SpatialEnchantItem {
    public SpatialSwordItem(final Properties props) {
        super(SpatialToolTier.INSTANCE, 3, -2.4F, props);
    }
    
    @Override
    public void appendHoverText(final ItemStack stack, final @Nullable Level level,
            final List<Component> tooltipComponents, final TooltipFlag isAdvanced) {
        SpatialEnchantItem.super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return SpatialEnchantItem.super.isFoil(stack);
    }
}
