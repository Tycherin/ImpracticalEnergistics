package com.tycherin.impen.datagen;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;
import com.tycherin.impen.ImpenRegistry;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class ImpenLootProvider extends LootTableProvider {

    public ImpenLootProvider(final DataGenerator gen) {
        super(gen);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return Collections.singletonList(Pair.of(ImpenBlockLoot::new, LootContextParamSets.BLOCK));
    }

    @Override
    protected void validate(final Map<ResourceLocation, LootTable> map, final ValidationContext validationtracker) {
        // do not validate against all registered loot tables
    }

    private static class ImpenBlockLoot extends BlockLoot {
        private final Set<Block> knownBlocks = new HashSet<>();

        @Override
        protected void addTables() {
            this.dropSelf(ImpenRegistry.AEROCRYSTAL_BLOCK.block());
            this.dropSelf(ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER.block());
            this.dropSelf(ImpenRegistry.BEAMED_NETWORK_LINK.block());
            this.dropSelf(ImpenRegistry.BLAZING_AEROCRYSTAL_BLOCK.block());
            this.dropSelf(ImpenRegistry.EXOTIC_AEROCRYSTAL_BLOCK.block());
            this.dropSelf(ImpenRegistry.POSSIBILITY_DISINTEGRATOR.block());
            this.dropSelf(ImpenRegistry.RIFT_SHARD_BLOCK.block());
            this.dropSelf(ImpenRegistry.RIFTSTONE.block());
            this.dropSelf(ImpenRegistry.RIFTSTONE_BRICKS.block());
            this.dropSelf(ImpenRegistry.SPATIAL_RIFT_STABILIZER.block());
            this.dropSelf(ImpenRegistry.SPATIAL_RIFT_SPAWNER.block());
            this.dropSelf(ImpenRegistry.EJECTION_DRIVE.block());

            this.dropWhenSilkTouch(ImpenRegistry.RIFT_GLASS.block());

            this.add(ImpenRegistry.RIFT_SHARD_ORE.block(), (block) -> {
                return createOreDrop(block, ImpenRegistry.RIFT_SHARD.asItem());
            });

            this.add(ImpenRegistry.SMOOTH_RIFTSTONE.block(), (block) -> {
                return createSingleItemTableWithSilkTouch(block, ImpenRegistry.RIFTSTONE.block());
            });
        }

        @Override
        protected void add(final Block block, final LootTable.Builder builder) {
            super.add(block, builder);
            this.knownBlocks.add(block);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return this.knownBlocks;
        }
    }
}
