package io.mangoo.utils;

import io.mangoo.constants.Required;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class PersistenceUtils {
    private static final Map<String, String> COLLECTIONS = new ConcurrentHashMap<>(16, 0.9f, 1);

    private PersistenceUtils(){
    }

    public static void addCollection(String key, String value) {
        Argument.requireNonBlank(key, Required.KEY);
        Argument.requireNonBlank(value, Required.VALUE);

        COLLECTIONS.put(key, value);
    }

    public static String getCollectionName(Class<?> clazz) {
        Objects.requireNonNull(clazz, Required.CLASS);

        return COLLECTIONS.get(clazz.getName());
    }
}
