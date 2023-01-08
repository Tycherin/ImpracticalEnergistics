package com.tycherin.impen.blockentity;

import java.util.List;

import com.tycherin.impen.ImpenRegistry.MachineDefinition;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class UpgradableMachineBlockEntity extends MachineBlockEntity implements IUpgradeableObject {
    
    private final IUpgradeInventory upgrades;
    
    public UpgradableMachineBlockEntity(final MachineDefinition<? extends Block, ? extends BlockEntity> machineDefinition,
            final BlockPos blockPos, final BlockState blockState, final int upgradeSlots) {
        super(machineDefinition, blockPos, blockState);
        this.upgrades = UpgradeInventories.forMachine(machineDefinition.asItem(), upgradeSlots, this::saveChanges);
    }

    @Override
    public void saveAdditional(final CompoundTag data) {
        super.saveAdditional(data);
        this.upgrades.writeToNBT(data, "upgrades");
    }
    
    @Override
    public void loadTag(final CompoundTag data) {
        super.loadTag(data);
        this.upgrades.readFromNBT(data, "upgrades");
    }
    
    @Override
    public void addAdditionalDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        upgrades.forEach(drops::add);
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }
}
