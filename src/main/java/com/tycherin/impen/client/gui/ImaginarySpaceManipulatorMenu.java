package com.tycherin.impen.client.gui;

import com.tycherin.impen.blockentity.ImaginarySpaceManipulatorBlockEntity;

import appeng.api.config.SecurityPermissions;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class ImaginarySpaceManipulatorMenu extends AEBaseMenu implements IProgressProvider {
    
    public static final MenuType<ImaginarySpaceManipulatorMenu> TYPE = MenuTypeBuilder
            .create(ImaginarySpaceManipulatorMenu::new, ImaginarySpaceManipulatorBlockEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("imaginaryspacemanipulator");

    @GuiSync(0)
    public long currentPower;
    @GuiSync(1)
    public long maxPower;
    @GuiSync(2)
    public long reqPower;
    @GuiSync(4)
    public int currentProgress;
    @GuiSync(5)
    public int maxProgress;
    @GuiSync(6)
    public String effectName;
    
    private int delay = 40;

    @GuiSync(31)
    public int xSize;
    @GuiSync(32)
    public int ySize;
    @GuiSync(33)
    public int zSize;

    public ImaginarySpaceManipulatorMenu(final int id, final Inventory playerInv, final ImaginarySpaceManipulatorBlockEntity ism) {
        super(TYPE, id, playerInv, ism);

        // TODO Better decoupling for inventory management slots & things
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.SPATIAL_STORAGE_CELLS,
                ism.getInternalInventory(), 0), SlotSemantics.MACHINE_INPUT);
        this.addSlot(new OutputSlot(ism.getInternalInventory(), 2,
                RestrictedInputSlot.PlacableItemType.SPATIAL_STORAGE_CELLS.icon), SlotSemantics.MACHINE_OUTPUT);

        this.createPlayerInventorySlots(playerInv);
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (this.isServerSide()) {
            this.delay++;

            var be = (ImaginarySpaceManipulatorBlockEntity) this.getBlockEntity();
            var gridNode = be.getGridNode();
            var grid = gridNode != null ? gridNode.getGrid() : null;

            if (this.delay > 15 && grid != null) {
                this.delay = 0;

                var eg = grid.getEnergyService();
                this.setCurrentPower((long) (100.0 * eg.getStoredPower()));
                this.setMaxPower((long) (100.0 * eg.getMaxStoredPower()));
                
                var sc = grid.getSpatialService();
                this.setRequiredPower((long)(100.0 * 50)); // TODO Pull this from the BE once that's implemented
                
                this.setCurrentProgress(Math.max(0, be.getCurrentProgress()));
                this.setMaxProgress(Math.max(1, be.getMaxProgress()));
                this.setEffectName("default"); // TODO Pull this from the BE once that's implemented

                var min = sc.getMin();
                var max = sc.getMax();

                if (min != null && max != null && sc.isValidRegion()) {
                    this.xSize = sc.getMax().getX() - sc.getMin().getX() - 1;
                    this.ySize = sc.getMax().getY() - sc.getMin().getY() - 1;
                    this.zSize = sc.getMax().getZ() - sc.getMin().getZ() - 1;
                } else {
                    this.xSize = 0;
                    this.ySize = 0;
                    this.zSize = 0;
                }
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
}
