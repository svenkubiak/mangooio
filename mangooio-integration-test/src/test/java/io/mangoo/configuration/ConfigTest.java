package io.mangoo.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import io.mangoo.enums.Key;
import io.mangoo.test.Mangoo;

public class ConfigTest {
    @Test
    public void testGetString() {
        //given
        Config config = Mangoo.TEST.getInstance(Config.class);
        
        //then
        assertThat(config.getString(Key.APPLICATION_PORT), equalTo("10808"));
        assertThat(config.getString(Key.APPLICATION_PORT.toString()), equalTo("10808"));
    }
}