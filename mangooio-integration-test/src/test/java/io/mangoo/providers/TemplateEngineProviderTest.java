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
public class TemplateEngineProviderTest {

    @Test
    public void testGet() {
        //given
        TemplateEngineProvider templateEngineProvider = Application.getInstance(TemplateEngineProvider.class);
        
        //then
        assertThat(templateEngineProvider, not(nullValue()));
        assertThat(templateEngineProvider.get(), not(nullValue()));
    }
}