package com.tycherin.impen.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ImpenItemTagsProvider extends ItemTagsProvider {

    public ImpenItemTagsProvider(final DataGenerator gen, final BlockTagsProvider blockTagsProvider, final String modId,
            final ExistingFileHelper efh) {
        super(gen, blockTagsProvider, modId, efh);
    }

    @Override
    protected void addTags() {
    }
}
