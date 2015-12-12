package io.mangoo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.mangoo.interfaces.MangooFilter;

/**
 * Filter annotation for request and controller filter
 *
 * @author svenkubiak
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface FilterWith {
    Class<? extends MangooFilter>[] value();
}