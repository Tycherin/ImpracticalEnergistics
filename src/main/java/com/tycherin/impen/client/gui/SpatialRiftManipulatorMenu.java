package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.rift.SpatialRiftManipulatorBlockEntity;
import com.tycherin.impen.recipe.SpatialRiftManipulatorBaseBlockRecipe;
import com.tycherin.impen.recipe.SpatialRiftManipulatorBlockWeightRecipe;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.InaccessibleSlot;
import appeng.util.inv.AppEngInternalInventory;
import lombok.Getter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class SpatialRiftManipulatorMenu extends AEBaseMenu implements IProgressProvider {

    public static final MenuType<SpatialRiftManipulatorMenu> TYPE = MenuTypeBuilder
            .create(SpatialRiftManipulatorMenu::new, SpatialRiftManipulatorBlockEntity.class)
            .build("spatial_rift_manipulator");

    private final InaccessibleSlot blockSlot;

    @GuiSync(10)
    @Getter
    public int currentProgress;
    @GuiSync(11)
    @Getter
    public int maxProgress;

    public SpatialRiftManipulatorMenu(final int id, final Inventory playerInv,
            final SpatialRiftManipulatorBlockEntity be) {
        super(TYPE, id, playerInv, be);

        this.addSlot(new FilteredInputSlot(be.getInventoryFilter(), be.getInternalInventory(), 0),
                SlotSemantics.MACHINE_INPUT);
        this.addSlot(new FilteredInputSlot(be.getInventoryFilter(), be.getInternalInventory(), 1),
                SlotSemantics.INSCRIBER_PLATE_BOTTOM); // Sigh
        this.addSlot(new FilteredInputSlot(be.getInventoryFilter(), be.getInternalInventory(), 2),
                SlotSemantics.MACHINE_OUTPUT);
        this.blockSlot = new InaccessibleSlot(new AppEngInternalInventory(1), 0);
        this.blockSlot.setHideAmount(true);
        this.addSlot(blockSlot, ImpenSlotSemantics.SRM_BLOCK);

        this.maxProgress = be.getMaxProgress();

        this.createPlayerInventorySlots(playerInv);
    }

    @Override
    public void broadcastChanges() {
        this.currentProgress = ((SpatialRiftManipulatorBlockEntity)this.getBlockEntity()).getProgress();
        final var recipeOpt = ((SpatialRiftManipulatorBlockEntity)this.getBlockEntity()).getRecipeOpt();
        if (recipeOpt.isEmpty()) {
            this.blockSlot.set(ItemStack.EMPTY);
        }
        else {
            if (recipeOpt.get() instanceof SpatialRiftManipulatorBaseBlockRecipe baseBlockRecipe) {
                this.blockSlot.set(baseBlockRecipe.getBaseBlock().asItem().getDefaultInstance());
            }
            else if (recipeOpt.get() instanceof SpatialRiftManipulatorBlockWeightRecipe blockWeightRecipe) {
                this.blockSlot.set(blockWeightRecipe.getBlock().asItem().getDefaultInstance());
            }
            else {
                // Other recipe types don't display a block
                this.blockSlot.set(ItemStack.EMPTY);
            }
        }
        
        super.broadcastChanges();
    }
}
