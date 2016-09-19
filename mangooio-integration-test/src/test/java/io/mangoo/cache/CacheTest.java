package io.mangoo.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.mangoo.core.Application;

/**
 * 
 * @author svenkubiak
 *
 */
public class CacheTest {
    private static final String TEST_VALUE = "This is a test value for the cache!";

    @Test
    public void testAdd() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        
        //when
        cache.put("test", TEST_VALUE);

        //then
        assertThat(cache.get("test"), not(nullValue()));
        assertThat(cache.get("test"), equalTo(TEST_VALUE));
    }

    @Test
    public void testClear() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        
        //when
        cache.put("test", TEST_VALUE);
        cache.clear();

        //then
        assertThat(cache.get("test"), equalTo(null));
    }

    @Test
    public void testCast() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        
        //when
    	cache.put("test", 1);

    	//then
        assertThat(cache.get("test"), equalTo(1));
    }
    
    @Test
    public void testPutAll() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        
        //when
        Map<String, Object> map = new HashMap<>();
        map.put("test", TEST_VALUE);
        map.put("test2", 1);
        cache.putAll(map);
        
        //then
        assertThat(cache.get("test"), equalTo(TEST_VALUE));
        assertThat(cache.get("test2"), equalTo(1));
    }
}