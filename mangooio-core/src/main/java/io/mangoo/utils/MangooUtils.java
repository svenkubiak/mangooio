package io.mangoo.utils;

import io.mangoo.cache.Cache;
import io.mangoo.cache.GuavaCache;
import io.mangoo.cache.HazlecastCache;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;

/**
 * 
 * @author svenkubiak
 *
 */
public final class MangooUtils {
    private static final Config CONFIG = Application.getConfig();
    private static Cache cache;
    
    public static Cache getCache() {
        if (cache == null) {
            if (Default.CACHE_CLASS.equals(CONFIG.getCacheClass())) {
                cache = new GuavaCache();                
            } else {
                cache = new HazlecastCache();
            }
        }
        
        return cache;
    }
}