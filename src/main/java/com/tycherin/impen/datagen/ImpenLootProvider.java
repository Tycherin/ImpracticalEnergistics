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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

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
        // Override the vanilla LootTableProvider behavior, which tries to validate against all vanilla loot tables
    }

    private static class ImpenBlockLoot extends BlockLoot {
        private final Set<Block> knownBlocks = new HashSet<>();

        @Override
        protected void addTables() {
            this.dropSelf(ImpenRegistry.AEROCRYSTAL_BLOCK.block());
            this.dropSelf(ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER.block());
            this.dropSelf(ImpenRegistry.BEAMED_NETWORK_LINK.block());
            this.dropSelf(ImpenRegistry.BLAZING_AEROCRYSTAL_BLOCK.block());
            this.dropSelf(ImpenRegistry.EJECTION_DRIVE.block());
            this.dropSelf(ImpenRegistry.EXOTIC_AEROCRYSTAL_BLOCK.block());
            this.dropSelf(ImpenRegistry.POSSIBILITY_DISINTEGRATOR.block());
            this.dropSelf(ImpenRegistry.RIFT_SHARD_BLOCK.block());
            this.dropSelf(ImpenRegistry.RIFTSTONE.block());
            this.dropSelf(ImpenRegistry.RIFTSTONE_STAIRS.block());
            this.dropSelf(ImpenRegistry.RIFTSTONE_BRICK.block());
            this.dropSelf(ImpenRegistry.RIFTSTONE_BRICK_STAIRS.block());
            this.dropSelf(ImpenRegistry.SMOOTH_RIFTSTONE.block());
            this.dropSelf(ImpenRegistry.SMOOTH_RIFTSTONE_STAIRS.block());
            this.dropSelf(ImpenRegistry.SPATIAL_RIFT_COLLAPSER.block());
            this.dropSelf(ImpenRegistry.SPATIAL_RIFT_SPAWNER.block());
            this.dropSelf(ImpenRegistry.SPATIAL_RIFT_MANIPULATOR.block());

            this.add(ImpenRegistry.RIFTSTONE_SLAB.asBlock(), BlockLoot::createSlabItemTable);
            this.add(ImpenRegistry.RIFTSTONE_BRICK_SLAB.asBlock(), BlockLoot::createSlabItemTable);
            this.add(ImpenRegistry.SMOOTH_RIFTSTONE_SLAB.asBlock(), BlockLoot::createSlabItemTable);

            // Give Unstable Riftstone a chance to drop Riftstone Dust when broken, but nothing else
            this.add(ImpenRegistry.UNSTABLE_RIFTSTONE.asBlock(), (block) -> {
                final var lootItem = LootItem.lootTableItem(ImpenRegistry.RIFTSTONE_DUST.asItem())
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                Enchantments.BLOCK_FORTUNE, 0.1F, 0.14285715F, 0.25F, 1.0F));
                return LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(applyExplosionDecay(block, lootItem)));
            });

            this.add(ImpenRegistry.RIFT_SHARD_ORE.block(), (block) -> {
                // TODO This should drop more
                return createOreDrop(block, ImpenRegistry.RIFT_SHARD.asItem());
            });
            this.add(ImpenRegistry.NETHER_GLOWSTONE_ORE.block(), (block) -> {
                // TODO This should probably be... different
                return createOreDrop(block, Items.GLOWSTONE_DUST);
            });
            this.add(ImpenRegistry.NETHER_DEBRIS_ORE.block(), (block) -> {
                // TODO This should probably be nerfed
                return createOreDrop(block, Items.NETHERITE_SCRAP);
            });
            this.add(ImpenRegistry.END_AMETHYST_ORE.block(), (block) -> {
                // TODO This should probably be buffed?
                return createOreDrop(block, Items.AMETHYST_SHARD);
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
