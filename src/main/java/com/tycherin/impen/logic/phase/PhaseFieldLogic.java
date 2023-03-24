package com.tycherin.impen.logic.phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.PhaseFieldControllerBlockEntity;
import com.tycherin.impen.util.MobUtil;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.MachineSource;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.Lazy;

@Slf4j
public class PhaseFieldLogic {

    private static final DamageSource DAMAGE_SOURCE = new DamageSource("phase_field");
    private static final Random RAND = new Random();

    private static final double EGG_CHANCE_BASE = .5;
    private static final double LOOT_CHANCE_BASE = .1;

    private final PhaseFieldControllerBlockEntity be;
    private final MachineSource actionSource;
    // Lazy load these two since the level isn't available when the block entity is being instantiated
    private final Lazy<Player> fakePlayer;
    private final Lazy<DamageSource> playerDamageSource;
    /** Cache of AABBs to search for affected entities in */
    private Optional<List<AABB>> aabbCache = Optional.empty();
    private PhaseFieldOperation operation = new PhaseFieldOperation(Collections.emptyList(), new PhaseFieldEffect());
    private final Set<Mob> lockedMobs = new HashSet<>();

    public PhaseFieldLogic(final PhaseFieldControllerBlockEntity be) {
        this.be = be;
        this.actionSource = new MachineSource(be);
        this.fakePlayer = Lazy.of(() -> FakePlayerFactory.getMinecraft((ServerLevel)be.getLevel()));
        this.playerDamageSource = Lazy.of(() -> new EntityDamageSource("possibility_disintegrator", fakePlayer.get()));
    }

    public boolean doOperation() {
        if (this.aabbCache.isEmpty()) {
            this.recomputeAABBCache();
        }

        if (this.be.getGridNode() == null || this.be.getGridNode().getGrid() == null) {
            // Grid is in a weird state, and we shouldn't be ticking
            log.warn("Asked to tick PhaseFieldController at {}, but grid was not found", this.be.getBlockPos());
            return false;
        }

        // Extract configured capsule items from attached ME storage

        if (this.operation.inputs().isEmpty()) {
            // No inputs are configured; skip the operation
            return false;
        }

        if (!this.operation.effect().doDamage) {
            // This will need to be changed once we support non-damaging effects
            return false;
        }

        final MEStorage storage = this.be.getGridNode().getGrid().getStorageService().getInventory();
        final List<Item> extractedInputs = new ArrayList<>();
        for (final var configuredInput : this.operation.inputs()) {
            final long amountExtracted = storage.extract(AEItemKey.of(configuredInput), 1, Actionable.MODULATE,
                    this.actionSource);

            if (amountExtracted == 1) {
                extractedInputs.add(configuredInput);
            }
            else {
                // Found an ingredient that was missing. Return all extracted ingredients and cancel the operation.
                extractedInputs.forEach(extractedInput -> {
                    storage.insert(AEItemKey.of(extractedInput), 1, Actionable.MODULATE, this.actionSource);
                });
                return false;
            }
        }

        // Apply effects to mobs in range

        final List<Mob> affectedMobs = this.aabbCache.get().stream()
                .flatMap(aabb -> getLevel().getEntitiesOfClass(Mob.class, aabb).stream())
                .filter(MobUtil::canBeCaptured)
                .collect(Collectors.toList());

        if (affectedMobs.isEmpty()) {
            // No valid mobs were in range. Put the ingredients back and mark the operation as failed.
            extractedInputs.forEach(extractedInput -> {
                storage.insert(AEItemKey.of(extractedInput), 1, Actionable.MODULATE, this.actionSource);
            });
            return false;
        }

        affectedMobs.forEach(this::applyEffect);

        return true;
    }

    private void applyEffect(final Mob target) {
        if (this.operation.effect.doDamage) {
            this.checkLock(target, true);

            // Edge case: if the target is a slime (or something similar), it will spawn children on death, and those
            // children will inherit a bunch of flags, including the noAi one. So here, we need to undo that flag, then
            // reset it if the entity didn't die.
            target.setNoAi(false);

            // TODO Add VFX for locked mobs
        }
        else {
            this.checkLock(target, false);
        }

        // TODO Add scaling damage back
        // Scale damage based on how long the mob has been trapped. This is geometric scaling, so it will kill large
        // mobs in a reasonable amount of time without insta-gibbing small mobs.
        // Math.min() to avoid unbounded damage scaling shenanigans.
        final float damageToDeal = 1;// Math.min(20, stats.timesHurt + 2);

        if (this.operation.effect.doPlayerKill) {
            target.hurt(playerDamageSource.get(), damageToDeal);
        }
        else {
            target.hurt(DAMAGE_SOURCE, damageToDeal);
        }

        if (!target.isAlive()) {
            if (this.operation.effect.doEgg) {
                final var spawnEgg = ForgeSpawnEggItem.fromEntityType(target.getType());
                if (spawnEgg != null) {
                    final int luckModifier = 1 * this.operation.effect.luckLevel;
                    final boolean doSpawnEgg = this.rollRandom(EGG_CHANCE_BASE * luckModifier);
                    if (doSpawnEgg) {
                        target.spawnAtLocation(spawnEgg);
                    }
                }
            }
            this.lockedMobs.remove(target);
            return;
        }
        else {
            if (this.operation.effect.doDamage) {
                // Target is still alive, so re-lock it
                target.setNoAi(true);
            }
        }

        if (this.operation.effect.lootLevel > 0) {
            final int luckModifier = 1 * (2 * this.operation.effect.luckLevel);
            final boolean doSpawnLoot = this.rollRandom(LOOT_CHANCE_BASE * luckModifier);

            if (doSpawnLoot) {
                // This code is mostly adapted from LivingEntity code that isn't easily accessible
                //
                // Worth noting that this will only spawn items from the normal loot table, i.e. no special drops (like
                // blocks held by Enderman - no dupe glitches for you)
                final ResourceLocation resourcelocation = target.getLootTable();
                final LootTable loottable = getLevel().getServer().getLootTables().get(resourcelocation);
                final var ctxBuilder = new LootContext.Builder(getLevel())
                        .withRandom(RAND)
                        .withParameter(LootContextParams.THIS_ENTITY, target)
                        .withParameter(LootContextParams.ORIGIN, target.position())
                        .withParameter(LootContextParams.DAMAGE_SOURCE, DAMAGE_SOURCE);

                if (this.operation.effect.doPlayerKill) {
                    ctxBuilder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, fakePlayer.get())
                            .withParameter(LootContextParams.KILLER_ENTITY, fakePlayer.get())
                            .withParameter(LootContextParams.DIRECT_KILLER_ENTITY, fakePlayer.get());
                }

                final LootContext ctx = ctxBuilder.create(LootContextParamSets.ENTITY);
                loottable.getRandomItems(ctx).forEach(target::spawnAtLocation);
            }
        }
    }

    private void checkLock(final Mob target, final boolean shouldBeLocked) {
        if (this.lockedMobs.contains(target) && !shouldBeLocked) {
            this.lockMob(target);
        }
        else if (!this.lockedMobs.contains(target) && shouldBeLocked) {
            this.unlockMob(target);
        }
    }

    private void lockMob(final Mob target) {
        target.setNoGravity(true);
        target.setNoAi(true);
        target.setDeltaMovement(Vec3.ZERO);
        this.lockedMobs.add(target);
    }

    private void unlockMob(final Mob target) {
        target.setNoGravity(false);
        target.setNoAi(false);
        this.lockedMobs.remove(target);
    }

    private boolean rollRandom(final double chance) {
        return RAND.nextDouble() < chance;
    }

    private ServerLevel getLevel() {
        return (ServerLevel)this.be.getLevel();
    }

    public void recomputeAABBCache() {
        this.aabbCache = Optional.of(this.be.getEmitters().stream()
                .map(emitter -> new AABB(emitter.getBlockEntity().getBlockPos().relative(emitter.getSide())))
                .collect(Collectors.toList()));
    }

    public void recomputeOperation() {
        final List<Item> configuredInputs = StreamSupport.stream(this.be.getInternalInventory().spliterator(), false)
                .filter(Predicate.not(ItemStack::isEmpty))
                .map(ItemStack::getItem)
                .collect(Collectors.toList());

        boolean doDamage = false;
        int lootLevel = 0;
        int luckLevel = 0;
        boolean doEgg = false;
        boolean doPlayerKill = false;
        for (final var item : configuredInputs) {
            if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_LOOT.asItem())) {
                doDamage = true;
                lootLevel++;
            }
            else if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_EGG.asItem())) {
                doDamage = true;
                doEgg = true;
            }
            else if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_PLAYER_KILL.asItem())) {
                doDamage = true;
                doPlayerKill = true;
            }
            else if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_LUCK.asItem())) {
                luckLevel++;
            }
        }

        final PhaseFieldEffect effect = new PhaseFieldEffect(doDamage, lootLevel, luckLevel, doEgg, doPlayerKill);

        final var oldOperation = this.operation;
        this.operation = new PhaseFieldOperation(configuredInputs, effect);

        // If the new operation doesn't do damage but the old one did, release any locked mobs
        if (oldOperation.effect.doDamage && !this.operation.effect.doDamage) {
            this.lockedMobs.forEach(this::unlockMob);
        }
    }

    private static record PhaseFieldOperation(List<Item> inputs, PhaseFieldEffect effect) {
    }

    private static record PhaseFieldEffect(
            boolean doDamage,
            int lootLevel,
            int luckLevel,
            boolean doEgg,
            boolean doPlayerKill) {

        PhaseFieldEffect() {
            this(false, 0, 0, false, false);
        }
    }
}
