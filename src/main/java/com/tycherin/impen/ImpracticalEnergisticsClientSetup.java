package com.tycherin.impen;

import com.tycherin.impen.client.render.BeamedNetworkLinkRenderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@OnlyIn(Dist.CLIENT)
public class ImpracticalEnergisticsClientSetup {
    
    public static void init() {
        final var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(ImpracticalEnergisticsClientSetup::modelRegistryEvent);
    }
    
    public static void modelRegistryEvent(final ModelRegistryEvent event) {
        BlockEntityRenderers.register(ImpracticalEnergisticsMod.BEAMED_NETWORK_LINK_BE.get(), BeamedNetworkLinkRenderer::new);
    }
}
