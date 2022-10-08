package com.tycherin.impen;

import com.tycherin.impen.client.gui.ImaginarySpaceManipulatorMenu;
import com.tycherin.impen.client.gui.ImaginarySpaceManipulatorScreen;
import com.tycherin.impen.client.gui.ImaginarySpaceStabilizerMenu;
import com.tycherin.impen.client.gui.ImaginarySpaceStabilizerScreen;
import com.tycherin.impen.client.gui.SpatialCrystallizerMenu;
import com.tycherin.impen.client.gui.SpatialCrystallizerScreen;
import com.tycherin.impen.client.render.BeamedNetworkLinkRenderer;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@OnlyIn(Dist.CLIENT)
public class ImpracticalEnergisticsClientSetup {

    public static void init() {
        final var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(ImpracticalEnergisticsClientSetup::modelRegistryEvent);
        modEventBus.addListener(ImpracticalEnergisticsClientSetup::clientSetupEvent);
    }

    public static void modelRegistryEvent(final ModelRegistryEvent event) {
        BlockEntityRenderers.register(ImpracticalEnergisticsMod.BEAMED_NETWORK_LINK_BE.get(),
                BeamedNetworkLinkRenderer::new);
    }

    public static void clientSetupEvent(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ImpracticalEnergisticsClientSetup.setupScreens();
        });
    }

    public static void setupScreens() {
        MenuScreens.<ImaginarySpaceManipulatorMenu, ImaginarySpaceManipulatorScreen>register(
                ImaginarySpaceManipulatorMenu.TYPE,
                (menu, playerInv, title) -> {
                    final ScreenStyle style = StyleManager.loadStyleDoc("/screens/imaginary_space_manipulator.json");
                    return new ImaginarySpaceManipulatorScreen(menu, playerInv, title, style);
                });

        MenuScreens.<ImaginarySpaceStabilizerMenu, ImaginarySpaceStabilizerScreen>register(
                ImaginarySpaceStabilizerMenu.TYPE,
                (menu, playerInv, title) -> {
                    final ScreenStyle style = StyleManager.loadStyleDoc("/screens/imaginary_space_stabilizer.json");
                    return new ImaginarySpaceStabilizerScreen(menu, playerInv, title, style);
                });
        MenuScreens.<SpatialCrystallizerMenu, SpatialCrystallizerScreen>register(
                SpatialCrystallizerMenu.TYPE,
                (menu, playerInv, title) -> {
                    final ScreenStyle style = StyleManager.loadStyleDoc("/screens/spatial_crystallizer.json");
                    return new SpatialCrystallizerScreen(menu, playerInv, title, style);
                });
    }
}
