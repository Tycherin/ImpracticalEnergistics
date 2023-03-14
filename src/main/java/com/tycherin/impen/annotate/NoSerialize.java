package com.tycherin.impen.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks something that should not be serialized/deserialized.
 * <p>
 * Gson provides the {@code @Expose} annotation which does something similar, but for ImpEn serialization, more fields
 * are serializable than not. So this provides in an inverted way of handling this behavior.
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoSerialize {
}
