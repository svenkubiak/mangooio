package io.mangoo.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.core.Application;
import io.mangoo.enums.Key;

public class ConfigTest {
    @Test
    public void testGetString() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString(Key.APPLICATION_PORT), equalTo("10808"));
        assertThat(config.getString(Key.APPLICATION_PORT.toString()), equalTo("10808"));
    }

    @Test
    public void testGetInt() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getInt(Key.APPLICATION_PORT), equalTo(10808));
        assertThat(config.getInt(Key.APPLICATION_PORT.toString()), equalTo(10808));
    }

    @Test
    public void testGetBoolean() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getBoolean(Key.APPLICATION_ADMIN_HEALTH), equalTo(true));
        assertThat(config.getBoolean(Key.APPLICATION_ADMIN_HEALTH.toString()), equalTo(true));
    }

    @Test
    public void testGetLong() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getLong(Key.APPLICATION_PORT), equalTo(10808L));
        assertThat(config.getLong(Key.APPLICATION_PORT.toString()), equalTo(10808L));
    }

    @Test
    public void testGetStringDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("foo", "bar"), equalTo("bar"));
    }

    @Test
    public void testGetIntDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getInt("foo", 42), equalTo(42));
    }

    @Test
    public void testGetBooleanDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getBoolean("foo", true), equalTo(true));
        assertThat(config.getBoolean("foo", false), equalTo(false));
    }

    @Test
    public void testGetLongDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getLong("foo", 42), equalTo(42L));
    }

    @Test
    public void testGetHasValidSecret() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.hasValidSecret(), equalTo(true));
    }

    @Test
    public void testGetAllConfigurationValues() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getAllConfigurations(), not(nullValue()));
        assertThat(config.getAllConfigurations().size(), greaterThan(12));
    }

    @Test
    public void testEnvironmentValues() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("smtp.username"), equalTo(""));
        assertThat(config.getString("smtp.port"), equalTo("3055"));
    }

    @Test
    public void testGetLocaleCookieName() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getLocaleCookieName(), equalTo("lang"));
    }
}