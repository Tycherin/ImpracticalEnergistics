package com.tycherin.impen.logic.rift;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.tycherin.impen.recipe.RiftCatalystRecipe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

/**
 * Wrapper class for storing, serializing, and deserializing inputs for a Spatial Rift Manipulator operation. Hides most
 * of the complexity involved in this process, hopefully.
 * 
 * @author Tycherin
 */
public abstract class RiftManipulatorInput {
    protected abstract Map<RiftCatalystRecipe, Integer> getRecipeCounts(final Level level);

    public abstract void writeToNBT(final CompoundTag data, final String key);

    public RiftWeightTracker createWeightTracker(final Level level) {
        return RiftWeightTracker.fromRecipeCounts(getRecipeCounts(level));
    }

    /*
     * Most of the shenanigans in this class are attempting to correct for the fact that recipes aren't available when
     * the level is being loaded, which is when readFromNBT() first gets called. To avoid that, we have to store string
     * IDs of recipes and deserialize them later. However, this adds a bunch of extra complexity (because we do have
     * recipe objects at other times), so rather than let that leak out into the rest of the code, it's all encapsulated
     * inside of this mess.
     */

    public static Optional<RiftManipulatorInput> readFromNBT(final CompoundTag data, final String key) {
        if (!data.contains(key)) {
            return Optional.empty();
        }

        final ListTag listTag = data.getList(key, Tag.TAG_COMPOUND);
        final Map<String, Integer> deserializedMap = listTag.stream()
                .map(tag -> (CompoundTag)tag)
                .collect(Collectors.toMap(tag -> tag.getString("recipe"), tag -> tag.getInt("count")));

        return Optional.of(new RiftManipulatorInputWithoutRecipes(deserializedMap));
    }

    public static RiftManipulatorInput of(final Map<RiftCatalystRecipe, Integer> recipeCounts) {
        return new RiftManipulatorInputWithRecipes(recipeCounts);
    }

    /** Subclass for inputs created when Recipe objects are available */
    private static class RiftManipulatorInputWithRecipes extends RiftManipulatorInput {
        private final Map<RiftCatalystRecipe, Integer> recipeCounts;

        public RiftManipulatorInputWithRecipes(final Map<RiftCatalystRecipe, Integer> recipeCounts) {
            this.recipeCounts = recipeCounts;
        }

        @Override
        protected Map<RiftCatalystRecipe, Integer> getRecipeCounts(final Level ignored) {
            return recipeCounts;
        }

        @Override
        public void writeToNBT(final CompoundTag data, final String key) {
            if (recipeCounts.isEmpty()) {
                return;
            }

            final var listTag = new ListTag();
            recipeCounts.forEach((recipe, count) -> {
                var tag = new CompoundTag();
                tag.putString("recipe", recipe.getId().toString());
                tag.putInt("count", count);
                listTag.add(tag);
            });
            data.put(key, listTag);
        }
    }

    /** Subclass for inputs created when Recipe objects are NOT available */
    private static class RiftManipulatorInputWithoutRecipes extends RiftManipulatorInput {
        private static final Logger LOGGER = LogUtils.getLogger();

        private final Map<String, Integer> recipeNamesWithCounts;

        public RiftManipulatorInputWithoutRecipes(final Map<String, Integer> recipeNamesWithoutCounts) {
            this.recipeNamesWithCounts = recipeNamesWithoutCounts;
        }

        @Override
        protected Map<RiftCatalystRecipe, Integer> getRecipeCounts(final Level level) {
            if (level == null) {
                throw new IllegalArgumentException("Level cannot be null");
            }

            final RecipeManager recipeManager = level.getRecipeManager();
            final Map<RiftCatalystRecipe, Integer> recipeCountMap = new HashMap<>();
            recipeNamesWithCounts.forEach((recipeName, count) -> {
                final Optional<RiftCatalystRecipe> recipeOpt = recipeManager.byKey(new ResourceLocation(recipeName))
                        .filter(recipe -> recipe instanceof RiftCatalystRecipe)
                        .map(recipe -> (RiftCatalystRecipe)recipe);
                if (recipeOpt.isPresent()) {
                    recipeCountMap.put(recipeOpt.get(), count);
                }
                else {
                    LOGGER.warn("No matching recipe found for " + recipeName + "; entry will be discarded");
                }
            });

            return recipeCountMap;
        }

        @Override
        public void writeToNBT(final CompoundTag data, final String key) {
            if (recipeNamesWithCounts.isEmpty()) {
                return;
            }

            final var listTag = new ListTag();
            recipeNamesWithCounts.forEach((recipeName, count) -> {
                var tag = new CompoundTag();
                tag.putString("recipe", recipeName);
                tag.putInt("count", count);
                listTag.add(tag);
            });
            data.put(key, listTag);
        }
    }
}
