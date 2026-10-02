package io.mangoo.scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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

    @Test
    void testScheduledFutureIsLookedUpOnAccess() {
        //given
        try (var executor = Executors.newSingleThreadScheduledExecutor()) {
            var first = executor.schedule(() -> {}, 1, TimeUnit.DAYS);
            var second = executor.schedule(() -> {}, 2, TimeUnit.DAYS);
            var current = new AtomicReference<ScheduledFuture<?>>(first);
            var schedule = Schedule.of("TestModel.class", "foo", "every 1d", current::get, false);

            //when
            current.set(second);

            //then
            assertThat(schedule.getScheduledFuture(), equalTo(second));
            assertThat(schedule.next(), greaterThan(LocalDateTime.now().plusDays(1)));

            executor.shutdownNow();
        }
    }
}
