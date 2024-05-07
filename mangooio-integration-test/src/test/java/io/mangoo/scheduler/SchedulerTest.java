package io.mangoo.scheduler;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
public class SchedulerTest {

    @Test
    void testSchedule() {
        //given
        Scheduler scheduler = Application.getInstance(Scheduler.class);
        ScheduledFuture<?> scheduledFuture = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
            public void run() {
            }
        }, 0, 1, TimeUnit.MILLISECONDS);

        //when
        scheduler.addSchedule(Schedule.of("TestModel.class", "foo", "null", scheduledFuture, false));

        //then
        assertThat(scheduler, not(nullValue()));
        assertThat(scheduler.getSchedules().size(), greaterThan(1));
    }
}
