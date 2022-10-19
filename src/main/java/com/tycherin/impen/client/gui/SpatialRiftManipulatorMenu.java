package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.SpatialRiftManipulatorBlockEntity;

import appeng.api.config.SecurityPermissions;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class SpatialRiftManipulatorMenu extends UpgradeableMenu<SpatialRiftManipulatorBlockEntity>
        implements IProgressProvider {

    public static final MenuType<SpatialRiftManipulatorMenu> TYPE = MenuTypeBuilder
            .create(SpatialRiftManipulatorMenu::new, SpatialRiftManipulatorBlockEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("imaginaryspacemanipulator");

    @GuiSync(10)
    public long currentPower;
    @GuiSync(11)
    public long maxPower;
    @GuiSync(12)
    public long reqPower;
    @GuiSync(14)
    public int currentProgress;
    @GuiSync(15)
    public int maxProgress;
    @GuiSync(16)
    public String effectName;
    @GuiSync(17)
    public int statusCode;

    public SpatialRiftManipulatorMenu(final int id, final Inventory playerInv,
            final SpatialRiftManipulatorBlockEntity ism) {
        super(TYPE, id, playerInv, ism);

        // TODO Better decoupling for inventory management slots & things
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.SPATIAL_STORAGE_CELLS,
                ism.getInternalInventory(), 0), SlotSemantics.MACHINE_INPUT);
        this.addSlot(new OutputSlot(ism.getInternalInventory(), 1,
                RestrictedInputSlot.PlacableItemType.SPATIAL_STORAGE_CELLS.icon), SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (this.isServerSide()) {
            var be = (SpatialRiftManipulatorBlockEntity) this.getBlockEntity();
            this.setRequiredPower(Math.round(100.0 * be.getPowerDraw()));
            this.setCurrentProgress(Math.max(0, be.getCurrentProgress()));
            this.setMaxProgress(Math.max(1, be.getMaxProgress()));
            this.setEffectName("default"); // TODO Pull this from the BE once that's implemented

            this.statusCode = be.getStatusCode();
        }

        super.broadcastChanges();
    }

    public long getCurrentPower() {
        return this.currentPower;
    }

    public long getMaxPower() {
        return this.maxPower;
    }

    public long getRequiredPower() {
        return this.reqPower;
    }

    private void setRequiredPower(long reqPower) {
        this.reqPower = reqPower;
    }

    public double getCurrentProgressPercent() {
        return ((double) this.getCurrentProgress()) / this.getMaxProgress();
    }

    @Override
    public int getCurrentProgress() {
        return this.currentProgress;
    }

    @Override
    public int getMaxProgress() {
        return this.maxProgress;
    }

    private void setCurrentProgress(final int i) {
        this.currentProgress = i;
    }

    private void setMaxProgress(final int i) {
        this.maxProgress = i;
    }

    public String getEffectName() {
        return effectName;
    }

    private void setEffectName(final String s) {
        this.effectName = s;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
