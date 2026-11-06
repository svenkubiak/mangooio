package io.mangoo.cache;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.function.Function;

public interface Cache {
    void put(String key, Object value);
    
    void put(String key, Object value, int expires, TemporalUnit temporalUnit);

    void put(String key, Object value, LocalDateTime expires);

    void remove(String key);

    void clear();

    <T> T get(String key);

    <T> T get(String key, Function<String, Object> fallback);

    void putAll(Map<String, Object> map);
    
    long increment(String key);

    long decrement(String key);

    long getCounter(String key);

    void resetCounter(String key);

    <T> T get(String key, int expires, TemporalUnit temporalUnit, Function<String, Object> fallback);

    Map<String, Object> getAll(String... keys);
}