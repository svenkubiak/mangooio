package io.mangoo.utils;

import org.apache.logging.log4j.util.Strings;

import java.util.HashMap;
import java.util.Map;

public final class ConfigUtils {

    private ConfigUtils() {}

    @SuppressWarnings("unchecked")
    public static void mergeMaps(Map<String, Object> baseMap, Map<String, Object> overrideMap) {
        overrideMap.forEach((key, value) -> {
            if (value instanceof Map && baseMap.get(key) instanceof Map) {
                mergeMaps((Map<String, Object>) baseMap.get(key), (Map<String, Object>) value);
            } else {
                baseMap.put(key, value);
            }
        });
    }

    public static Map<String, String> flattenMap(Map<String, Object> map) {
        Map<String, String> flatMap = new HashMap<>();
        flattenMapHelper(map, "", flatMap);
        return flatMap;
    }

    @SuppressWarnings("unchecked")
    public static void flattenMapHelper(Map<String, Object> map, String prefix, Map<String, String> flatMap) {
        map.forEach((key, value) -> {
            String newKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (value instanceof Map) {
                flattenMapHelper((Map<String, Object>) value, newKey, flatMap);
            } else {
                flatMap.put(newKey, value != null ? value.toString() : Strings.EMPTY);
            }
        });
    }
}
