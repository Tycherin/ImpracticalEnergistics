package com.tycherin.impen.blockentity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.tycherin.impen.ImpracticalEnergisticsMod;
import com.tycherin.impen.config.ImpenConfig;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.me.helpers.MachineSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.ForgeSpawnEggItem;

public class PossibilityDisintegratorBlockEntity extends AENetworkBlockEntity
        implements IGridTickable, IUpgradeableObject {

    private static final Random RAND = new Random();
    private static final DamageSource DAMAGE_SOURCE = new DamageSource("possibility_disintegrator");

    private static final double EGG_CHANCE_BASE = .4;
    private static final double LOOT_CHANCE_BASE = .1;

    private final Map<Mob, TargetStats> targets = new HashMap<>();
    private final IUpgradeInventory upgrades;
    private final int baseTicksPerOperation;

    private int disintegrationDelay;

    private static class TargetStats {
        public int disintegrationDelay = 0;
        public int timesHurt = 0;
    }

    public PossibilityDisintegratorBlockEntity(final BlockPos pos,
            final BlockState blockState) {
        super(ImpracticalEnergisticsMod.POSSIBILITY_DISINTEGRATOR_BE.get(), pos, blockState);

        this.getMainNode()
                .setExposedOnSides(EnumSet.complementOf(EnumSet.of(Direction.UP)))
                .addService(IGridTickable.class, this)
                .setIdlePowerUsage(ImpenConfig.POWER.possibilityDisintegratorCostTick())
                .setFlags(GridFlags.REQUIRE_CHANNEL);

        // TODO Expose upgrade inventory in UI
        this.upgrades = UpgradeInventories.forMachine(ImpracticalEnergisticsMod.POSSIBILITY_DISINTEGRATOR_ITEM.get(), 2,
                this::saveChanges);
        this.baseTicksPerOperation = ImpenConfig.SETTINGS.possibilityDisintegratorWorkRate();
        this.disintegrationDelay = this.baseTicksPerOperation;
    }

    public void handleEntity(final Entity entity) {
        if (!this.getMainNode().isActive()) {
            return;
        }

        if (entity instanceof Mob mob && !targets.containsKey(mob)) {
            if (mob.isBaby() || mob instanceof WitherBoss || mob instanceof EnderDragon) {
                // TODO Forge 1.19 adds a tag to see if a mob is a boss or not, we should denylist those instead of
                // hardcoding vanilla mob classes
                return;
            }
            this.lockTarget(mob);
            targets.put(mob, new TargetStats());
        }
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(10, 10, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (!this.getMainNode().isActive()) {
            if (!this.targets.isEmpty()) {
                this.unlockAll();
            }
            return TickRateModulation.SLEEP;
        }
        else if (this.targets.isEmpty()) {
            return TickRateModulation.SLEEP;
        }

        // Use an iterator here to avoid concurrent modification problems
        final Iterator<Entry<Mob, TargetStats>> iter = this.targets.entrySet().iterator();
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
                final var stats = entry.getValue();
                stats.disintegrationDelay += ticksSinceLastCall;
                if (stats.disintegrationDelay >= this.disintegrationDelay) {
                    this.disintegrate(target, stats);
                    if (target.isAlive()) {
                        stats.disintegrationDelay = 0;
                    }
                    else {
                        iter.remove();
                    }
                }
            }
        }

        if (targets.isEmpty()) {
            return TickRateModulation.SLEEP;
        }
        else {
            return TickRateModulation.SAME;
        }
    }

    @Override
    public void setRemoved() {
        this.unlockAll();
        super.setRemoved();
    }

    private void unlockAll() {
        this.targets.keySet().forEach(this::unlockTarget);
        this.targets.clear();
    }

    private void lockTarget(final Mob target) {
        if (!target.isAlive()) {
            return;
        }

        // TODO This breaks Magma Cubes spawning cubelets

        target.setNoAi(true);
        target.moveTo(this.getBlockPos().above(), target.getYRot(), target.getXRot());

        this.getMainNode().ifPresent((grid, node) -> {
            grid.getTickManager().wakeDevice(node);
        });
    }

    private void unlockTarget(final Mob target) {
        target.setNoAi(false);
    }

    private void recalculateDisintegrationDelay() {
        final int delay = switch (this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD)) {
        case 0 -> this.baseTicksPerOperation;
        case 1 -> (int) (this.baseTicksPerOperation * .6);
        case 2 -> (int) (this.baseTicksPerOperation * .4);
        default -> this.baseTicksPerOperation;
        };
        this.disintegrationDelay = Math.max(1, delay);
    }

    private void disintegrate(final Mob target, final TargetStats stats) {
        // TODO Use real items instead of placeholders
        // TODO Power consumption as well
        // TODO Expose ingredients in UI

        final boolean isLucky = this.consumeIngredient(Items.ACACIA_BUTTON);

        if (!this.consumeIngredient(Items.BREAD)) {
            // Scale damage based on how long the mob has been trapped. This is geometric scaling, so it will kill large
            // mobs in a reasonable amount of time without insta-gibbing small mobs.
            // If you don't want the mob to die, use the relevant ingredient to avoid doing damage.
            // Math.min() to avoid unbounded damage scaling shenanigans.
            target.hurt(DAMAGE_SOURCE, Math.min(20, stats.timesHurt + 1));
            stats.timesHurt++;

            if (!target.isAlive()) {
                final var spawnEgg = ForgeSpawnEggItem.fromEntityType(target.getType());
                if (spawnEgg != null && this.rollRandom(EGG_CHANCE_BASE * (isLucky ? 1 : 2))
                        && this.consumeIngredient(Items.EGG)) {
                    target.spawnAtLocation(spawnEgg);
                }

                return;
            }
        }

        if (this.rollRandom(LOOT_CHANCE_BASE * (isLucky ? 1 : 3)) && this.consumeIngredient(Items.LAPIS_LAZULI)) {
            // This code is mostly adapted from LivingEntity code that isn't directly accessible
            //
            // Worth noting that this will only spawn items from the normal loot table, i.e. no special drops (like
            // blocks held by Enderman - no dupe glitches for you)
            // TODO I should probably test that
            // TODO Add ingredient for allowing player kill only drops
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

    private boolean rollRandom(final double chance) {
        return RAND.nextDouble() < chance;
    }

    private boolean consumeIngredient(final Item ingredient) {
        final MEStorage storage = this.getGridNode().getGrid().getStorageService().getInventory();
        return storage.extract(AEItemKey.of(ingredient), 1, Actionable.MODULATE, new MachineSource(this)) == 1;
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

    @Override
    public void saveChanges() {
        super.saveChanges();
        this.recalculateDisintegrationDelay();
    }
}
