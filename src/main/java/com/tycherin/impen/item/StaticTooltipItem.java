package com.tycherin.impen.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Item variant that displays a static tooltip in the hover text
 */
public class StaticTooltipItem extends Item {

    private static final String TOOLTIP_PREFIX = "gui.impracticalenergistics.tooltip.";
    
    public StaticTooltipItem(final Properties props) {
        super(props);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, final Level level, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        lines.add(new TranslatableComponent(TOOLTIP_PREFIX + stack.getItem().getRegistryName().getPath())
                .withStyle(ChatFormatting.GRAY));
    }
}
