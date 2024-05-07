package io.mangoo.core;

import io.mangoo.TestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
public class ApplicationTest {
    @Test
    void testIsStarted() {
        //given
        boolean started = Application.isStarted();

        //then
        assertThat(started, equalTo(true));
    }

    @Test
    void testGetUptime() {
        //given
        Duration uptime = Application.getUptime();

        //then
        assertThat(uptime, not(nullValue()));
    }
}
