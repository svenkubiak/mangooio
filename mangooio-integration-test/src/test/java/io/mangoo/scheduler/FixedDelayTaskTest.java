package io.mangoo.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

class FixedDelayTaskTest {
    private final CapturingScheduler scheduler = new CapturingScheduler();
    private final HoldingExecutor executor = new HoldingExecutor();

    @AfterEach
    void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    void testNextRunIsScheduledOnlyAfterTheTaskFinished() {
        //given
        var executions = new AtomicInteger();
        var task = new FixedDelayTask(executions::incrementAndGet, "test", 60, scheduler, executor);
        task.schedule();

        //when the scheduler fires while the task has not finished yet
        scheduler.command.run();

        //then nothing new is scheduled, so runs can not overlap
        assertThat(scheduler.scheduled, equalTo(1));
        assertThat(executions.get(), equalTo(0));

        //when the task finishes
        executor.runHeld();

        //then exactly one next run is scheduled with the configured delay
        assertThat(executions.get(), equalTo(1));
        assertThat(scheduler.scheduled, equalTo(2));
        assertThat(scheduler.delaySeconds, equalTo(60L));
    }

    @Test
    void testExceptionInTaskStillSchedulesNextRun() {
        //given
        var task = new FixedDelayTask(() -> { throw new IllegalStateException("boom"); }, "test", 60, scheduler, Runnable::run);
        task.schedule();

        //when
        scheduler.command.run();

        //then
        assertThat(scheduler.scheduled, equalTo(2));
    }

    @Test
    void testScheduledFutureIsTheCurrentOne() {
        //given
        var task = new FixedDelayTask(() -> {}, "test", 60, scheduler, Runnable::run);
        ScheduledFuture<?> first = task.schedule();

        //when
        scheduler.command.run();

        //then
        assertThat(task.getScheduledFuture(), not(sameInstance(first)));
        assertThat(task.getScheduledFuture(), sameInstance(scheduler.last));
    }

    private static final class CapturingScheduler extends ScheduledThreadPoolExecutor {
        private Runnable command;
        private long delaySeconds;
        private int scheduled;
        private ScheduledFuture<?> last;

        private CapturingScheduler() {
            super(1);
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            this.command = command;
            this.delaySeconds = unit.toSeconds(delay);
            this.scheduled++;
            this.last = super.schedule(() -> {}, 1, TimeUnit.DAYS);
            return last;
        }
    }

    // Holds submitted runnables until runHeld is called, to simulate a task that is still running
    private static final class HoldingExecutor implements Executor {
        private final List<Runnable> held = new ArrayList<>();

        @Override
        public void execute(Runnable command) {
            held.add(command);
        }

        private void runHeld() {
            var commands = new ArrayList<>(held);
            held.clear();
            commands.forEach(Runnable::run);
        }
    }
}
