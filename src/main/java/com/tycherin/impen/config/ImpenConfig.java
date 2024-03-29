package com.tycherin.impen.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class ImpenConfig {

    private static final ImpenConfig INSTANCE;
    public static final ForgeConfigSpec SPEC;
    public static final PowerValues POWER;
    public static final MiscellaneousConfig MISC;
    public static final MachineSettings SETTINGS;
    static {
        INSTANCE = new ImpenConfig();
        SPEC = INSTANCE.spec;
        POWER = INSTANCE.power;
        MISC = INSTANCE.misc;
        SETTINGS = INSTANCE.settings;
    }

    private final ForgeConfigSpec spec;
    private final PowerValues power;
    private final MachineSettings settings;
    private final MiscellaneousConfig misc;

    public ImpenConfig() {
        final Builder builder = new Builder();

        builder.push("Machines");
        {
            builder.push("Power");
            this.power = new PowerValues(builder);
            builder.pop();

            builder.push("Settings");
            this.settings = new MachineSettings(builder);
            builder.pop();
        }
        builder.pop();

        builder.push("Miscellaneous");
        this.misc = new MiscellaneousConfig(builder);
        builder.pop();

        this.spec = builder.build();
    }

    public static class PowerValues {
        private final DoubleValue globalModifier;
        private final IntValue bnl;
        private final IntValue psdTick;
        private final IntValue psdOp;
        private final IntValue atm;
        private final IntValue srs;
        private final IntValue srm;
        private final IntValue src;

        public PowerValues(final Builder builder) {
            this.globalModifier = builder.comment("Global power consumption modifier for all machines")
                    .defineInRange("global_power_modifier", 1.0, 0.0, 100.0);

            builder.comment("Power consumption for various machines are defined below. Units are AE.");
            this.bnl = builder.comment("Beamed Network Emitter consumption per tick while active")
                    .defineInRange("beamed_network_emitter", 10, 0, Integer.MAX_VALUE);
            this.srs = builder.comment("Spatial Rift Spawner consumption per tick")
                    .defineInRange("spatial_rift_spawner", 40, 0, Integer.MAX_VALUE);
            this.srm = builder.comment("Spatial Rift Manipulator consumption per tick")
                    .defineInRange("spatial_rift_manipulator", 40, 0, Integer.MAX_VALUE);
            this.src = builder.comment("Spatial Rift Collapser consumption per tick")
                    .defineInRange("spatial_rift_collapser", 40, 0, Integer.MAX_VALUE);
            this.psdTick = builder.comment("Possibility Disintegrator consumption per tick")
                    .defineInRange("possibility_disintegrator_tick", 10, 0, Integer.MAX_VALUE);
            this.psdOp = builder.comment("Possibility Disintegrator consumption per operation")
                    .defineInRange("possibility_disintegrator_operation", 100, 0, Integer.MAX_VALUE);
            this.atm = builder.comment("Atmospheric Crystallizer consumption per tick")
                    .defineInRange("atmospheric_crystallizer_operation", 10, 0, Integer.MAX_VALUE);
        }

        public double beamedNetworkEmitterCost() {
            return bnl.get() * globalModifier.get();
        }

        public double spatialRiftSpawnerCost() {
            return srs.get() * globalModifier.get();
        }

        public double spatialRiftManipulatorCost() {
            return srm.get() * globalModifier.get();
        }

        public double spatialRiftCollapserCost() {
            return src.get() * globalModifier.get();
        }

        public double possibilityDisintegratorCostTick() {
            return psdTick.get() * globalModifier.get();
        }

        public double possibilityDisintegratorCostOperation() {
            return psdOp.get() * globalModifier.get();
        }

        public double atmosphericCrystallizerCost() {
            return atm.get() * globalModifier.get();
        }
    }

    public static class MachineSettings {
        private final IntValue bnlRange;
        private final IntValue atmWorkRate;
        private final IntValue psdWorkRate;
        private final DoubleValue psdConsumeChance;
        private final BooleanValue riftOverwrite;

        public MachineSettings(final Builder builder) {
            this.bnlRange = builder.comment("Default connection range for Beamed Network blocks")
                    .defineInRange("beamed_network_range", 16, 4, 64);
            this.atmWorkRate = builder.comment("Base number of ticks for each Atmospheric Crystallizer operation")
                    .defineInRange("atmospheric_crystallizer_work_rate", 80, 4, Integer.MAX_VALUE);
            this.psdWorkRate = builder.comment("Base number of ticks for each Possibility Disintegrator operation")
                    .defineInRange("possibility_disintegrator_work_rate", 40, 1, Integer.MAX_VALUE);
            this.psdConsumeChance = builder.comment(
                    "Chance that the Possibility Disintegrator will consume its inputs on each operation")
                    .defineInRange("possibility_disintegrator_consume_chance", 0.015, 0.0, 1.0);
            this.riftOverwrite = builder
                    .comment("If true, the Spatial Rift Collapser can overwrite existing blocks in spatial storage")
                    .define("spatial_rift_overwrite_blocks", false);
        }

        public int beamedNetworkLinkRange() {
            return bnlRange.get();
        }

        public int atmosphericCrystallizerWorkRate() {
            return atmWorkRate.get();
        }

        public int possibilityDisintegratorWorkRate() {
            return psdWorkRate.get();
        }

        public double possibilityDisintegratorConsumeChance() {
            return psdConsumeChance.get();
        }

        public boolean canSRCOverwriteBlocks() {
            return riftOverwrite.get();
        }
    }

    public static class MiscellaneousConfig {
        public MiscellaneousConfig(final Builder builder) {
        }
    }
}
