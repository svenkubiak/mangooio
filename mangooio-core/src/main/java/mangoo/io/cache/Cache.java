package mangoo.io.cache;

import mangoo.io.enums.Default;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.google.inject.Singleton;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Cache {
    private net.sf.ehcache.Cache cacheInstance;

    public Cache() {
        CacheManager cm = CacheManager.getInstance();
        cm.addCacheIfAbsent(Default.CACHE_NAME.toString());
        this.cacheInstance = cm.getCache(Default.CACHE_NAME.toString());
    }

    /**
     * Adds a value with the given key to the cache
     * 
     * @param key The key to store the value
     * @param value The actual value to store
     */
    public void add(String key, Object value) {
        this.cacheInstance.put(new Element(key, value));
    }
    
    /**
     * Adds a value with the given key to the cache and
     * sets and expiration
     * 
     * @param key The key to store the value
     * @param value The actual value to store
     * @param expiration The time after which the value gets evicted in seconds
     */
    public void add(String key, Object value, int expiration) {
        Element element = new Element(key, value);
        element.setTimeToLive(expiration);

        this.cacheInstance.put(element);
    }

    /**
     * Retrieves a value for a given key from the cache
     * 
     * @param key The key on which the value is stored
     * @return The retrieved value or null if the key is not found
     */
    public Object get(String key) {
        if (this.cacheInstance.get(key) != null) {
            return this.cacheInstance.get(key).getObjectValue();
        }

        return null;
    }
    
    /**
     * Retrieves a value for given key from the cache
     * autocasting it to the required class
     * 
     * @param key The key on which the value is stored
     * @param clazz The class to cast to
     * @return The class to cast to to containing the cache value or null if the key is not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        return (T) this.cacheInstance.get(key).getObjectValue();
    }

    /**
     * Clears the complete cache by removing all entries
     */
    public void clear() {
        this.cacheInstance.removeAll();
    }
}