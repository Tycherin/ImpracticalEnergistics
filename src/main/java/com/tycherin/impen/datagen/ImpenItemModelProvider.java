package com.tycherin.impen.datagen;

import java.util.Set;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpracticalEnergisticsMod;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ImpenItemModelProvider extends ItemModelProvider {

    private static final Set<ItemLike> ITEMS_WITH_CUSTOM_MODEL = Set.of(
            ImpenRegistry.CAPTURE_PLANE_ITEM,
            ImpenRegistry.PHASE_FIELD_EMITTER_ITEM);
    
    public ImpenItemModelProvider(final DataGenerator generator, final String modid,
            final ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ImpenRegistry.getRegisteredItems().stream()
            .filter(this::shouldGenerateModel)
            .forEach(itemLike -> {
                    if (itemLike instanceof ImpenRegistry.BlockLike blockLike
                            // Crop seeds don't have the same name as the parent block
                            && !(itemLike instanceof ImpenRegistry.PlantDefinition)) {
                        // There is a more elegant way of doing this, but here we are, doing string manipulation
                        final var modelFile = new ResourceLocation(String.format("%s:block/%s",
                                ImpracticalEnergisticsMod.MOD_ID, blockLike.asBlock().getRegistryName().getPath()));
                        getBuilder(itemLike.asItem().toString())
                                .parent(new ModelFile.ExistingModelFile(modelFile, existingFileHelper));
                    }
                else {
                    basicItem(itemLike.asItem());
                }
            });
    }
    
    private boolean shouldGenerateModel(final ItemLike itemLike) {
        return !ITEMS_WITH_CUSTOM_MODEL.contains(itemLike);
    }
}
