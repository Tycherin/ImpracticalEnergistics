package com.tycherin.impen.blockentity;

import java.util.EnumSet;

import com.tycherin.impen.ImpenRegistry;

import appeng.api.inventories.InternalInventory;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.blockentity.storage.ChestBlockEntity;
import appeng.me.cells.BasicCellInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class EjectionDriveBlockEntity extends ChestBlockEntity {

    // Note that sleep ticks are a convenience thing to reduce the frequency of semi-expensive calculations, so they
    // aren't persisted
    private int sleepTicks = 0;

    public EjectionDriveBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.EJECTION_DRIVE.blockEntity(), pos, blockState);
    }

    @Override
    public void serverTick() {
        super.serverTick();

        this.sleepTicks--;

        if (this.sleepTicks <= 0) {
            if (this.getCellStatus(0).equals(CellState.ABSENT)) {
                this.sleepTicks = 100;
            }

            final double fillPercent = this.getFillPercent();
            final double ejectThreshold = this.getEjectThreshold();
            if (fillPercent >= ejectThreshold) {
                this.ejectCell();
                this.sleepTicks = 20;
            }
            else {
                // Modulate how fast we tick based on how close we are to the threshold
                final double diff = ejectThreshold - fillPercent;
                if (diff > 0.1) {
                    this.sleepTicks = 100;
                }
                else if (diff > 0.05) {
                    this.sleepTicks = 20;
                }
                else if (diff > 0.01) {
                    this.sleepTicks = 5;
                }
                else {
                    this.sleepTicks = 1;
                }
            }
        }

    }

    private double getFillPercent() {
        final CellState status = this.getCellStatus(0);
        if (status.equals(CellState.TYPES_FULL) || status.equals(CellState.FULL)) {
            return 1.0;
        }
        else if (status.equals(CellState.ABSENT)) {
            return 0.0;
        }
        else {
            final var storage = StorageCells.getCellInventory(this.getCell(), null);
            if (storage instanceof BasicCellInventory cellInv) {
                return (double)cellInv.getUsedBytes() / cellInv.getTotalBytes();
            }
            else {
                return 0.0;
            }
        }
    }

    private double getEjectThreshold() {
        // Redstone states:
        // 0 (no signal) -> eject at 100% full
        // 7 (half signal) -> eject at 50% full or more
        // 15 (full signal) -> eject at 0% full or more
        return (15 - this.level.getBestNeighborSignal(this.worldPosition)) / 15.0;
    }

    @Override
    public void onReady() {
        super.onReady();
        this.setOrientationFromState();
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        this.setOrientationFromState();
    }

    private void setOrientationFromState() {
        final var exposedSides = EnumSet.complementOf(EnumSet.of(this.getForward(), this.getForward().getOpposite()));
        this.getMainNode().setExposedOnSides(exposedSides);
        this.setPowerSides(exposedSides);
    }

    @Override
    public InternalInventory getExposedInventoryForSide(final Direction side) {
        // Some shenanigans here to trick the ChestBlockEntity into returning the right inventory based on our state

        if (side == this.getForward()) {
            // Forward face: output only, no exposed inventory
            return InternalInventory.empty();
        }
        else if (side == this.getForward().getOpposite()) {
            // Back face: you can put cells in here
            return super.getExposedInventoryForSide(this.getForward());
        }
        else {
            // Any other face: exposes the inventory of the stored cell
            return super.getExposedInventoryForSide(this.getForward().getOpposite());
        }
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot) {
        super.onChangeInventory(inv, slot);
        this.sleepTicks = 0;
    }

    private void ejectCell() {
        final ItemStack cell = this.getCell();

        // If there's a block next to this one that has an inventory, put the cell into that
        boolean didInsert = false;
        final BlockEntity neighbor = this.level.getBlockEntity(this.getBlockPos().relative(this.getForward()));
        if (neighbor != null) {
            final var invCapOpt = neighbor.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    this.getForward().getOpposite());
            if (invCapOpt.isPresent()) {
                final IItemHandler itemHandler = invCapOpt.resolve().get();
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    if (itemHandler.isItemValid(0, cell)) {
                        final ItemStack result = itemHandler.insertItem(i, cell, false);
                        if (result.isEmpty()) {
                            didInsert = true;
                            break;
                        }
                    }
                }
            }
        }

        // Otherwise, just spawn the item in the world
        if (!didInsert) {
            double xPos = this.getBlockPos().getX() + .5;
            double yPos = this.getBlockPos().getY() + .5;
            double zPos = this.getBlockPos().getZ() + .5;
            double xSpeed = 0;
            double ySpeed = 0;
            double zSpeed = 0;

            // I'm sure there's a more elegant way to do this somehow
            switch (this.getForward()) {
            case DOWN:
                yPos = yPos - 1;
                ySpeed = -1;
                break;
            case EAST:
                xPos = xPos + 1;
                xSpeed = 1;
                break;
            case NORTH:
                zPos = zPos - 1;
                zSpeed = -1;
                break;
            case SOUTH:
                zPos = zPos + 1;
                zSpeed = 1;
                break;
            case UP:
                yPos = yPos + 1;
                ySpeed = 1;
                break;
            case WEST:
                xPos = xPos - 1;
                xSpeed = -1;
                break;
            default:
                throw new RuntimeException("Unhandled direction " + this.getForward());
            }

            final ItemEntity itemEntity = new ItemEntity(level, xPos, yPos, zPos, cell);
            itemEntity.setDeltaMovement(xSpeed, ySpeed, zSpeed);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }

        // And finally remove the existing cell
        this.setCell(ItemStack.EMPTY);
    }
}
