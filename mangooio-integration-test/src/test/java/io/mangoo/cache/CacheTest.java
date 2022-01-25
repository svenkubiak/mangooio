package io.mangoo.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class CacheTest {
    private static final String TEST_VALUE = "This is a test value for the cache!";

    @Test
    void testAdd() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        
        //when
        cache.put("test", TEST_VALUE);

        //then
        assertThat(cache.get("test"), not(nullValue()));
        assertThat(cache.get("test"), equalTo(TEST_VALUE));
    }

    @Test
    void testClear() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        
        //when
        cache.put("test", TEST_VALUE);
        cache.clear();

        //then
        assertThat(cache.get("test"), equalTo(null));
    }

    @Test
    void testCast() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        
        //when
    	cache.put("test", 1);

    	//then
        assertThat(cache.get("test"), equalTo(1));
    }
    
    @Test
    void testPutAll() {
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
    
    @Test
    void testIncrement() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        
        //when
        AtomicInteger increment = cache.getAndIncrementCounter("increment");
        
        //then
        assertThat(increment.get(), equalTo(1));
        
        //when
        increment = cache.getAndIncrementCounter("increment");
        
        //then
        assertThat(increment.get(), equalTo(2));
    }
    
    @Test
    void testDecrement() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        
        //when
        AtomicInteger decrement = cache.getAndDecrementCounter("decrement");
        
        //then
        assertThat(decrement.get(), equalTo(-1));
        
        //when
        decrement = cache.getAndDecrementCounter("decrement");
        
        //then
        assertThat(decrement.get(), equalTo(-2));
    }
}