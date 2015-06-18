package mangoo.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import mangoo.io.cache.Cache;
import mangoo.io.core.Application;

import org.junit.Before;
import org.junit.Test;

public class CacheTest {
    private static final String TEST = "this is a test for the cache";
	private static Cache cache;

    @Before
    public void init() {
        cache = Application.getInjector().getInstance(Cache.class);
    }

    @Test
    public void addTest() {
        cache.add("test", TEST);

        assertEquals(cache.get("test"), TEST);
    }

    @Test
    public void clearTest() {
        cache.add("test", TEST);

        assertEquals(cache.get("test"), TEST);

        cache.clear();

        assertNull(cache.get("test"));
    }

    @Test
    public void castTest() {
    	cache.add("test", TEST);
    	cache.add("test2", 1);

    	String test = cache.getTyped("test");
    	assertEquals(TEST, test);

    	int foo = cache.getTyped("test2");
    	assertEquals(1, foo);
    }
}