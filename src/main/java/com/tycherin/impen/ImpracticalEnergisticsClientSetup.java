package com.tycherin.impen;

import java.util.Arrays;
import java.util.function.Supplier;

import com.tycherin.impen.client.gui.AtmosphericCrystallizerMenu;
import com.tycherin.impen.client.gui.AtmosphericCrystallizerScreen;
import com.tycherin.impen.client.gui.PossibilityDisintegratorMenu;
import com.tycherin.impen.client.gui.PossibilityDisintegratorScreen;
import com.tycherin.impen.client.render.BeamedNetworkLinkRenderer;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.client.render.SimpleModelLoader;
import appeng.core.AppEng;
import appeng.parts.automation.PlaneModel;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;
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
        BlockEntityRenderers.register(ImpenRegistry.BEAMED_NETWORK_LINK.blockEntity(),
                BeamedNetworkLinkRenderer::new);

        addPlaneModel("part/capture_plane", "part/capture_plane");
        addPlaneModel("part/capture_plane_on", "part/capture_plane_on");
    }

    private static void addPlaneModel(String planeName, String frontTexture) {
        ResourceLocation frontTextureId = AppEng.makeId(frontTexture);
        ResourceLocation sidesTextureId = AppEng.makeId("part/plane_sides");
        ResourceLocation backTextureId = AppEng.makeId("part/transition_plane_back");
        addBuiltInModel(planeName, () -> new PlaneModel(frontTextureId, sidesTextureId, backTextureId));
    }

    private static <T extends IModelGeometry<T>> void addBuiltInModel(String id, Supplier<T> modelFactory) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, id),
                new SimpleModelLoader<>(modelFactory));
    }

    public static void clientSetupEvent(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ImpracticalEnergisticsClientSetup.setupScreens();
            ImpracticalEnergisticsClientSetup.setupBlockEntityRepresentations();

            ItemBlockRenderTypes.setRenderLayer(ImpenRegistry.RIFT_GLASS.block(),
                    RenderType.translucent());
        });
    }

    public static void setupScreens() {
        MenuScreens.<AtmosphericCrystallizerMenu, AtmosphericCrystallizerScreen>register(
                AtmosphericCrystallizerMenu.TYPE,
                (menu, playerInv, title) -> {
                    final ScreenStyle style = StyleManager.loadStyleDoc("/screens/atmospheric_crystallizer.json");
                    return new AtmosphericCrystallizerScreen(menu, playerInv, title, style);
                });
        MenuScreens.<PossibilityDisintegratorMenu, PossibilityDisintegratorScreen>register(
                PossibilityDisintegratorMenu.TYPE,
                (menu, playerInv, title) -> {
                    final ScreenStyle style = StyleManager.loadStyleDoc("/screens/possibility_disintegrator.json");
                    return new PossibilityDisintegratorScreen(menu, playerInv, title, style);
                });
    }
    
    public static void setupBlockEntityRepresentations() {
        // AE2 needs to be told which item icon to use when representing each BE in the network display. This needs to
        // happen before BEs are instantiated, or else AE2 will explode.
        Arrays.asList(
                // TODO Add everything else
                ImpenRegistry.SPATIAL_RIFT_SPAWNER,
                ImpenRegistry.SPATIAL_RIFT_STABILIZER)
                .forEach(machineDef -> {
                    AEBaseBlockEntity.registerBlockEntityItem(machineDef.blockEntity(), machineDef.item());
                });
    }

    public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ImpenRegistry.RIFT_PRISM.entity(), ItemEntityRenderer::new);
        event.registerEntityRenderer(ImpenRegistry.STABILIZED_RIFT_PRISM.entity(),
                ItemEntityRenderer::new);
    }
}
