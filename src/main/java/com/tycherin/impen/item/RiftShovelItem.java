package com.tycherin.impen.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class RiftShovelItem extends ShovelItem implements RiftEnchantItem {
    public RiftShovelItem(final Properties props) {
        super(RiftToolTier.INSTANCE, 1.5F, -3.0F, props);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final @Nullable Level level,
            final List<Component> tooltipComponents, final TooltipFlag isAdvanced) {
        RiftEnchantItem.super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return RiftEnchantItem.super.isFoil(stack);
    }
}
