package io.mangoo.scheduler;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

class SchedulerThreadTest {
    // Shortly before the next minute, so that the cron task with "* * * * *" fires after 100 milliseconds
    private static final Clock CLOCK = Clock.fixed(ZonedDateTime.of(2026, 10, 1, 12, 0, 59, 900_000_000, ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);

    @Test
    void testLongRunningCronJobDoesNotBlockOtherTasks() throws Exception {
        //given
        var scheduler = Executors.newSingleThreadScheduledExecutor();
        var executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());
        Thread schedulerThread = scheduler.submit(Thread::currentThread).get();
        var cronJobThread = new AtomicReference<Thread>();
        var cronJobStarted = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var otherTaskRan = new CountDownLatch(1);

        var cronTask = new CronTask(() -> {
            cronJobThread.set(Thread.currentThread());
            cronJobStarted.countDown();
            await(release);
        }, "blocking", "* * * * *", scheduler, executor, CLOCK);
        var otherTask = new FixedDelayTask(otherTaskRan::countDown, "other", 1, scheduler, executor);

        try {
            //when
            cronTask.schedule();
            otherTask.schedule();

            //then the other task runs while the cron job is still blocked
            assertThat(cronJobStarted.await(5, TimeUnit.SECONDS), equalTo(true));
            assertThat(otherTaskRan.await(5, TimeUnit.SECONDS), equalTo(true));
            assertThat(release.getCount(), equalTo(1L));
            assertThat(cronJobThread.get(), not(equalTo(schedulerThread)));
        } finally {
            release.countDown();
            scheduler.shutdownNow();
            executor.shutdownNow();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
