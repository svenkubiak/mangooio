package io.mangoo.routing.annotations;

import io.mangoo.interfaces.filters.PerRequestFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface FilterWith {
    Class<? extends PerRequestFilter>[] value();
}