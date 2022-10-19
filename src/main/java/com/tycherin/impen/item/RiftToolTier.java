package com.tycherin.impen.item;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

public class RiftToolTier implements Tier {

    public static final RiftToolTier INSTANCE = new RiftToolTier();

    @Override
    public int getUses() {
        return Tiers.DIAMOND.getUses() / 2;
    }

    @Override
    public float getSpeed() {
        return Tiers.DIAMOND.getSpeed() - 1f;
    }

    @Override
    public float getAttackDamageBonus() {
        return Tiers.DIAMOND.getAttackDamageBonus() - .5f;
    }

    @Override
    public int getLevel() {
        return Tiers.DIAMOND.getLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return 12;
    }

    @Override
    public Ingredient getRepairIngredient() {
        // TODO Replace with the appropriate material
        return Ingredient.of(ImpenRegistry.RIFT_PRISM_ITEM.get());
    }

    @Override
    public String toString() {
        return ImpracticalEnergisticsMod.MOD_ID + ":spatial";
    }
}
