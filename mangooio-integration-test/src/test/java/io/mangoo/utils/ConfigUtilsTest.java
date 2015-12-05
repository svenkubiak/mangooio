package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

/**
 *
 * @author svenkubiak
 *
 */
public class ConfigUtilsTest {

    @Test
    public void testConfigUtils() {
        //then
        assertThat(ConfigUtils.getApplicationHost(), not(nullValue()));
        assertThat(ConfigUtils.getApplicationPort(), not(nullValue()));
    }
}