package mangoo.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import mangoo.io.cache.Cache;
import mangoo.io.core.Application;

import org.junit.Before;
import org.junit.Test;

public class CacheTest {
    private static Cache cache;
    
    @Before
    public void init() {
        cache = Application.getInjector().getInstance(Cache.class);
    }
    
    @Test
    public void addTest() {
        String test = "this is a test for the cache";
        cache.add("test", test);
        
        assertEquals(cache.get("test"), test);
    }
    
    @Test
    public void clearTest() {
        String test = "this is a test for the cache";
        cache.add("test", test);
        
        assertEquals(cache.get("test"), test);
        
        cache.clear();
        
        assertNull(cache.get("test"));
    }
}