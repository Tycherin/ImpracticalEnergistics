package com.tycherin.impen.config;

import appeng.core.AEConfig;
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
        private final IntValue ism;
        private final IntValue psdTick;
        private final IntValue psdOp;
        private final IntValue spc;

        public PowerValues(final Builder builder) {
            this.globalModifier = builder.comment("Global power consumption modifier for all machines")
                    .defineInRange("global_power_modifier", 1.0, 0.0, 100.0);

            builder.comment("Power consumption for various machines are defined below. Units are AE.");
            this.bnl = builder.comment("Beamed Network Link consumption per tick while active")
                    .defineInRange("beamed_network_link", 10, 0, Integer.MAX_VALUE);
            this.ism = builder.comment("Imaginary Space Manipulator consumption per block modified")
                    .defineInRange("imaginary_space_manipulator", 80, 0, Integer.MAX_VALUE);
            this.psdTick = builder.comment("Possibility Disintegrator consumption per tick")
                    .defineInRange("possibility_disintegrator_tick", 10, 0, Integer.MAX_VALUE);
            this.psdOp = builder.comment("Possibility Disintegrator consumption per operation")
                    .defineInRange("possibility_disintegrator_operation", 100, 0, Integer.MAX_VALUE);
            this.spc = builder.comment("Spatial Crystallizer consumption per tick")
                    .defineInRange("spatial_crystallizer_operation", 10, 0, Integer.MAX_VALUE);
        }

        public double beamedNetworkLinkCost() {
            return bnl.get() * globalModifier.get();
        }

        public double imaginarySpaceManipulatorCost() {
            return ism.get() * globalModifier.get();
        }

        public double possibilityDisintegratorCostTick() {
            return psdTick.get() * globalModifier.get();
        }

        public double possibilityDisintegratorCostOperation() {
            return psdOp.get() * globalModifier.get();
        }

        public double spaceCrystallizerCost() {
            return spc.get() * globalModifier.get();
        }
    }

    public static class MachineSettings {
        private final IntValue bnlRange;
        private final IntValue spcWorkRate;
        private final IntValue psdWorkRate;

        public MachineSettings(final Builder builder) {
            this.bnlRange = builder.comment("Connection range for the Beamed Network Link block")
                    .defineInRange("beamed_network_link_range", 16, 2, 64);
            this.spcWorkRate = builder.comment("Base number of ticks for each Spatial Crystallizer operation")
                    .defineInRange("spatial_crystallizer_work_rate", 80, 4, Integer.MAX_VALUE);
            this.psdWorkRate = builder.comment("Base number of ticks for each Possibility Disintegrator operation")
                    .defineInRange("possibility_disintegrator_work_rate", 40, 1, Integer.MAX_VALUE);
        }

        public int beamedNetworkLinkRange() {
            return bnlRange.get();
        }

        public int spatialCrystallizerWorkRate() {
            return spcWorkRate.get();
        }

        public int possibilityDisintegratorWorkRate() {
            return psdWorkRate.get();
        }
    }

    public static class MiscellaneousConfig {
        private final BooleanValue overrideAe2;

        public MiscellaneousConfig(final Builder builder) {
            this.overrideAe2 = builder
                    .comment(
                            "If true, enables fluix-style in-world crafting even if AE2's in-world fluix crafting is disabled")
                    .define("force_in_world_crafting", false);
        }

        public boolean isInWorldCraftingEnabled() {
            return AEConfig.instance().isInWorldFluixEnabled() || overrideAe2.get();
        }
    }
}
