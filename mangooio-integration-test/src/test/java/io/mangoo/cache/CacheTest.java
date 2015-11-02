package io.mangoo.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.Before;
import org.junit.Test;

import io.mangoo.test.MangooInstance;

/**
 * 
 * @author svenkubiak
 *
 */
public class CacheTest {
    private static final String TEST = "this is a test for the cache";
	private static Cache cache;

    @Before
    public void init() {
        cache = MangooInstance.TEST.getInjector().getInstance(Cache.class);
    }

    @Test
    public void testAdd() {
        cache.add("test", TEST);

        assertEquals(cache.get("test"), TEST);
    }

    @Test
    public void testClear() {
        cache.add("test", TEST);

        assertEquals(cache.get("test"), TEST);

        cache.clear();

        assertNull(cache.get("test"));
    }

    @Test
    public void testCast() {
    	cache.add("test", TEST);
    	cache.add("test2", 1);

    	String test = cache.get("test");
    	assertEquals(TEST, test);

    	int foo = cache.get("test2");
    	assertEquals(1, foo);
    }
    
    @Test
    public void testGetAll() {
        cache.add("test", TEST);
        cache.add("test2", 1);
        
        ConcurrentMap<String, Object> map = cache.getAll();
        assertNotNull(map);
        assertEquals(2, map.size());
    }
    
    @Test
    public void testPutAll() {
        ConcurrentMap<String, Object> concurrentMap = new ConcurrentHashMap<String, Object>();
        concurrentMap.put("test", TEST);
        concurrentMap.put("test2", 1);
        
        cache.addAll(concurrentMap);
        
        ConcurrentMap<String, Object> map = cache.getAll();
        assertNotNull(map);
        assertEquals(2, map.size());
    }
}