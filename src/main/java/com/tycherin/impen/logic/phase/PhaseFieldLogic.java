package com.tycherin.impen.logic.phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.blockentity.PhaseFieldControllerBlockEntity;
import com.tycherin.impen.config.ImpenConfig;
import com.tycherin.impen.util.MobUtil;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.MachineSource;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.dimension.DimensionType;
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

    private final PhaseFieldControllerBlockEntity be;
    private final MachineSource actionSource;
    // Lazy load these two since the level isn't available when the block entity is being instantiated
    private final Lazy<Player> fakePlayer;
    private final Lazy<DamageSource> playerDamageSource;
    private final Map<Entity, InFieldStatus> inFieldMap = new HashMap<>();
    private final double consumeChance;
    /** Cache of AABBs to search for affected entities in */
    private Optional<List<AABB>> aabbCache = Optional.empty();
    private PhaseFieldOperation operation = new PhaseFieldOperation(Collections.emptyList(), null);

    public PhaseFieldLogic(final PhaseFieldControllerBlockEntity be) {
        this.be = be;
        this.actionSource = new MachineSource(be);
        this.fakePlayer = Lazy.of(() -> FakePlayerFactory.getMinecraft((ServerLevel)be.getLevel()));
        this.playerDamageSource = Lazy.of(() -> new EntityDamageSource("phase_field", fakePlayer.get()));
        this.consumeChance = ImpenConfig.SETTINGS.possibilityDisintegratorConsumeChance();
    }

    /**
     * Attempts to execute a Phase Field operation based on the currently configured inputs
     * 
     * @return True if an operation was executed; false otherwise
     */
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

        if (!this.operation.effect().actuallyDoesSomething()) {
            // The operation exists, but it won't have any effect; skip the operation
            return false;
        }

        final MEStorage storage = this.be.getGridNode().getGrid().getStorageService().getInventory();
        final List<Item> extractedInputs = new ArrayList<>();

        final boolean consumeIngredients = this
                .rollRandom(this.consumeChance * this.operation.effect.consumeRateChange);

        for (final var configuredInput : this.operation.inputs()) {
            final Actionable act = consumeIngredients ? Actionable.MODULATE : Actionable.SIMULATE;
            final long amountExtracted = storage.extract(AEItemKey.of(configuredInput), 1, act, this.actionSource);

            if (amountExtracted == 1) {
                extractedInputs.add(configuredInput);
            }
            else {
                // Found an ingredient that was missing. Return all extracted ingredients and cancel the operation.
                if (consumeIngredients) {
                    extractedInputs.forEach(extractedInput -> {
                        final long remainder = storage.insert(AEItemKey.of(extractedInput), 1, Actionable.MODULATE,
                                this.actionSource);
                        if (remainder > 0) {
                            // This shouldn't happen because we're single-threaded from the point where the item was
                            // extracted in the first place
                            log.warn("Failed to put ingredient {} back into ME storage", extractedInput);
                        }
                    });
                }
                else {
                    // If no ingredients were consumed, then nothing needs to be cleaned up
                }

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
            if (consumeIngredients) {
                extractedInputs.forEach(extractedInput -> {
                    storage.insert(AEItemKey.of(extractedInput), 1, Actionable.MODULATE, this.actionSource);
                });
            }
            else {
                // If no ingredients were consumed, then nothing needs to be cleaned up
            }
            return false;
        }

        // TODO Add VFX for the field being active

        inFieldMap.values().forEach(status -> status.touchedThisCycle = false);
        affectedMobs.forEach(this::applyEffect);

        // Any mobs that weren't in the field this cycle need to be removed from the tracking list
        final List<InFieldStatus> entitiesToRemove = inFieldMap.values().stream()
                .filter(status -> !status.touchedThisCycle)
                .collect(Collectors.toList());
        entitiesToRemove.forEach(this::clearEntity); // separate step to avoid concurrent modification

        return true;
    }

    private void applyEffect(final Mob target) {
        final var effect = this.operation.effect;

        final int timeInField = this.incrementTimeInField(target, effect.timedEffectBoostLevel);

        // Set this flag to skip all effects that would otherwise happen later
        boolean skipSubsequentEffects = false;

        // Sliming
        if (effect.slimeifyChance > 0) {
            if (this.rollRandom(effect.slimeifyChance)) {
                final EntityType<?> type;
                if (target.getLevel().dimension().location().equals(DimensionType.NETHER_LOCATION.location())) {
                    type = EntityType.MAGMA_CUBE;
                }
                else {
                    type = EntityType.SLIME;
                }
                final Entity slimeEntity = type.spawn((ServerLevel)target.getLevel(), null, null,
                        target.getOnPos(), MobSpawnType.TRIGGERED, true, true);

                if (slimeEntity != null) {
                    target.discard();
                    this.clearEntity(target);
                    this.incrementTimeInField(slimeEntity, effect.timedEffectBoostLevel);
                    skipSubsequentEffects = true;
                }
                else {
                    log.warn("Failed to spawn {} at {}", type.toShortString(), target.getOnPos());
                }
            }
        }

        // Damage
        if (!skipSubsequentEffects && (effect.fixedDamage > 0 || effect.timeBasedDamageScalingLevel > 0)) {
            this.setLocked(target, true);

            // Edge case: if the target is a slime (or something similar), it will spawn children on death, and those
            // children will inherit a bunch of flags, including the noAi one. So here, we need to undo that flag, then
            // reset it if the entity didn't die.
            target.setNoAi(false);

            // Scale damage based on how long the mob has been trapped. This is geometric scaling, so it will kill large
            // mobs in a reasonable amount of time without insta-gibbing small mobs.
            final float maxDamage = (float)((effect.fixedDamage + (effect.timeBasedDamageScalingLevel * timeInField))
                    * effect.globalEffectModifier);
            // Math.min() to avoid unbounded damage scaling shenanigans.
            final float damageToDeal = Math.min(20, maxDamage);

            if (effect.playerKillLoot) {
                target.hurt(playerDamageSource.get(), damageToDeal);
            }
            else {
                target.hurt(DAMAGE_SOURCE, damageToDeal);
            }

            if (!target.isAlive()) {
                if (effect.eggSpawnChance > 0) {
                    final var spawnEgg = ForgeSpawnEggItem.fromEntityType(target.getType());
                    if (spawnEgg != null) {
                        final boolean doSpawnEgg = this.rollRandom(effect.eggSpawnChance * effect.globalEffectModifier);
                        if (doSpawnEgg) {
                            target.spawnAtLocation(spawnEgg);
                        }
                    }
                }

                if (effect.timeBasedLootDropLevel > 0) {
                    final int effectiveTimeInField = timeInField * switch (effect.timeBasedLootDropLevel) {
                    case 1 -> 2;
                    case 2 -> 4;
                    case 3 -> 8;
                    default -> 1;
                    };
                    final double extraLootSets = Math.log10(effectiveTimeInField) * effect.globalEffectModifier;
                    int extraLootSetsFixed = (int)extraLootSets;
                    extraLootSetsFixed += this.rollRandom(extraLootSets - extraLootSetsFixed) ? 1 : 0;

                    if (extraLootSetsFixed > 0) {
                        // This code is mostly adapted from LivingEntity code that isn't easily accessible
                        //
                        // Worth noting that this will only spawn items from the normal loot table, i.e. no special
                        // drops (like blocks held by Enderman - no dupe glitches for you)
                        final ResourceLocation resourcelocation = target.getLootTable();
                        final LootTable loottable = getLevel().getServer().getLootTables().get(resourcelocation);
                        final var ctxBuilder = new LootContext.Builder(getLevel())
                                .withRandom(RAND)
                                .withParameter(LootContextParams.THIS_ENTITY, target)
                                .withParameter(LootContextParams.ORIGIN, target.position())
                                .withParameter(LootContextParams.DAMAGE_SOURCE, DAMAGE_SOURCE);

                        if (effect.playerKillLoot) {
                            ctxBuilder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, fakePlayer.get())
                                    .withParameter(LootContextParams.KILLER_ENTITY, fakePlayer.get())
                                    .withParameter(LootContextParams.DIRECT_KILLER_ENTITY, fakePlayer.get());
                        }

                        final LootContext ctx = ctxBuilder.create(LootContextParamSets.ENTITY);

                        for (int i = 0; i < extraLootSetsFixed; i++) {
                            loottable.getRandomItems(ctx).forEach(target::spawnAtLocation);
                        }
                    }
                }

                if (effect.timeBasedExperienceDropLevel > 0) {
                    // Note that this requires an AT to access
                    final int xpBase = target.getExperienceReward(null);

                    if (xpBase > 0) {
                        final int effectiveTimeInField = timeInField * switch (effect.timeBasedExperienceDropLevel) {
                        case 1 -> 2;
                        case 2 -> 4;
                        case 3 -> 8;
                        default -> 1;
                        };
                        final double modifier = Math.log10(effectiveTimeInField) * effect.globalEffectModifier;
                        final int reward = (int)(xpBase * modifier);
                        ExperienceOrb.award((ServerLevel)target.getLevel(), target.getPosition(1.0f), reward);
                    }
                }

                this.clearEntity(target);
                skipSubsequentEffects = true;
            }
            else {
                // Target is still alive, so re-lock it
                target.setNoAi(true);
            }
        }

        // Healing
        if (!skipSubsequentEffects && effect.fixedHealing > 0) {
            // Note that despite the fact that this is labeled "healing," it actually restores health directly, so it
            // will still heal undead mobs properly
            target.heal((float)(effect.fixedHealing * effect.globalEffectModifier));
        }

        // Breeding
        if (!skipSubsequentEffects && effect.doBreeding) {
            if (target instanceof Animal animal) {
                if (animal.isInLove()) {
                    // Breeding is already active - do nothing
                }
                else if (animal.canFallInLove()) {
                    animal.setInLove(null);
                }
                else if (effect.timeBasedBreedCdrLevel > 0) {
                    if (!animal.isBaby() && animal.getAge() > 0) {
                        animal.ageUp((int)(effect.timeBasedBreedCdrLevel * 20 * effect.globalEffectModifier));
                    }
                }
            }
        }

        // Floral effect

        // TODO Implement this
        // This will also need to handle the fact that it can affect players as well as mobs somehow
        // TODO Once I implement Floral, I should go back and revisit the other "On Death" effects to see if I can cause
        // them to trigger on any death as well (i.e. not just a death triggered by the Phase Field itself).
    }

    private void setLocked(final Mob target, final boolean shouldBeLocked) {
        final var status = this.inFieldMap.get(target);

        if (status.locked && !shouldBeLocked) {
            target.setNoGravity(true);
            target.setNoAi(true);
            target.setDeltaMovement(Vec3.ZERO);
            status.locked = false;
        }
        else if (!status.locked && shouldBeLocked) {
            target.setNoGravity(false);
            target.setNoAi(false);
            status.locked = true;
        }
    }

    private void clearEntity(final Entity target) {
        if (this.inFieldMap.containsKey(target)) {
            this.clearEntity(this.inFieldMap.get(target));
        }
    }

    private int incrementTimeInField(final Entity target, final int timedEffectBoostLevel) {
        if (!this.inFieldMap.containsKey(target)) {
            this.inFieldMap.put(target, new InFieldStatus(target));
        }
        final var status = this.inFieldMap.get(target);
        status.timeInField += timedEffectBoostLevel;
        status.touchedThisCycle = true;
        return status.timeInField;
    }

    private void clearEntity(final InFieldStatus status) {
        if (status.entity instanceof Mob mob) {
            this.setLocked(mob, false);
        }
        this.inFieldMap.remove(status.entity);
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
        final Map<Item, Integer> inputCounts = new HashMap<>();
        configuredInputs.forEach(item -> {
            if (!inputCounts.containsKey(item)) {
                inputCounts.put(item, 0);
            }
            inputCounts.merge(item, 1, Integer::sum);
        });

        int fixedDamage = 0;
        int timeBasedDamageScalingLevel = 0;
        boolean playerKillLoot = false;
        double eggSpawnChance = 0.0;
        int fixedHealing = 0;
        int timeBasedExperienceDropLevel = 0;
        boolean doBreeding = false;
        int timeBasedBreedCdrLevel = 0;
        double consumeRateChange = 1;
        double globalEffectModifier = 1;
        int timeBasedLootDropLevel = 0;
        int fieldSizeBoost = 0;
        int timedEffectBoostLevel = 0;
        double slimeifyChance = 0;
        int floralEffectLevel = 0;

        for (var entry : inputCounts.entrySet()) {
            final Item item = entry.getKey();
            final Integer count = entry.getValue();
            if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY.asItem())) { // TODO Red
                fixedDamage += count * 2;
                timeBasedDamageScalingLevel += count;
            }
            else if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY.asItem())) { // TODO Orange
                fixedDamage += count;
                playerKillLoot = true;
            }
            else if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY.asItem())) { // TODO Yellow
                fixedDamage += count;
                eggSpawnChance += switch (count) {
                case 1 -> 0.1;
                case 2 -> 0.3;
                case 3 -> 0.5;
                default -> 0;
                };
            }
            else if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY.asItem())) { // TODO Lime
                fixedHealing += count * 2;
                timeBasedExperienceDropLevel = count;
            }
            else if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY.asItem())) { // TODO Green
                doBreeding = true;
                timeBasedBreedCdrLevel = count;
            }
            else if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY.asItem())) { // TODO White
                consumeRateChange += count * 0.3;
                globalEffectModifier += count * 0.5;
            }
            else if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY.asItem())) { // TODO Blue
                timeBasedLootDropLevel = count;
            }
            else if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY.asItem())) { // TODO Gray
                globalEffectModifier += count * 0.1;
                consumeRateChange -= count * 0.1;
            }
            else if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY.asItem())) { // TODO Pale
                fieldSizeBoost += count * 2;
                timedEffectBoostLevel += count;
            }
            else if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY.asItem())) { // TODO Slimy
                slimeifyChance = switch (count) {
                case 1 -> 0.1;
                case 2 -> 0.25;
                case 3 -> 0.5;
                default -> 0.0;
                };
                consumeRateChange = consumeRateChange / (1 + count);
            }
            else if (item.equals(ImpenRegistry.DISINTEGRATOR_CAPSULE_EMPTY.asItem())) { // TODO Floral
                floralEffectLevel = count;
            }
            else {
                log.warn("Unrecognized input item: {}; item will be ignored", item);
            }
        }

        final PhaseFieldEffect effect = PhaseFieldEffect.builder()
                .consumeRateChange(consumeRateChange)
                .doBreeding(doBreeding)
                .eggSpawnChance(eggSpawnChance)
                .fieldSizeBoost(fieldSizeBoost)
                .fixedDamage(fixedDamage)
                .fixedHealing(fixedHealing)
                .globalEffectModifier(globalEffectModifier)
                .playerKillLoot(playerKillLoot)
                .slimeifyChance(slimeifyChance)
                .timeBasedBreedCdrLevel(timeBasedBreedCdrLevel)
                .timeBasedDamageScalingLevel(timeBasedDamageScalingLevel)
                .timeBasedExperienceDropLevel(timeBasedExperienceDropLevel)
                .timeBasedLootDropLevel(timeBasedLootDropLevel)
                .timedEffectBoostLevel(timedEffectBoostLevel)
                .floralEffectLevel(floralEffectLevel)
                .build();

        final var oldOperation = this.operation;
        this.operation = new PhaseFieldOperation(configuredInputs, effect);

        // If the new operation doesn't do damage but the old one did, release any locked mobs
        if (oldOperation.effect.shouldLock() && !this.operation.effect.shouldLock()) {
            this.inFieldMap.keySet().forEach(entity -> {
                if (entity instanceof Mob mob) {
                    this.setLocked(mob, false);
                }
            });
        }
    }

    private static record PhaseFieldOperation(List<Item> inputs, PhaseFieldEffect effect) {
    }

    /*
     * == Capsule Effects ==
     * 
     * Red
     * | Fixed - Damage
     * | Timed - Damage
     * Orange
     * | Fixed - Damage
     * | On Death - Player-kill only loot
     * Yellow
     * | Fixed - Damage
     * | On Death - Spawn egg (chance)
     * Lime
     * | Fixed - Healing
     * | On Death - Drop experience
     * Green
     * | Fixed - Trigger breeding
     * | Fixed - Reduce breeding cooldown
     * White
     * | Fixed - Improve capsules
     * | Fixed - Reduce consumption
     * Blue
     * | On Death + Timed - Drop extra loot
     * Gray
     * | Fixed - Reduces consumption
     * | Fixed - Improves capsules
     * Pale
     * | Fixed - Increases field area
     * | Fixed - Increases timed effect accumulation speed
     * Slimy
     * | Fixed - Turns mobs into slimes
     * | Fixed - Reduces consumption
     * Floral
     * | Fixed - Adds "Flowering" buff
     * | On Death (w/ Flowering) - Drop flowers
     */

    @Builder
    private static record PhaseFieldEffect(
            int fixedDamage,
            int timeBasedDamageScalingLevel,
            boolean playerKillLoot,
            double eggSpawnChance,
            int fixedHealing,
            int timeBasedExperienceDropLevel,
            boolean doBreeding,
            int timeBasedBreedCdrLevel,
            double consumeRateChange,
            double globalEffectModifier,
            int timeBasedLootDropLevel,
            int fieldSizeBoost,
            int timedEffectBoostLevel,
            double slimeifyChance,
            int floralEffectLevel) {

        /** @return True if this effect causes mobs within the field to get locked in place; false otherwise */
        public boolean shouldLock() {
            return this.fixedDamage > 0 || this.timeBasedDamageScalingLevel > 0;
        }

        /** @return True if this effect will actually do something; false otherwise */
        public boolean actuallyDoesSomething() {
            return this.fixedDamage > 0
                    || this.timeBasedDamageScalingLevel > 0
                    || this.fixedHealing > 0
                    || this.slimeifyChance > 0
                    || this.doBreeding
                    || this.floralEffectLevel > 0;
        }
    }

    @Data
    private static class InFieldStatus {
        final Entity entity;
        int timeInField = 0;
        boolean locked = false;
        boolean touchedThisCycle = false;
    }
}
