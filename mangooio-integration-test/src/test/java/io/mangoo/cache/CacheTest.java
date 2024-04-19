package io.mangoo.cache;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    public static final String FALLBACK = "fallback";

    @Test
    void testAdd() {
        //given
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        Cache cache = Application.getInstance(Cache.class);
        
        //when
        cache.put(key, value);

        //then
        assertThat(cache.get(key), not(nullValue()));
        assertThat(cache.get(key), equalTo(value));
    }

    @Test
    void testExpiresLocalDateTime() {
        //given
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        Cache cache = Application.getInstance(Cache.class);

        //when
        cache.put(key, value, LocalDateTime.now().plusMinutes(5));

        //then
        assertThat(cache.get(key), not(nullValue()));
        assertThat(cache.get(key), equalTo(value));
    }

    @Test
    void testExpiresTemporal() {
        //given
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        Cache cache = Application.getInstance(Cache.class);

        //when
        cache.put(key, value, 5, ChronoUnit.MINUTES);

        //then
        assertThat(cache.get(key), not(nullValue()));
        assertThat(cache.get(key), equalTo(value));
    }

    @Test
    void testClear() {
        //given
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        Cache cache = Application.getInstance(Cache.class);
        
        //when
        cache.put(key, value);
        cache.clear();

        //then
        assertThat(cache.get(key), equalTo(null));
    }

    @Test
    void testCast() {
        //given
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        Cache cache = Application.getInstance(Cache.class);
        
        //when
    	cache.put(key, value);

    	//then
        assertThat(cache.get(key), equalTo(value));
    }
    
    @Test
    void testGetWithFallback() {
        //given
        String key = UUID.randomUUID().toString();
        Cache cache = Application.getInstance(Cache.class);

        //then
        assertThat(cache.get(key), equalTo(null));

        //when
        String value = cache.get(key, v -> fallback());

        //then
        assertThat(value, equalTo(FALLBACK));

        //then
        assertThat(cache.get(key), equalTo(FALLBACK));
    }
    
    @Test
    void testPutAll() {
        //given
        String key1 = UUID.randomUUID().toString();
        String value1 = UUID.randomUUID().toString();
        String key2 = UUID.randomUUID().toString();
        String value2 = UUID.randomUUID().toString();
        Cache cache = Application.getInstance(Cache.class);
        
        //when
        Map<String, Object> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        cache.putAll(map);
        
        //then
        assertThat(cache.get(key1), equalTo(value1));
        assertThat(cache.get(key2), equalTo(value2));
    }
    
    @Test
    void testGetAll() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        String key1 = UUID.randomUUID().toString();
        String key2 = UUID.randomUUID().toString();
        String value1 = UUID.randomUUID().toString();
        String value2 = UUID.randomUUID().toString();
        
        //when
        cache.put(key1, value1);
        cache.put(key2, value2);
        
        //then
        assertThat(cache.getAll(key1, key2), not(equalTo(null)));
        assertThat(cache.getAll(key1, key2).get(key1), equalTo(value1));
        assertThat(cache.getAll(key1, key2).get(key2), equalTo(value2));
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
        return FALLBACK;
    }
}