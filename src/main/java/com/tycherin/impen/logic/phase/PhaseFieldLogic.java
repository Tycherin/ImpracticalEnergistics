package com.tycherin.impen.logic.phase;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.tycherin.impen.blockentity.PhaseFieldControllerBlockEntity;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.MachineSource;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@RequiredArgsConstructor
public class PhaseFieldLogic {

    private final PhaseFieldControllerBlockEntity be;

    private Optional<List<AABB>> aabbCache = Optional.empty();

    public boolean doOperation() {
        if (this.aabbCache.isEmpty()) {
            this.recomputeAABBCache();
        }

        final Set<Mob> affectedEntities = this.aabbCache.get().stream()
                .flatMap(aabb -> getLevel().getEntitiesOfClass(Mob.class, aabb).stream())
                .collect(Collectors.toSet());

        // Temporary code
        // TODO Replace with proper capsule configuration
        // TODO Replace with proper inventory management
        final MEStorage storage = this.be.getGridNode().getGrid().getStorageService().getInventory();
        final AtomicBoolean gotIngredients = new AtomicBoolean();
        gotIngredients.set(true);
        this.be.getInternalInventory().forEach(configuredInput -> {
            if (configuredInput.isEmpty()) {
                return;
            }

            if (storage.extract(AEItemKey.of(configuredInput), 1, Actionable.SIMULATE, new MachineSource(be)) != 1) {
                gotIngredients.set(false);
            }
        });

        if (gotIngredients.get()) {
            affectedEntities.forEach(Mob::kill);
        }
        
        return true;
    }

    private Level getLevel() {
        return this.be.getLevel();
    }

    public void recomputeAABBCache() {
        this.aabbCache = Optional.of(this.be.getEmitters().stream()
                .map(emitter -> new AABB(emitter.getBlockEntity().getBlockPos().relative(emitter.getSide())))
                .collect(Collectors.toList()));
    }
}
