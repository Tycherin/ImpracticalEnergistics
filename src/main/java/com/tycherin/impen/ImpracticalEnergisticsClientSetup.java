package com.tycherin.impen;

import com.tycherin.impen.client.gui.SpatialRiftManipulatorMenu;
import com.tycherin.impen.client.gui.SpatialRiftManipulatorScreen;
import com.tycherin.impen.client.gui.SpatialRiftStabilizerMenu;
import com.tycherin.impen.client.gui.SpatialRiftStabilizerScreen;
import com.tycherin.impen.client.gui.PossibilityDisintegratorMenu;
import com.tycherin.impen.client.gui.PossibilityDisintegratorScreen;
import com.tycherin.impen.client.gui.AtmosphericCrystallizerMenu;
import com.tycherin.impen.client.gui.AtmosphericCrystallizerScreen;
import com.tycherin.impen.client.render.BeamedNetworkLinkRenderer;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@OnlyIn(Dist.CLIENT)
public class ImpracticalEnergisticsClientSetup {

    public static void init() {
        final var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(ImpracticalEnergisticsClientSetup::modelRegistryEvent);
        modEventBus.addListener(ImpracticalEnergisticsClientSetup::clientSetupEvent);
        modEventBus.addListener(ImpracticalEnergisticsClientSetup::registerEntityRenderers);
    }

    public static void modelRegistryEvent(final ModelRegistryEvent event) {
        BlockEntityRenderers.register(ImpenRegistry.BEAMED_NETWORK_LINK_BE.get(),
                BeamedNetworkLinkRenderer::new);
    }

    public static void clientSetupEvent(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ImpracticalEnergisticsClientSetup.setupScreens();

            ItemBlockRenderTypes.setRenderLayer(ImpenRegistry.PLANTABLE_CERTUS_BLOCK.get(),
                    RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ImpenRegistry.PLANTABLE_FLUIX_BLOCK.get(),
                    RenderType.cutout());
        });
    }

    public static void setupScreens() {
        MenuScreens.<SpatialRiftManipulatorMenu, SpatialRiftManipulatorScreen>register(
                SpatialRiftManipulatorMenu.TYPE,
                (menu, playerInv, title) -> {
                    final ScreenStyle style = StyleManager.loadStyleDoc("/screens/imaginary_space_manipulator.json");
                    return new SpatialRiftManipulatorScreen(menu, playerInv, title, style);
                });
        MenuScreens.<SpatialRiftStabilizerMenu, SpatialRiftStabilizerScreen>register(
                SpatialRiftStabilizerMenu.TYPE,
                (menu, playerInv, title) -> {
                    final ScreenStyle style = StyleManager.loadStyleDoc("/screens/imaginary_space_stabilizer.json");
                    return new SpatialRiftStabilizerScreen(menu, playerInv, title, style);
                });
        MenuScreens.<AtmosphericCrystallizerMenu, AtmosphericCrystallizerScreen>register(
                AtmosphericCrystallizerMenu.TYPE,
                (menu, playerInv, title) -> {
                    final ScreenStyle style = StyleManager.loadStyleDoc("/screens/spatial_crystallizer.json");
                    return new AtmosphericCrystallizerScreen(menu, playerInv, title, style);
                });
        MenuScreens.<PossibilityDisintegratorMenu, PossibilityDisintegratorScreen>register(
                PossibilityDisintegratorMenu.TYPE,
                (menu, playerInv, title) -> {
                    final ScreenStyle style = StyleManager.loadStyleDoc("/screens/possibility_disintegrator.json");
                    return new PossibilityDisintegratorScreen(menu, playerInv, title, style);
                });
    }

    public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ImpenRegistry.RIFT_PRISM_ENTITY.get(), ItemEntityRenderer::new);
        event.registerEntityRenderer(ImpenRegistry.STABILIZED_RIFT_PRISM_ENTITY.get(),
                ItemEntityRenderer::new);
    }
}
