package com.tycherin.impen.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import appeng.core.localization.GuiText;
import appeng.hooks.IntrinsicEnchantItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Common interface for adding enchantments to Spatial tools via the AE2 IntrinsicEnchantItem mixin
 * 
 * @author Tycherin
 *
 */
public interface RiftEnchantItem extends IntrinsicEnchantItem {
    default int getIntrinsicEnchantLevel(final ItemStack stack, final Enchantment enchantment) {
        if (enchantment == Enchantments.BLOCK_FORTUNE || enchantment == Enchantments.UNBREAKING) {
            return 1;
        }
        else {
            return 0;
        }
    }

    default void appendHoverText(final ItemStack stack, final @Nullable Level level, final List<Component> tooltipComponents,
            final TooltipFlag isAdvanced) {
        tooltipComponents.add(GuiText.IntrinsicEnchant.text(Enchantments.BLOCK_FORTUNE.getFullname(1)));
        tooltipComponents.add(GuiText.IntrinsicEnchant.text(Enchantments.UNBREAKING.getFullname(1)));
    }

    default boolean isFoil(final ItemStack stack) {
        return true;
    }
}
