package io.mangoo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Collection {
    /**
     * Specifies a specific class as a MongoDB entity
     *
     * @return The name of the collection in the database
     */
    public String name(); 
}