package io.mangoo.utils;

import io.mangoo.constants.Required;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class PersistenceUtils {
    private static final Map<String, String> COLLECTIONS = new ConcurrentHashMap<>(16, 0.9f, 1);

    private PersistenceUtils(){
    }

    /**
     * Adds a collection to be retrieved for the datastore
     *
     * @param key The key of the collection
     * @param value The value/name of the collection
     */
    public static void addCollection(String key, String value) {
        Arguments.requireNonBlank(key, Required.KEY);
        Arguments.requireNonBlank(value, Required.VALUE);

        COLLECTIONS.put(key, value);
    }

    /**
     * Returns a collection name based on the given class
     *
     * @param clazz The class to lookup
     * @return The name of the collection
     */
    public static String getCollectionName(Class<?> clazz) {
        Objects.requireNonNull(clazz, Required.CLASS);

        return COLLECTIONS.get(clazz.getName());
    }
}
