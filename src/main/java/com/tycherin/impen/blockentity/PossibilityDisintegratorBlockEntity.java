package com.tycherin.impen.blockentity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.tycherin.impen.ImpracticalEnergisticsMod;

import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class PossibilityDisintegratorBlockEntity extends AENetworkBlockEntity implements ServerTickingBlockEntity {

    private static final Random RAND = new Random();
    private static final int CHECK_DELAY_TICKS = 1 * 20;
    private static final int DISINTEGRATION_DELAY_TICKS = 2 * 20;
    private static final DamageSource DAMAGE_SOURCE = new DamageSource("possibility_disintegrator");

    private final Map<Mob, Integer> targets = new HashMap<>();
    private int checkDelay = 0;

    public PossibilityDisintegratorBlockEntity(final BlockPos pos,
            final BlockState blockState) {
        super(ImpracticalEnergisticsMod.POSSIBILITY_DISINTEGRATOR_BE.get(), pos, blockState);
    }

    public void handleEntity(final Entity entity) {
        if (entity instanceof Mob mob && !targets.containsKey(mob)) {
            if (mob.isBaby() || mob instanceof WitherBoss || mob instanceof EnderDragon) {
                // TODO Forge 1.19 adds a tag to see if a mob is a boss or not, we should denylist those instead of
                // hardcoding vanilla mob classes
                return;
            }
            this.lockTarget(mob);
            targets.put(mob, 0);
        }
    }

    @Override
    public void serverTick() {
        this.checkDelay++;

        if (this.checkDelay >= CHECK_DELAY_TICKS) {
            // Use an iterator here to avoid concurrent modification problems from forEach()
            final Iterator<Entry<Mob, Integer>> iter = this.targets.entrySet().iterator();
            while (iter.hasNext()) {
                final var entry = iter.next();
                final Mob target = entry.getKey();
                // Check to see if a target has died (not via disintegrate()) or if it has moved off of this block
                // somehow, and if so, undo the lock
                if (!target.isAlive() || !target.getOnPos().equals(this.getBlockPos())) {
                    this.unlockTarget(target);
                    iter.remove();
                }
                else {
                    final int newVal = entry.getValue() + this.checkDelay;
                    if (newVal >= DISINTEGRATION_DELAY_TICKS) {
                        this.disintegrate(target);
                        if (target.isAlive()) {
                            entry.setValue(0);
                        }
                        else {
                            iter.remove();
                        }
                    }
                    else {
                        entry.setValue(newVal);
                    }
                }
            }

            this.checkDelay = 0;
        }
    }

    @Override
    public void setRemoved() {
        this.targets.keySet().forEach(this::unlockTarget);
        this.targets.clear();
        super.setRemoved();
    }

    private void lockTarget(final Mob target) {
        if (!target.isAlive()) {
            return;
        }

        target.setNoAi(true);
        target.moveTo(this.getBlockPos().above(), target.getYRot(), target.getXRot());
    }

    private void unlockTarget(final Mob target) {
        target.setNoAi(false);
    }

    private void disintegrate(final Mob target) {
        // TODO Scale damage based on mob health so that Ravagers don't take 200 years to kill
        target.hurt(DAMAGE_SOURCE, 1f);
        if (!target.isAlive()) {
            return;
        }
        
        // TODO Randomize drop rates
        // TODO Consume items from ME system as part of operation
        // TODO Power consumption as well

        // This code is mostly adapted from Entity code that isn't directly accessible
        //
        // Worth noting that this will only spawn items from the normal loot table, i.e. no player kill-only items and
        // no special drops (like blocks held by Enderman - no dupe glitches for you)
        final ResourceLocation resourcelocation = target.getLootTable();
        final LootTable loottable = this.level.getServer().getLootTables().get(resourcelocation);
        final LootContext ctx = new LootContext.Builder((ServerLevel) this.level)
                .withRandom(RAND)
                .withParameter(LootContextParams.THIS_ENTITY, target)
                .withParameter(LootContextParams.ORIGIN, target.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, DAMAGE_SOURCE)
                .create(LootContextParamSets.ENTITY);
        loottable.getRandomItems(ctx).forEach(target::spawnAtLocation);
    }
}
