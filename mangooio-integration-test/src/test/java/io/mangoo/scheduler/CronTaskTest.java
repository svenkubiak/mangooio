package io.mangoo.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

class CronTaskTest {
    private static final ZoneId UTC = ZoneOffset.UTC;
    private final CapturingScheduler scheduler = new CapturingScheduler();

    @AfterEach
    void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    void testRunDoesNotRecurse() {
        //given
        var executions = new AtomicInteger();
        var clock = new MutableClock(ZonedDateTime.of(2026, 10, 1, 12, 0, 0, 0, UTC).toInstant());
        var cronTask = new CronTask(executions::incrementAndGet, "test", "* * * * *", scheduler, Runnable::run, clock);
        cronTask.schedule();

        //when
        for (var i = 0; i < 100_000; i++) {
            clock.instant = clock.instant.plusSeconds(60);
            scheduler.command.run();
        }

        //then
        assertThat(executions.get(), equalTo(100_000));
        assertThat(scheduler.scheduled, equalTo(100_001));
    }

    @Test
    void testExceptionInTaskStillReschedules() {
        //given
        var clock = new MutableClock(ZonedDateTime.of(2026, 10, 1, 12, 0, 0, 0, UTC).toInstant());
        var cronTask = new CronTask(() -> { throw new IllegalStateException("boom"); }, "test", "* * * * *", scheduler, Runnable::run, clock);
        cronTask.schedule();

        //when
        clock.instant = clock.instant.plusSeconds(60);
        scheduler.command.run();

        //then
        assertThat(scheduler.scheduled, equalTo(2));
    }

    @Test
    void testDelayIsMillisecondPrecise() {
        //given
        var clock = new MutableClock(ZonedDateTime.of(2026, 10, 1, 23, 59, 0, 300_000_000, UTC).toInstant());
        var cronTask = new CronTask(() -> {}, "test", "0 0 * * *", scheduler, Runnable::run, clock);

        //when
        ScheduledFuture<?> scheduledFuture = cronTask.schedule();

        //then
        assertThat(scheduledFuture, not(nullValue()));
        assertThat(scheduler.delayMillis, equalTo(59_700L));
    }

    @Test
    void testNoDuplicateExecutionWhenFiredEarly() {
        //given
        var clock = new MutableClock(ZonedDateTime.of(2026, 10, 1, 23, 59, 0, 0, UTC).toInstant());
        var executions = new AtomicInteger();
        var cronTask = new CronTask(executions::incrementAndGet, "test", "0 0 * * *", scheduler, Runnable::run, clock);
        cronTask.schedule();

        //when
        clock.instant = ZonedDateTime.of(2026, 10, 1, 23, 59, 59, 999_000_000, UTC).toInstant();
        scheduler.command.run();

        //then
        assertThat(executions.get(), equalTo(1));
        assertThat(scheduler.delayMillis, equalTo(TimeUnit.DAYS.toMillis(1) + 1));
    }

    private static final class CapturingScheduler extends ScheduledThreadPoolExecutor {
        private Runnable command;
        private long delayMillis;
        private int scheduled;

        private CapturingScheduler() {
            super(1);
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            this.command = command;
            this.delayMillis = unit.toMillis(delay);
            this.scheduled++;
            return super.schedule(() -> {}, 1, TimeUnit.DAYS);
        }
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
