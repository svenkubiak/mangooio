package io.mangoo.enums;

/**
 *
 * @author svenkubiak
 *
 */
public enum Binding {
    AUTHENTICATION("io.mangoo.routing.bindings.Authentication"),
    BODY("io.mangoo.routing.bindings.Body"),
    DOUBLE("java.lang.Double"),
    OPTIONAL("java.util.Optional"),    
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
    REQUEST("io.mangoo.routing.bindings.Request"),
    SESSION("io.mangoo.routing.bindings.Session"),
    STRING("java.lang.String"),
    UNDEFINED("undefined");

    private final String value;

    Binding (String value) {
        this.value = value;
    }

    public static Binding fromString(String value) {
        for (Binding binding : Binding.values()) {
            if (binding.toString().equalsIgnoreCase(value)) {
                return binding;
            }
        }

        return null;
    }
    
    @Override
    public String toString() {
        return this.value;
    }
}