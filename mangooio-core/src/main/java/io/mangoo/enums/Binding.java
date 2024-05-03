package io.mangoo.enums;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public enum Binding {
    AUTHENTICATION("io.mangoo.routing.bindings.Authentication"),
    DOUBLE("java.lang.Double"),
    DOUBLE_PRIMITIVE("double"),
    FLASH("io.mangoo.routing.bindings.Flash"),
    FLOAT("java.lang.Float"),
    FLOAT_PRIMITIVE("float"),
    FORM("io.mangoo.routing.bindings.Form"),
    INT_PRIMITIVE("int"),
    INTEGER("java.lang.Integer"),
    LOCAL_DATE("java.time.LocalDate"),
    LOCAL_DATE_TIME("java.time.LocalDateTime"),
    LONG("java.lang.Long"),
    LONG_PRIMITIVE("long"),
    MESSAGES("io.mangoo.i18n.Messages"),
    OPTIONAL("java.util.Optional"),
    REQUEST("io.mangoo.routing.bindings.Request"),
    SESSION("io.mangoo.routing.bindings.Session"),
    STRING("java.lang.String"),
    UNDEFINED("undefined");

    private final String value;
    private final static Map<String, Binding> values;
    static {
        Map<String, Binding> bindings = Maps.newHashMapWithExpectedSize(Binding.values().length);
        for (Binding binding : Binding.values()) {
            bindings.put(binding.toString().toLowerCase(Locale.ENGLISH), binding);
        }
        
        values = Collections.unmodifiableMap(bindings);
    }
    
    Binding (String value) {
        this.value = value;
    }
    
    public static Binding fromString(String value) {
        return values.get(value.toLowerCase(Locale.ENGLISH));
    }
    
    @Override
    public String toString() {
        return this.value;
    }
}