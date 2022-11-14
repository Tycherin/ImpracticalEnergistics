package com.tycherin.impen.datagen;

import com.tycherin.impen.ImpenRegistry;
import com.tycherin.impen.util.ImpenIdUtil;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ImpenItemTagsProvider extends ItemTagsProvider {

    public static final TagKey<Item> RIFT_CATALYSTS = ItemTags.create(ImpenIdUtil.makeId("rift_catalysts"));

    public ImpenItemTagsProvider(final DataGenerator gen, final BlockTagsProvider blockTagsProvider, final String modId,
            final ExistingFileHelper efh) {
        super(gen, blockTagsProvider, modId, efh);
    }

    @Override
    protected void addTags() {
        this.tag(RIFT_CATALYSTS)
                .add(ImpenRegistry.RIFT_CATALYST_BLACKSTONE.asItem())
                .add(ImpenRegistry.RIFT_CATALYST_DEEPSLATE.asItem())
                .add(ImpenRegistry.RIFT_CATALYST_DIRT.asItem())
                .add(ImpenRegistry.RIFT_CATALYST_END_STONE.asItem())
                .add(ImpenRegistry.RIFT_CATALYST_NETHERRACK.asItem())
                .add(ImpenRegistry.RIFT_CATALYST_STONE.asItem())
                .add(ImpenRegistry.RIFT_CATALYST_RIFTSTONE.asItem());
    }
}
