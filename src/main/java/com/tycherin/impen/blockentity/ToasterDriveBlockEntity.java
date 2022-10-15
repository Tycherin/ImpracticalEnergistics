package com.tycherin.impen.blockentity;

import java.util.Random;

import com.tycherin.impen.ImpracticalEnergisticsMod;

import appeng.api.storage.cells.CellState;
import appeng.blockentity.storage.ChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ToasterDriveBlockEntity extends ChestBlockEntity {

    private static final Random RAND = new Random();

    public ToasterDriveBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(ImpracticalEnergisticsMod.TOASTER_DRIVE_BE.get(), pos, blockState);
    }

    @Override
    public void serverTick() {
        super.serverTick();

        if (this.getCellStatus(0).equals(CellState.TYPES_FULL) || this.getCellStatus(0).equals(CellState.FULL)) {
            this.ejectCell();
        }
    }

    private void ejectCell() {
        final ItemStack cell = this.getCell();

        // If there's a block above this one that has an inventory, put the cell into that
        boolean didInsert = false;
        final BlockEntity aboveNeighbor = this.level.getBlockEntity(this.getBlockPos().relative(this.getUp()));
        if (aboveNeighbor != null) {
            final var invCapOpt = aboveNeighbor.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    this.getUp().getOpposite());
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
            final double x = this.getBlockPos().getX() + .5;
            final double y = this.getBlockPos().getY() + 1;
            final double z = this.getBlockPos().getZ() + .5;
            final double xSpeed = (RAND.nextDouble() * .1); // [0, 0.1)
            final double ySpeed = (RAND.nextDouble() * .1) + .4; // [0.4, .5)
            final double zSpeed = (RAND.nextDouble() * .1); // [0, 0.1)

            final ItemEntity itemEntity = new ItemEntity(level, x, y, z, cell);
            itemEntity.setDeltaMovement(xSpeed, ySpeed, zSpeed);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }

        // And finally remove the existing cell
        this.setCell(ItemStack.EMPTY);
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return ImpracticalEnergisticsMod.TOASTER_DRIVE_ITEM.get();
    }
}
