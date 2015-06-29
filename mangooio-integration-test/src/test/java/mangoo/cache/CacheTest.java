package mangoo.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import mangoo.io.cache.Cache;
import mangoo.io.test.MangooTest;

public class CacheTest {
    private static final String TEST = "this is a test for the cache";
	private static Cache cache;

    @Before
    public void init() {
        cache = MangooTest.INSTANCE.getInjector().getInstance(Cache.class);
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

    	String test = cache.get("test", String.class);
    	assertEquals(TEST, test);

    	int foo = cache.get("test2", int.class);
    	assertEquals(1, foo);
    }
}