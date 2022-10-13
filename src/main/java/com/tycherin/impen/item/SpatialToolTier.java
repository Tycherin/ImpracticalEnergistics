package com.tycherin.impen.item;

import com.tycherin.impen.ImpracticalEnergisticsMod;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

public class SpatialToolTier implements Tier {

    public static final SpatialToolTier INSTANCE = new SpatialToolTier();

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
        return Ingredient.of(ImpracticalEnergisticsMod.FLUIX_CATALYST_ITEM.get());
    }

    @Override
    public String toString() {
        return ImpracticalEnergisticsMod.MOD_ID + ":spatial";
    }
}
