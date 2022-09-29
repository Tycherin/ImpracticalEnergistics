package com.tycherin.impen.item;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.contents.CellConfig;
import appeng.items.storage.BasicStorageCell;
import appeng.me.cells.BasicCellInventory;
import appeng.util.ConfigInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Reference:
// BasicStorageCell

public class LunchboxCellItem extends Item implements IBasicCellItem {

    private static final Logger LOGGER = LogUtils.getLogger();
    
    public LunchboxCellItem() {
        super(new Item.Properties().tab(CreativeModeTab.TAB_MISC));
    }
    
    // TODO Change item model when food is available vs. not
    
    // TODO Display the name of what item is loaded in the tooltip
    
    // TODO Add small & large variants
    
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack lunchIs = player.getItemInHand(hand);
        final Optional<ItemStack> storedIs = this.getStoredItemStack(lunchIs);
        return storedIs.isPresent() ? storedIs.get().use(level, player, hand) : InteractionResultHolder.pass(lunchIs);
     }

     public ItemStack finishUsingItem(final ItemStack is, final Level level, final LivingEntity entity) {
         final StorageCell storCell = StorageCells.getCellInventory(is, null);
         final KeyCounter storedItems = storCell.getAvailableStacks();
         if (storedItems.getFirstEntry() != null) {
             // This will apply the effects to the player, but it won't actually mutate the contents of the cell
             storedItems.getFirstEntry().getKey().wrapForDisplayOrFilter().finishUsingItem(level, entity);
             // For that, we need this bit here
             storCell.extract(storedItems.getFirstKey(), 1, Actionable.MODULATE, null);
         }
         return is;
     }
    
    @Override
    public FoodProperties getFoodProperties(final ItemStack is, final LivingEntity entity) {
        final Optional<ItemStack> storedIs = this.getStoredItemStack(is);
        return storedIs.isPresent() ? storedIs.get().getFoodProperties(entity) : null;
    }
    
    @Override
    public UseAnim getUseAnimation(final ItemStack is) {
        final Optional<ItemStack> storedIs = this.getStoredItemStack(is);
        return storedIs.isPresent() ? storedIs.get().getUseAnimation() : UseAnim.NONE;
    }

    @Override
    public int getUseDuration(final ItemStack is) {
        final Optional<ItemStack> storedIs = this.getStoredItemStack(is);
        return storedIs.isPresent() ? storedIs.get().getUseDuration() : 0;
    }
    
    @Override
    public boolean isBlackListed(final ItemStack cellItem, final AEKey requestedAddition) {
        return !requestedAddition.wrapForDisplayOrFilter().isEdible();
    }
    
    private Optional<ItemStack> getStoredItemStack(final ItemStack lunchboxCellIs) {
        final KeyCounter storedItems = StorageCells.getCellInventory(lunchboxCellIs, null).getAvailableStacks();
        if (storedItems.isEmpty()) {
            return Optional.empty();
        }
        else {
            return Optional.of(storedItems.getFirstEntry().getKey().wrapForDisplayOrFilter());
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack is, final Level level, final List<Component> lines, final TooltipFlag advancedTooltips) {
        // TODO Localization goes here
        final KeyCounter storedItems = StorageCells.getCellInventory(is, null).getAvailableStacks();
        if (!storedItems.isEmpty()) {
            final AEKey whatItem = storedItems.getFirstKey();
            final long whatAmount = storedItems.getFirstEntry().getLongValue();
            lines.add(Tooltips.of(
                    Tooltips.of("Currently holding "),
                    Tooltips.of(whatItem.formatAmount(whatAmount, AmountFormat.FULL)),
                    Tooltips.of(" "),
                    Tooltips.of(whatItem.getDisplayName())
                    ));
        }
        else {
            lines.add(Tooltips.of("Currently holding nothing"));
        }
    }

    // IBasicCellItem shenanigans go here
    
    @Override
    public boolean isEditable(ItemStack is) {
        return false;
    }

    @Override
    public AEKeyType getKeyType() {
        return AEKeyType.items();
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return 1024;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 8;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 1;
    }

    @Override
    public double getIdleDrain() {
        return 1.0;
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 4);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(AEKeyType.items().filter(), is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = is.getOrCreateTag().getString("FuzzyMode");
        if (fz.isEmpty()) {
            return FuzzyMode.IGNORE_ALL;
        }
        try {
            return FuzzyMode.valueOf(fz);
        } catch (Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        is.getOrCreateTag().putString("FuzzyMode", fzMode.name());
    }
}
