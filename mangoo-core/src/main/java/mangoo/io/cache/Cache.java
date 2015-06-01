package mangoo.io.cache;

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
        cm.addCacheIfAbsent("mangoo");
        this.cacheInstance = cm.getCache("mangoo");
    }

    public void add(String key, Object value) {
        this.cacheInstance.put(new Element(key, value));
    }

    public Object get(String key) {
        if (this.cacheInstance.get(key) != null) {
            return this.cacheInstance.get(key).getObjectValue();
        }

        return null;
    }

    public void clear() {
        this.cacheInstance.removeAll();
    }
}