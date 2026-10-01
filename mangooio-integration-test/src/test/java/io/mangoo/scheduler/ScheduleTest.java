package io.mangoo.scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

class ScheduleTest {

    @Test
    void testNextCronExecutionIsExactSlot() {
        //given
        try (var executor = Executors.newSingleThreadScheduledExecutor()) {
            var scheduledFuture = executor.schedule(() -> {}, 1, TimeUnit.DAYS);
            var schedule = Schedule.of("TestModel.class", "foo", "0 0 * * *", scheduledFuture, true);

            //when
            LocalDateTime next = schedule.next();

            //then
            assertThat(next.toLocalTime(), equalTo(LocalTime.MIDNIGHT));
            assertThat(next, greaterThan(LocalDateTime.now()));

            executor.shutdownNow();
        }
    }
}
