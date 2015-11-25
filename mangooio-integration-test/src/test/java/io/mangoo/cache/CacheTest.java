package io.mangoo.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.junit.Test;

import io.mangoo.test.Mangoo;

/**
 * 
 * @author svenkubiak
 *
 */
public class CacheTest {
    private static final String TEST_VALUE = "This is a test value for the cache!";
    private static final String FROM_CALLABLE = "from callable";

    @Test
    public void testAdd() {
        //given
        Cache cache = Mangoo.TEST.getInstance(Cache.class);
        
        //when
        cache.put("test", TEST_VALUE);

        //then
        assertThat(cache.get("test"), not(nullValue()));
        assertThat(cache.get("test").get(), equalTo(TEST_VALUE));
    }

    @Test
    public void testClear() {
        //given
        Cache cache = Mangoo.TEST.getInstance(Cache.class);
        
        //when
        cache.put("test", TEST_VALUE);
        cache.clear();

        //then
        assertThat(cache.get("test"), equalTo(Optional.empty()));
    }

    @Test
    public void testCast() {
        //given
        Cache cache = Mangoo.TEST.getInstance(Cache.class);
        
        //when
    	cache.put("test", 1);

    	//then
        assertThat(cache.get("test").get(), equalTo(1));
    }
    
    @Test
    public void testGetAll() {
        //given
        Cache cache = Mangoo.TEST.getInstance(Cache.class);
        
        //when
        cache.clear();
        cache.put("test", TEST_VALUE);
        cache.put("test2", 1);
        
        //then
        assertThat(cache.getAll(), not(nullValue()));
        assertThat(cache.getAll().size(), equalTo(2));
    }
    
    @Test
    public void testPutAll() {
        //given
        Cache cache = Mangoo.TEST.getInstance(Cache.class);
        
        //when
        Map<String, Object> map = new HashMap<>();
        map.put("test", TEST_VALUE);
        map.put("test2", 1);
        cache.putAll(map);
        
        //then
        assertThat(cache.getAll(), not(nullValue()));
        assertThat(cache.getAll().size(), equalTo(2));
    }
    
    @Test
    public void testSize() {
        //given
        Cache cache = Mangoo.TEST.getInstance(Cache.class);
        
        //when
        cache.clear();
        cache.put("test1", TEST_VALUE);
        cache.put("test2", TEST_VALUE);
        cache.put("test3", TEST_VALUE);
        cache.put("test4", TEST_VALUE);
        
        //then
        assertThat(cache.size(), equalTo(4L));
    }
    
    @Test
    public void testStats() {
        //given
        Cache cache = Mangoo.TEST.getInstance(Cache.class);
        
        //then
        assertThat(cache.getStats(), not(nullValue()));
    }
    
    @Test
    public void testGetWithCallable() {
        //given
        Cache cache = Mangoo.TEST.getInstance(Cache.class);
        
        //then
        assertThat(cache.get("test", new CacheCallable()).get(), equalTo(FROM_CALLABLE));
    }
    
    private class CacheCallable implements Callable<String> {
        @Override
        public String call() throws Exception {
            return FROM_CALLABLE;
        }
    }
}