package io.mangoo.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

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
    
    @Test
    public void testGetInt() {
        //given
        Config config = Mangoo.TEST.getInstance(Config.class);
        
        //then
        assertThat(config.getInt(Key.APPLICATION_PORT), equalTo(10808));
        assertThat(config.getInt(Key.APPLICATION_PORT.toString()), equalTo(10808));
    }
    
    @Test
    public void testGetBoolean() {
        //given
        Config config = Mangoo.TEST.getInstance(Config.class);
        
        //then
        assertThat(config.getBoolean(Key.APPLICATION_ADMIN_HEALTH), equalTo(true));
        assertThat(config.getBoolean(Key.APPLICATION_ADMIN_HEALTH.toString()), equalTo(true));
    }
    
    @Test
    public void testGetLong() {
        //given
        Config config = Mangoo.TEST.getInstance(Config.class);
        
        //then
        assertThat(config.getLong(Key.APPLICATION_PORT), equalTo(10808L));
        assertThat(config.getLong(Key.APPLICATION_PORT.toString()), equalTo(10808L));
    }
    
    @Test
    public void testGetStringDefaultValue() {
        //given
        Config config = Mangoo.TEST.getInstance(Config.class);
        
        //then
        assertThat(config.getString("foo", "bar"), equalTo("bar"));
    }
    
    @Test
    public void testGetIntDefaultValue() {
        //given
        Config config = Mangoo.TEST.getInstance(Config.class);
        
        //then
        assertThat(config.getInt("foo", 42), equalTo(42));
    }
    
    @Test
    public void testGetBooleanDefaultValue() {
        //given
        Config config = Mangoo.TEST.getInstance(Config.class);
        
        //then
        assertThat(config.getBoolean("foo", true), equalTo(true));
        assertThat(config.getBoolean("foo", false), equalTo(false));
    }
    
    public void testGetLongDefaultValue() {
        //given
        Config config = Mangoo.TEST.getInstance(Config.class);
        
        //then
        assertThat(config.getLong("foo", 42), equalTo(42));
    }
    
    @Test
    public void testGetHasValidSecret() {
        //given
        Config config = Mangoo.TEST.getInstance(Config.class);
        
        //then
        assertThat(config.hasValidSecret(), equalTo(true));
    }

    @Test
    public void testGetAllConfigurationValues() {
        //given
        Config config = Mangoo.TEST.getInstance(Config.class);
        
        //then
        assertThat(config.getAllConfigurations(), not(nullValue()));
        assertThat(config.getAllConfigurations().size(), greaterThan(12));
    }
    
    @Test
    public void testEnvironmentValues() {
        //given
        Config config = Mangoo.TEST.getInstance(Config.class);
        
        //then
        assertThat(config.getString("smtp.username"), equalTo(""));
        assertThat(config.getString("smtp.port"), equalTo("3055"));
    }
}