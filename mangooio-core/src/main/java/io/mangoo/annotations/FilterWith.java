package io.mangoo.annotations;

import io.mangoo.interfaces.filters.PerRequestFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface FilterWith {
    /**
     * Specifies that the given filter is used before the method or class is called
     *
     * @return Tbe name of the filter class
     */
    Class<? extends PerRequestFilter>[] value();
}