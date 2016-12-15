package io.mangoo.providers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.core.Application;

/**
 * 
 * @author svenkubiak
 *
 */
public class CacheProviderTest {
    @Test
    public void testGet() {
        //given
        CacheProvider cacheProvider = Application.getInstance(CacheProvider.class);
        
        //then
        assertThat(cacheProvider, not(nullValue()));
        assertThat(cacheProvider.get(), not(nullValue()));
    }
}