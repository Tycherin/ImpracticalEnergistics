package com.tycherin.impen.datagen;

import java.util.function.BiFunction;

import appeng.core.AppEng;
import appeng.datagen.providers.models.PartModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ImpenPartModelProvider extends PartModelProvider {

    public ImpenPartModelProvider(final DataGenerator generator, final ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        addBuiltInModel("part/capture_plane");
        addBuiltInModel("part/capture_plane_on");
        addBuiltInModel("part/phase_field_emitter");
        addBuiltInModel("part/phase_field_emitter_on");
    }

    private void addBuiltInModel(final String name) {
        getBuilder(name).customLoader(customLoader(name));
    }

    private BiFunction<BlockModelBuilder, ExistingFileHelper, CustomLoaderBuilder<BlockModelBuilder>> customLoader(
            final String name) {
        return (modelBuilder, fileHelper) -> new CustomLoaderBuilder<>(AppEng.makeId(name), modelBuilder, fileHelper) {
        };
    }
}
