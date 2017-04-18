package io.mangoo.enums;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author svenkubiak
 *
 */
public enum Binding {
    AUTHENTICATION("io.mangoo.routing.bindings.Authentication"),
    BODY("io.mangoo.routing.bindings.Body"),
    DOUBLE("java.lang.Double"),
    DOUBLE_PRIMITIVE("double"),    
    FLASH("io.mangoo.routing.bindings.Flash"),
    FLOAT("java.lang.Float"),
    FLOAT_PRIMITIVE("float"),
    FORM("io.mangoo.routing.bindings.Form"),
    INT_PRIMITIVE("int"),
    INTEGER("java.lang.Integer"),
    LOCALDATE("java.time.LocalDate"),
    LOCALDATETIME("java.time.LocalDateTime"),
    LONG("java.lang.Long"),
    LONG_PRIMITIVE("long"),
    OPTIONAL("java.util.Optional"),
    REQUEST("io.mangoo.routing.bindings.Request"),
    SESSION("io.mangoo.routing.bindings.Session"),
    STRING("java.lang.String"),
    UNDEFINED("undefined");

    private final String value;
    private static Map<String, Binding> values = new ConcurrentHashMap<>();

    Binding (String value) {
        this.value = value;
    }
    
    public static Binding fromString(String value) {
        Objects.requireNonNull(value, Required.BINDING.toString());
        if (values.isEmpty()) {
            for (Binding binding : Binding.values()) {
                values.put(binding.toString().toLowerCase(Locale.ENGLISH), binding);
            }  
        }

        return values.get(value.toLowerCase(Locale.ENGLISH));
    }
    
    @Override
    public String toString() {
        return this.value;
    }
}