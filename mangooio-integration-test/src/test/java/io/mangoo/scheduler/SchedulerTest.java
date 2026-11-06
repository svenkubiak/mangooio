package io.mangoo.scheduler;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
public class SchedulerTest {

    @Test
    void testSchedule() {
        //given
        Scheduler scheduler = Application.getInstance(Scheduler.class);

        //then
        assertThat(scheduler.getSchedules().size(), equalTo(7));
    }

    @Test
    void testRunMethodWithFurtherAnnotationsIsScheduled() {
        //given
        Scheduler scheduler = Application.getInstance(Scheduler.class);

        //then
        assertThat(scheduler.getSchedules().stream()
                .filter(schedule -> schedule.getClazz().equals("jobs.AnnotatedJob"))
                .map(schedule -> schedule.getMethod() + " " + schedule.getRunAt())
                .toList(), contains("execute every 1h"));
    }
}
