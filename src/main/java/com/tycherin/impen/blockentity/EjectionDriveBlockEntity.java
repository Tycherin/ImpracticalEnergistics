package com.tycherin.impen.blockentity;

import java.util.EnumSet;

import com.tycherin.impen.ImpenRegistry;

import appeng.api.inventories.InternalInventory;
import appeng.api.storage.cells.CellState;
import appeng.blockentity.storage.ChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class EjectionDriveBlockEntity extends ChestBlockEntity {

    // TODO: Force eject if the block gets a redstone signal

    public EjectionDriveBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpenRegistry.EJECTION_DRIVE.blockEntity(), pos, blockState);
    }

    @Override
    public void serverTick() {
        super.serverTick();

        if (this.getCellStatus(0).equals(CellState.TYPES_FULL) || this.getCellStatus(0).equals(CellState.FULL)) {
            this.ejectCell();
        }
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

    @Override
    protected Item getItemFromBlockEntity() {
        return ImpenRegistry.EJECTION_DRIVE.item();
    }
}
