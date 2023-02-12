package com.tycherin.impen;

import java.util.Arrays;
import java.util.function.Supplier;

import com.tycherin.impen.blockentity.beam.BeamNetworkAmplifierBlockEntity;
import com.tycherin.impen.blockentity.beam.BeamNetworkEmitterBlockEntity;
import com.tycherin.impen.blockentity.beam.BeamNetworkMirrorBlockEntity;
import com.tycherin.impen.blockentity.beam.BeamNetworkSplitterBlockEntity;
import com.tycherin.impen.client.gui.AtmosphericCrystallizerMenu;
import com.tycherin.impen.client.gui.AtmosphericCrystallizerScreen;
import com.tycherin.impen.client.gui.PossibilityDisintegratorMenu;
import com.tycherin.impen.client.gui.PossibilityDisintegratorScreen;
import com.tycherin.impen.client.gui.SpatialRiftCollapserMenu;
import com.tycherin.impen.client.gui.SpatialRiftCollapserScreen;
import com.tycherin.impen.client.gui.SpatialRiftManipulatorMenu;
import com.tycherin.impen.client.gui.SpatialRiftManipulatorScreen;
import com.tycherin.impen.client.gui.SpatialRiftSpawnerMenu;
import com.tycherin.impen.client.gui.SpatialRiftSpawnerScreen;
import com.tycherin.impen.client.particle.DisintegratorDamageParticle;
import com.tycherin.impen.client.particle.DisintegratorLockParticle;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.client.render.SimpleModelLoader;
import appeng.core.AppEng;
import appeng.parts.automation.PlaneModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
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
        modEventBus.addListener(ImpracticalEnergisticsClientSetup::registerParticleProviders);
    }

    public static void modelRegistryEvent(final ModelRegistryEvent event) {
        BlockEntityRenderers.register(
                ImpenRegistry.BEAM_NETWORK_AMPLIFIER.blockEntity(),
                BeamNetworkAmplifierBlockEntity.Renderer::new);
        BlockEntityRenderers.register(
                ImpenRegistry.BEAM_NETWORK_EMITTER.blockEntity(),
                BeamNetworkEmitterBlockEntity.Renderer::new);
        BlockEntityRenderers.register(
                ImpenRegistry.BEAM_NETWORK_MIRROR.blockEntity(),
                BeamNetworkMirrorBlockEntity.Renderer::new);
        BlockEntityRenderers.register(
                ImpenRegistry.BEAM_NETWORK_SPLITTER.blockEntity(),
                BeamNetworkSplitterBlockEntity.Renderer::new);

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
        MenuScreens.<SpatialRiftSpawnerMenu, SpatialRiftSpawnerScreen>register(
                SpatialRiftSpawnerMenu.TYPE,
                (menu, playerInv, title) -> {
                    final ScreenStyle style = StyleManager.loadStyleDoc("/screens/spatial_rift_spawner.json");
                    return new SpatialRiftSpawnerScreen(menu, playerInv, title, style);
                });
        MenuScreens.<SpatialRiftManipulatorMenu, SpatialRiftManipulatorScreen>register(
                SpatialRiftManipulatorMenu.TYPE,
                (menu, playerInv, title) -> {
                    final ScreenStyle style = StyleManager.loadStyleDoc("/screens/spatial_rift_manipulator.json");
                    return new SpatialRiftManipulatorScreen(menu, playerInv, title, style);
                });
        MenuScreens.<SpatialRiftCollapserMenu, SpatialRiftCollapserScreen>register(
                SpatialRiftCollapserMenu.TYPE,
                (menu, playerInv, title) -> {
                    final ScreenStyle style = StyleManager.loadStyleDoc("/screens/spatial_rift_collapser.json");
                    return new SpatialRiftCollapserScreen(menu, playerInv, title, style);
                });
    }

    public static void setupBlockEntityRepresentations() {
        // AE2 needs to be told which item icon to use when representing each BE in the network display. This needs to
        // happen before BEs are instantiated, or else AE2 will explode.
        //
        // Weirdly, this needs to be done for anything extending AEBaseBlockEntity, not just ones that are networked.
        Arrays.asList(
                ImpenRegistry.ATMOSPHERIC_CRYSTALLIZER,
                ImpenRegistry.EJECTION_DRIVE,
                ImpenRegistry.POSSIBILITY_DISINTEGRATOR,
                ImpenRegistry.SPATIAL_RIFT_SPAWNER,
                ImpenRegistry.SPATIAL_RIFT_MANIPULATOR,
                ImpenRegistry.SPATIAL_RIFT_COLLAPSER,
                ImpenRegistry.BEAM_NETWORK_AMPLIFIER,
                ImpenRegistry.BEAM_NETWORK_EMITTER,
                ImpenRegistry.BEAM_NETWORK_MIRROR,
                ImpenRegistry.BEAM_NETWORK_RECEIVER,
                ImpenRegistry.BEAM_NETWORK_SPLITTER)
                .forEach(machineDef -> {
                    AEBaseBlockEntity.registerBlockEntityItem(machineDef.blockEntity(), machineDef.item());
                });
    }

    public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
    }

    @SuppressWarnings("resource")
    public static void registerParticleProviders(final ParticleFactoryRegisterEvent event) {
        // The event doesn't actually do anything here, it's just a timing hint
        final var particleEngine = Minecraft.getInstance().particleEngine;
        particleEngine.register(ImpenRegistry.DISINTEGRATOR_DAMAGE_PARTICLE.get(),
                new DisintegratorDamageParticle.ProviderFactory());
        particleEngine.register(ImpenRegistry.DISINTEGRATOR_LOCK_PARTICLE.get(),
                new DisintegratorLockParticle.ProviderFactory());
    }
}
