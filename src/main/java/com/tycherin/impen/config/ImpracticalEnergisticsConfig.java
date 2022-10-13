package com.tycherin.impen.config;

import appeng.core.AEConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class ImpracticalEnergisticsConfig {

    private static final ImpracticalEnergisticsConfig INSTANCE;
    public static final FeatureFlags FLAGS;
    public static final ForgeConfigSpec SPEC;
    public static final PowerValues POWER;
    public static final MiscellaneousConfig MISC;
    public static final MachineSettings SETTINGS;
    static {
        INSTANCE = new ImpracticalEnergisticsConfig();
        FLAGS = INSTANCE.flags;
        SPEC = INSTANCE.spec;
        POWER = INSTANCE.power;
        MISC = INSTANCE.misc;
        SETTINGS = INSTANCE.settings;
    }

    private final ForgeConfigSpec spec;
    private final FeatureFlags flags;
    private final PowerValues power;
    private final MachineSettings settings;
    private final MiscellaneousConfig misc;

    public ImpracticalEnergisticsConfig() {
        final Builder builder = new Builder();

        builder.push("FeatureFlags");
        this.flags = new FeatureFlags(builder);
        builder.pop();

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

    public static class FeatureFlags {
        private final BooleanValue bnlFlag;
        private final BooleanValue equipmentFlag;
        private final BooleanValue ismFlag;
        private final BooleanValue spaceCrystFlag;
        private final BooleanValue pdFlag;
        private final BooleanValue plantFlag;
        private final BooleanValue capturePlaneFlag;
        private final BooleanValue toasterFlag;

        public FeatureFlags(final ForgeConfigSpec.Builder builder) {
            this.bnlFlag = builder
                    .comment("Set to false to disable the Beamed Network Link block")
                    .define("enable_beamed_network_link", true);
            this.equipmentFlag = builder
                    .comment("Set to false to disable equipment items (Lunchbox Cell, Spatial tools)")
                    .define("enable_equipment", true);
            this.ismFlag = builder
                    .comment("Set to false to disable the Imaginary Space Manipulator block and related items")
                    .define("enable_imaginary_space_manipulator", true);
            this.spaceCrystFlag = builder
                    .comment("Set to false to disable the Spatial Crystallizer block")
                    .define("enable_spatial_crystallizer", true);
            this.pdFlag = builder
                    .comment("Set to false to disable the Possibility Disintegrator block and related items")
                    .define("enable_possibility_disintegrator", true);
            this.plantFlag = builder
                    .comment("Set to false to disable the ability to plany AE2 seeds as crops")
                    .define("enable_ae2_crops", true);
            this.capturePlaneFlag = builder
                    .comment("Set to false to disable the Capture Plane block")
                    .define("enable_capture_plane", true);
            this.toasterFlag = builder
                    .comment("Set to false to disable the Toaster Drive block")
                    .define("enable_toaster_drive", true);
        }

        public boolean enableBeamedNetworkLink() {
            return bnlFlag.get();
        }

        public boolean enableEquipment() {
            return equipmentFlag.get();
        }

        public boolean enableImaginarySpaceManipulator() {
            return ismFlag.get();
        }

        public boolean enableSpaceCrystallizer() {
            return spaceCrystFlag.get();
        }

        public boolean enablePossibilityDisintegrator() {
            return pdFlag.get();
        }

        public boolean enablePlants() {
            return plantFlag.get();
        }

        public boolean enableCapturePlane() {
            return capturePlaneFlag.get();
        }

        public boolean enableToasterDrive() {
            return toasterFlag.get();
        }
    }

    public static class PowerValues {
        private final DoubleValue globalModifier;
        private final IntValue bnl;
        private final IntValue ism;
        private final IntValue iss;
        private final IntValue psdTick;
        private final IntValue psdOp;
        private final IntValue spc;

        public PowerValues(final Builder builder) {
            this.globalModifier = builder.comment("Global power consumption modifier for all machines")
                    .defineInRange("global_power_modifier", 1.0, 0.0, 100.0);

            builder.comment("Power consumption for various machines are defined below. Units are AE.");
            this.bnl = builder.comment("Beamed Network Link consumption per tick")
                    .defineInRange("beamed_network_link", 10, 0, Integer.MAX_VALUE);
            this.ism = builder.comment("Imaginary Space Manipulator consumption per operation")
                    .defineInRange("imaginary_space_manipulator", 10, 0, Integer.MAX_VALUE);
            this.iss = builder.comment("Imaginary Space Stabilizer consumption per tick")
                    .defineInRange("imaginary_space_stabilizer", 10, 0, Integer.MAX_VALUE);
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

        public double imaginarySpaceStabilizerCost() {
            return iss.get() * globalModifier.get();
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
        private final BooleanValue addCraftingRecipes;
        private final BooleanValue addOtherRecipes;

        public MiscellaneousConfig(final Builder builder) {
            this.overrideAe2 = builder
                    .comment(
                            "If true, enables fluix-style in-world crafting even if AE2's in-world fluix crafting is disabled")
                    .define("force_in_world_crafting", false);
            this.addCraftingRecipes = builder
                    .comment("Set to false to disable all normal crafting recipes added by this mod")
                    .define("add_crafting_recipes", true);
            this.addOtherRecipes = builder
                    .comment(
                            "Set to false to disable adding any non-standard recipe types. NOTE: Several blocks added by this mod won't do anything if this is set, unless you add custom recipes yourself.")
                    .define("add_custom_recipes", true);
        }

        public boolean isInWorldCraftingEnabled() {
            return AEConfig.instance().isInWorldFluixEnabled() || overrideAe2.get();
        }

        public boolean addCraftingRecipes() {
            return addCraftingRecipes.get();
        }

        public boolean addOtherRecipes() {
            return addOtherRecipes.get();
        }
    }
}
