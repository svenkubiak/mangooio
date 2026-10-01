package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation without meaning, used to place a foreign annotation next to the
 * persistence annotations of a model. Its package sorts before io.mangoo, as
 * annotations are reported ordered by their fully qualified name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Label {
    String value();
}
