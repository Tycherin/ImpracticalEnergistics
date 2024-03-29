package com.tycherin.impen;

import com.tycherin.impen.client.gui.AtmosphericCrystallizerMenu;
import com.tycherin.impen.client.gui.PhaseFieldControllerMenu;
import com.tycherin.impen.client.gui.SpatialRiftCollapserMenu;
import com.tycherin.impen.client.gui.SpatialRiftManipulatorMenu;
import com.tycherin.impen.client.gui.SpatialRiftSpawnerMenu;
import com.tycherin.impen.config.ImpenConfig;
import com.tycherin.impen.datagen.ImpenBlockStateProvider;
import com.tycherin.impen.datagen.ImpenBlockTagsProvider;
import com.tycherin.impen.datagen.ImpenItemModelProvider;
import com.tycherin.impen.datagen.ImpenItemTagsProvider;
import com.tycherin.impen.datagen.ImpenLootProvider;
import com.tycherin.impen.datagen.ImpenPartModelProvider;
import com.tycherin.impen.datagen.recipe.ImpenRecipeProvider;
import com.tycherin.impen.part.CapturePlanePart;
import com.tycherin.impen.service.PhaseFieldService;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ImpracticalEnergisticsMod.MOD_ID)
public class ImpracticalEnergisticsMod {
    public static final String MOD_ID = "impracticalenergistics";

    public ImpracticalEnergisticsMod() {
        MinecraftForge.EVENT_BUS.register(this);

        final var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ImpenRegistry.register(modEventBus);

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ImpracticalEnergisticsClientSetup::init);

        modEventBus.addListener(ImpenRegistry::commonSetup);
        modEventBus.addGenericListener(MenuType.class, this::registerMenus);
        modEventBus.addListener(this::gatherData);

        MinecraftForge.EVENT_BUS.addListener(CapturePlanePart::handleProjectileEvent);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ImpenConfig.SPEC);

        PhaseFieldService.init();
    }

    public void registerMenus(final RegistryEvent.Register<MenuType<?>> event) {
        event.getRegistry().registerAll(
                AtmosphericCrystallizerMenu.TYPE,
                PhaseFieldControllerMenu.TYPE,
                SpatialRiftSpawnerMenu.TYPE,
                SpatialRiftManipulatorMenu.TYPE,
                SpatialRiftCollapserMenu.TYPE);
    }

    public void gatherData(final GatherDataEvent event) {
        final DataGenerator gen = event.getGenerator();
        final var efh = event.getExistingFileHelper();
        gen.addProvider(new ImpenRecipeProvider(gen));
        gen.addProvider(new ImpenLootProvider(gen));
        gen.addProvider(new ImpenPartModelProvider(gen, efh));
        final var blockTagsProvider = new ImpenBlockTagsProvider(gen, MOD_ID, efh);
        gen.addProvider(blockTagsProvider);
        gen.addProvider(new ImpenItemTagsProvider(gen, blockTagsProvider, MOD_ID, efh));
        // Note that ordering is important here - item models refer to block models, so the block models need to be
        // generated first
        gen.addProvider(new ImpenBlockStateProvider(gen, MOD_ID, efh));
        gen.addProvider(new ImpenItemModelProvider(gen, MOD_ID, efh));
    }
}
