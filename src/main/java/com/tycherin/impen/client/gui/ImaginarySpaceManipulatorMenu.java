package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.ImaginarySpaceManipulatorBlockEntity;

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

public class ImaginarySpaceManipulatorMenu extends UpgradeableMenu<ImaginarySpaceManipulatorBlockEntity>
        implements IProgressProvider {
    
    public static final MenuType<ImaginarySpaceManipulatorMenu> TYPE = MenuTypeBuilder
            .create(ImaginarySpaceManipulatorMenu::new, ImaginarySpaceManipulatorBlockEntity.class)
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
    
    private int delay = 40;

    public ImaginarySpaceManipulatorMenu(final int id, final Inventory playerInv, final ImaginarySpaceManipulatorBlockEntity ism) {
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
            this.delay++;

            var be = (ImaginarySpaceManipulatorBlockEntity) this.getBlockEntity();
            var gridNode = be.getGridNode();
            var grid = gridNode != null ? gridNode.getGrid() : null;

            if (grid != null && ((this.delay > 15)) || (this.statusCode != be.getStatusCode())) {
                this.delay = 0;

                var eg = grid.getEnergyService();
                this.setCurrentPower((long) (100.0 * eg.getStoredPower()));
                this.setMaxPower((long) (100.0 * eg.getMaxStoredPower()));
                
                this.setRequiredPower(Math.round(100.0 * be.getPowerDraw()));
                
                this.setCurrentProgress(Math.max(0, be.getCurrentProgress()));
                this.setMaxProgress(Math.max(1, be.getMaxProgress()));
                this.setEffectName("default"); // TODO Pull this from the BE once that's implemented
                
                this.statusCode = be.getStatusCode();
            }
        }

        super.broadcastChanges();
    }

    public long getCurrentPower() {
        return this.currentPower;
    }

    private void setCurrentPower(long currentPower) {
        this.currentPower = currentPower;
    }

    public long getMaxPower() {
        return this.maxPower;
    }

    private void setMaxPower(long maxPower) {
        this.maxPower = maxPower;
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
