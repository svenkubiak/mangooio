package io.mangoo.scheduler;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CronTask implements Runnable {
    private static final Logger LOG = LogManager.getLogger(CronTask.class);
    private final Runnable task;
    private final String name;
    private final ExecutionTime executionTime;
    private final ScheduledExecutorService scheduler;
    private final Executor executor;
    private final Clock clock;
    private ZonedDateTime lastSlot;
    private volatile ScheduledFuture<?> scheduledFuture;

    public CronTask(Class<?> clazz, String methodName, String cron) {
        this(new Task(Objects.requireNonNull(clazz, Required.CLASS), Objects.requireNonNull(methodName, Required.METHOD)),
                clazz.getName() + "." + methodName,
                cron,
                Application.getScheduledExecutorService(),
                Application.getExecutorService(),
                Clock.systemDefaultZone());
    }

    CronTask(Runnable task, String name, String cron, ScheduledExecutorService scheduler, Executor executor, Clock clock) {
        Objects.requireNonNull(cron, Required.CRON);
        this.task = Objects.requireNonNull(task, "task can not be null");
        this.name = Objects.requireNonNull(name, "name can not be null");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler can not be null");
        this.executor = Objects.requireNonNull(executor, "executor can not be null");
        this.clock = Objects.requireNonNull(clock, "clock can not be null");
        this.executionTime = ExecutionTime.forCron(new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)).parse(cron));
    }

    // Runs the task on the executor and reschedules itself once the task has finished, instead of calling itself recursively.
    @Override
    public void run() {
        try {
            executor.execute(() -> {
                try {
                    task.run();
                } catch (RuntimeException e) {
                    LOG.error("Failed to execute scheduled cron task '{}'", name, e);
                } finally {
                    schedule();
                }
            });
        } catch (RejectedExecutionException e) {
            LOG.debug("Executor rejected cron task '{}', stopping", name);
        }
    }

    // Uses the last slot as base while it lies in the future, so that a task finishing early does not run twice in the same slot.
    // The delay is scheduled in milliseconds, as truncating to seconds would fire before the slot; returns null if there is no further execution.
    @SuppressWarnings("java:S1452")
    public ScheduledFuture<?> schedule() {
        var now = ZonedDateTime.now(clock);
        var base = lastSlot != null && lastSlot.isAfter(now) ? lastSlot : now;

        var next = executionTime.nextExecution(base);
        if (next.isEmpty()) {
            LOG.warn("Cron task '{}' has no further execution and will not be scheduled again", name);
            return null;
        }

        lastSlot = next.orElseThrow();
        try {
            var future = scheduler.schedule(this, Duration.between(now, lastSlot).toMillis(), TimeUnit.MILLISECONDS);
            scheduledFuture = future;
            return future;
        } catch (RejectedExecutionException e) {
            LOG.debug("Scheduler rejected cron task '{}', stopping", name);
            return null;
        }
    }

    @SuppressWarnings("java:S1452")
    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }
}
