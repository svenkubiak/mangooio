package io.mangoo.enums;

/**
 * 
 * @author svenkubiak
 *
 */
public enum CacheType {
    DEFAULT("io.mangoo.cache.Cache"),
    MEMCACHE("io.mangoo.cache.CacheMemcache"),
    HAZELCAST("io.mangoo.cache.CacheHazelcast"),
    REDIS("io.mangoo.cache.CacheRedis");

    private final String value;

    CacheType (String value) {
        this.value = value;
    }

    public String getClassName() {
        return this.value;
    }
}
