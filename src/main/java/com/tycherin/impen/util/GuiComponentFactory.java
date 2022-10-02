package com.tycherin.impen.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class GuiComponentFactory {

    private static final String BASE_NAMESPACE = "gui.impracticalenergistics";

    private final String namespace;

    public GuiComponentFactory(final String localNamespace) {
        this.namespace = BASE_NAMESPACE + "." + localNamespace;
    }

    public GuiComponentWrapper build(final String key) {
        return new GuiComponentWrapper(this.namespace + "." + key);
    }

    public static final class GuiComponentWrapper {
        private final String translationKey;

        public GuiComponentWrapper(final String translationKey) {
            this.translationKey = translationKey;
        }

        public Component text() {
            return new TranslatableComponent(translationKey);
        }

        public Component text(final Object... args) {
            return new TranslatableComponent(translationKey, args);
        }
    }
}
