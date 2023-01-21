package com.tycherin.impen.datagen;

import java.util.Set;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.ImpenRegistry.BlockLike;
import com.tycherin.impen.ImpenRegistry.MachineDefinition;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ImpenBlockModelProvider extends BlockModelProvider {

    private static final Set<BlockLike> BLOCKS_WITH_CUSTOM_MODEL = Set.of();

    public ImpenBlockModelProvider(final DataGenerator generator, final String modid,
            final ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ImpenRegistry.getRegisteredBlocks().stream()
                .filter(blockLike -> !(blockLike instanceof MachineDefinition))
                .filter(blockLike -> !(BLOCKS_WITH_CUSTOM_MODEL.contains(blockLike)))
                .forEach(blockLike -> basicBlock(blockLike.asBlock()));
    }

    private void basicBlock(final Block block) {
        getBuilder(block.getRegistryName().toString())
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/cube_all"))
                .texture("all", new ResourceLocation(block.getRegistryName().getNamespace(),
                        "block/" + block.getRegistryName().getPath()));
    }
}
