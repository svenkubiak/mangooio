package io.mangoo.providers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;

import org.ehcache.core.statistics.CacheStatistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheProvider;
import io.mangoo.core.Application;
import io.mangoo.enums.CacheName;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class CacheProviderTest {
    @Test
    void testGet() {
        //given
        CacheProvider cacheProvider = Application.getInstance(CacheProvider.class);
        
        //then
        assertThat(cacheProvider, not(nullValue()));
        assertThat(cacheProvider.get(), not(nullValue()));
    }
    
    @Test
    void testCaches() {
        //given
        CacheProvider cacheProvider = Application.getInstance(CacheProvider.class);
        
        //when
        CacheName[] names = CacheName.values();
        Map<String, CacheStatistics> cacheStatistics = cacheProvider.getCacheStatistics();
        
        //then
        for (CacheName name : names) {
            assertThat(name, not(nullValue()));
            assertThat(cacheStatistics.get(name.toString()), not(nullValue()));
        }
    }
    
    @Test
    void testGetCache() {
        //given
        CacheProvider cacheProvider = Application.getInstance(CacheProvider.class);
        
        //when
        Cache cache = cacheProvider.getCache(CacheName.WSS);
        Cache cache2 = cacheProvider.getCache(CacheName.WSS.toString());
        
        //then
        assertThat(cache, not(nullValue()));
        assertThat(cache2, not(nullValue()));
        assertThat(cache, equalTo(cache2));
    }
    
    @Test
    void testDafultCache() {
        //given
        CacheProvider cacheProvider = Application.getInstance(CacheProvider.class);
        Cache cache = Application.getInstance(Cache.class);
        
        //when
        Cache defaultCache = cacheProvider.get();
        
        //then
        assertThat(defaultCache, not(nullValue()));
        assertThat(cache, not(nullValue()));
        assertThat(cache, equalTo(defaultCache));
    }
}