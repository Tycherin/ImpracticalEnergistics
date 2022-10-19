package com.tycherin.impen;

import com.tycherin.impen.client.gui.ImaginarySpaceManipulatorMenu;
import com.tycherin.impen.client.gui.ImaginarySpaceStabilizerMenu;
import com.tycherin.impen.client.gui.PossibilityDisintegratorMenu;
import com.tycherin.impen.client.gui.SpatialCrystallizerMenu;
import com.tycherin.impen.config.ImpenConfig;
import com.tycherin.impen.datagen.ImpenRecipeProvider;
import com.tycherin.impen.logic.ism.IsmService;
import com.tycherin.impen.part.CapturePlanePart;

import appeng.api.parts.PartModels;
import appeng.items.parts.PartModelsHelper;
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
        
        IsmService.init();

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ImpracticalEnergisticsClientSetup::init);

        modEventBus.addListener(ImpenRegistry::commonSetup);
        modEventBus.addGenericListener(MenuType.class, this::registerMenus);
        modEventBus.addListener(this::gatherData);

        MinecraftForge.EVENT_BUS.addListener(CapturePlanePart::handleProjectileEvent);

        PartModels.registerModels(PartModelsHelper.createModels(CapturePlanePart.class));
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ImpenConfig.SPEC);
    }

    public void registerMenus(final RegistryEvent.Register<MenuType<?>> event) {
        event.getRegistry().registerAll(
                ImaginarySpaceManipulatorMenu.TYPE,
                ImaginarySpaceStabilizerMenu.TYPE,
                SpatialCrystallizerMenu.TYPE,
                PossibilityDisintegratorMenu.TYPE);
    }
    
    public void gatherData(final GatherDataEvent event) {
        final DataGenerator gen = event.getGenerator();
        gen.addProvider(new ImpenRecipeProvider(gen));
    }
}
