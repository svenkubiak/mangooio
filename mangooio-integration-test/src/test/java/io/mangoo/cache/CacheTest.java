package io.mangoo.cache;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThat(cache.getAll(key1, key2).get(key1), equalTo(value1));
        assertThat(cache.getAll(key1, key2).get(key2), equalTo(value2));
    }
    
    @Test
    void testIncrement() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        String key = UUID.randomUUID().toString();

        //then
        assertThat(cache.increment(key), equalTo(1L));
        assertThat(cache.increment(key), equalTo(2L));
        assertThat(cache.getCounter(key), equalTo(2L));
    }

    @Test
    void testDecrement() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        String key = UUID.randomUUID().toString();

        //then
        assertThat(cache.decrement(key), equalTo(-1L));
        assertThat(cache.decrement(key), equalTo(-2L));
        assertThat(cache.increment(key), equalTo(-1L));
    }

    @Test
    void testMissingCounterIsZero() {
        //given
        Cache cache = Application.getInstance(Cache.class);

        //then
        assertThat(cache.getCounter(UUID.randomUUID().toString()), equalTo(0L));
    }

    @Test
    void testResetCounter() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        String key = UUID.randomUUID().toString();
        cache.increment(key);
        cache.increment(key);

        //when
        cache.resetCounter(key);

        //then
        assertThat(cache.getCounter(key), equalTo(0L));
        assertThat(cache.increment(key), equalTo(1L));
    }

    @Test
    void testConcurrentIncrementsOfANewCounterAreNotLost() throws Exception {
        //given
        Cache cache = Application.getInstance(Cache.class);
        String key = UUID.randomUUID().toString();
        var start = new CountDownLatch(1);

        //when
        try (var executor = Executors.newFixedThreadPool(50)) {
            List<Future<?>> futures = new ArrayList<>();
            for (var i = 0; i < 50; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    for (var j = 0; j < 100; j++) {
                        cache.increment(key);
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        }

        //then
        assertThat(cache.getCounter(key), equalTo(5000L));
    }

    @Test
    void testExpiredCounterStartsAgain() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        String key = UUID.randomUUID().toString();
        cache.put(key, 5L, LocalDateTime.now().minusSeconds(1));

        //then
        assertThat(cache.increment(key), equalTo(1L));
    }

    @Test
    void testIncrementOfAValueThatIsNotACounterFails() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        String key = UUID.randomUUID().toString();
        cache.put(key, "text");

        //then
        assertThrows(IllegalStateException.class, () -> cache.increment(key));
        assertThrows(IllegalStateException.class, () -> cache.getCounter(key));
        assertThat(cache.get(key), equalTo("text"));
    }

    private String fallback() {
        return FALLBACK;
    }

    @Test
    void testFallbackReturningNullIsNotCached() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        String key = UUID.randomUUID().toString();
        AtomicInteger calls = new AtomicInteger();

        //when
        Object first = cache.get(key, k -> { calls.incrementAndGet(); return null; });
        Object second = cache.get(key, k -> { calls.incrementAndGet(); return null; });

        //then
        assertThat(first, nullValue());
        assertThat(second, nullValue());
        assertThat(calls.get(), equalTo(2));
    }

    @Test
    void testFallbackWithExpiryReturningNullIsNotCached() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        String key = UUID.randomUUID().toString();

        //when
        Object value = cache.get(key, 1, ChronoUnit.HOURS, k -> null);

        //then
        assertThat(value, nullValue());
        assertThat(cache.get(key), nullValue());
    }

    @Test
    void testPutNullRemovesValue() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        String key = UUID.randomUUID().toString();
        cache.put(key, "value");

        //when
        cache.put(key, null);

        //then
        assertThat(cache.get(key), nullValue());
    }
}
