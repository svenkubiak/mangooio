package io.mangoo.annotations;

import io.mangoo.enums.Sort;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Indexed {
    /**
     * Specifies that a given field is used as an index in the database
     *
     * @return The filter (either ascending or descending), default ascending
     */
    public Sort sort() default Sort.ASCENDING;
    public boolean unique() default false;
}