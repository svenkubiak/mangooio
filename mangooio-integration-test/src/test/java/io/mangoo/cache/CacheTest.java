package io.mangoo.cache;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class CacheTest {
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
    void testGetWithFallback() {
        //given
        Cache cache = Application.getInstance(Cache.class);

        //then
        assertThat(cache.get("foo"), equalTo(null));

        //when
        String value = cache.get("foo", v -> fallback());

        //then
        assertThat(value, equalTo("fallback"));

        //then
        assertThat(cache.get("foo"), equalTo("fallback"));
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
    void testGetAll() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        String value1 = UUID.randomUUID().toString();
        String value2 = UUID.randomUUID().toString();
        
        //when
        cache.put("foo", value1);
        cache.put("bar", value2);
        
        //then
        assertThat(cache.getAll("foo", "bar"), not(equalTo(null)));
        assertThat(cache.getAll("foo", "bar").get("foo"), equalTo(value1));
        assertThat(cache.getAll("foo", "bar").get("bar"), equalTo(value2));
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

    private String fallback() {
        return "fallback";
    }
}