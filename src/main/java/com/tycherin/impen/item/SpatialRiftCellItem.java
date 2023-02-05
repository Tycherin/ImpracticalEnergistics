package com.tycherin.impen.item;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.logic.rift.SpatialRiftCellDataManager;
import com.tycherin.impen.logic.rift.SpatialRiftCellDataManager.SpatialRiftCellData;

import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.AEBaseItem;
import appeng.items.storage.SpatialStorageCellItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

public class SpatialRiftCellItem extends AEBaseItem {

    private static final String TAG_PLOT_ID = "plot_id";
    private static final LazyOptional<Ingredient> INGREDIENT = LazyOptional.of(
            () -> Ingredient.of(
                    ImpenRegistry.SPATIAL_RIFT_CELL_2_ITEM,
                    ImpenRegistry.SPATIAL_RIFT_CELL_16_ITEM,
                    ImpenRegistry.SPATIAL_RIFT_CELL_128_ITEM));

    public static final Ingredient getIngredient() {
        return INGREDIENT.orElseThrow(() -> new RuntimeException("Problem creating rift cell ingredient"));
    }

    public static final SpatialRiftCellItem getMatchingCell(final SpatialStorageCellItem spatialCell) {
        final Item item = (switch (spatialCell.getMaxStoredDim(null)) {
        case 2 -> ImpenRegistry.SPATIAL_RIFT_CELL_2_ITEM;
        case 16 -> ImpenRegistry.SPATIAL_RIFT_CELL_16_ITEM;
        case 128 -> ImpenRegistry.SPATIAL_RIFT_CELL_128_ITEM;
        default -> throw new RuntimeException("Unrecognized cell size for " + spatialCell);
        }).asItem();
        // Theoretically I should change ItemDefinition to track the actual item type to avoid the cast here, but ehhh
        return (SpatialRiftCellItem)item;
    }

    private final ItemLike originalItem;

    public SpatialRiftCellItem(final Item.Properties props, final ItemLike originalItem) {
        super(props);
        this.originalItem = originalItem;
    }

    public ItemLike getOriginalItem() {
        return originalItem;
    }

    public void setPlotId(final ItemStack is, final int plotId) {
        final CompoundTag c = is.getOrCreateTag();
        c.putInt(TAG_PLOT_ID, plotId);
    }

    public int getPlotId(final ItemStack is) {
        final CompoundTag c = is.getTag();
        if (c != null && c.contains(TAG_PLOT_ID)) {
            return c.getInt(TAG_PLOT_ID);
        }
        else {
            return -1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, final Level level, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        int plotId = this.getPlotId(stack);
        if (plotId == -1) {
            lines.add(Tooltips.of(GuiText.Unformatted).withStyle(ChatFormatting.ITALIC));
            final int maxDim = ((SpatialStorageCellItem)(this.originalItem.asItem())).getMaxStoredDim(null);
            lines.add(Tooltips.of(GuiText.SpatialCapacity, maxDim, maxDim, maxDim));
            return;
        }
        else {
            // AE2 generates this directly within the tooltip code, so we have to imitate it here
            final String serialNumber = String.format(Locale.ROOT, "SP-%04d", plotId);
            // TODO Localization goes here
            lines.add(Tooltips.of("Attuned to: " + serialNumber));

            final SpatialRiftCellData data = SpatialRiftCellDataManager.INSTANCE.getDataForPlot(plotId).get();
            final BlockPos size = data.getPlot().getSize();
            lines.add(Tooltips.of(GuiText.StoredSize, size.getX(), size.getY(), size.getZ()));

            final String desc = String.format("%d of %d inputs added (%d%% precision)",
                    data.getUsedSlots(), data.getMaxSlots(), data.getPrecision(level));
            lines.add(Tooltips.of(desc));
            
            if (!data.getInputs().isEmpty()) {
                final String inputDesc = data.getInputs().stream()
                        .map(block -> block.asItem().getDefaultInstance().getHoverName().getString())
                        .collect(Collectors.joining(", "));
                lines.add(Tooltips.of("Targeting: " + inputDesc));
            }
        }
    }
}
