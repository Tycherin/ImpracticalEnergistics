package com.tycherin.impen;

import com.tycherin.impen.client.gui.ImaginarySpaceManipulatorMenu;
import com.tycherin.impen.client.gui.ImaginarySpaceManipulatorScreen;
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
        BlockEntityRenderers.register(ImpracticalEnergisticsMod.BEAMED_NETWORK_LINK_BE.get(), BeamedNetworkLinkRenderer::new);
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
    }
}
