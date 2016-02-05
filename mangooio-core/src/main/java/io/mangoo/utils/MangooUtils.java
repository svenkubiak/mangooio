package io.mangoo.utils;

import java.util.Arrays;
import java.util.List;

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
    private static final List<String> ADMINISTRSTIVE_URLS = Arrays
            .asList("@cache", "@metrics", "@config", "@routes", "@health", "@scheduler", "@memory", "@system");
    
    /**
     * @return An instance of the internal mangoo I/O cache
     */
    public static Cache getInternalCache() {
        if (cache == null) {
            if (Default.CACHE_CLASS.toString().equals(CONFIG.getCacheClass())) {
                cache = new GuavaCache();                
            } else {
                cache = new HazlecastCache();
            }
        }
        
        return cache;
    }
    
    /**
     * @return A list of all administrative URLs
     */
    public static List<String> getAdministrativeURLs() {
        return ADMINISTRSTIVE_URLS;
    }
}