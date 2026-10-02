package io.mangoo.scheduler;

import com.google.common.base.Preconditions;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FixedDelayTask implements Runnable {
    private static final Logger LOG = LogManager.getLogger(FixedDelayTask.class);
    private final Runnable task;
    private final String name;
    private final long delaySeconds;
    private final ScheduledExecutorService scheduler;
    private final Executor executor;
    private volatile ScheduledFuture<?> scheduledFuture;

    public FixedDelayTask(Class<?> clazz, String methodName, long delaySeconds) {
        this(new Task(Objects.requireNonNull(clazz, Required.CLASS), Objects.requireNonNull(methodName, Required.METHOD)),
                clazz.getName() + "." + methodName,
                delaySeconds,
                Application.getScheduledExecutorService(),
                Application.getExecutorService());
    }

    FixedDelayTask(Runnable task, String name, long delaySeconds, ScheduledExecutorService scheduler, Executor executor) {
        Preconditions.checkArgument(delaySeconds > 0, "delaySeconds must be greater than 0");
        this.task = Objects.requireNonNull(task, "task can not be null");
        this.name = Objects.requireNonNull(name, "name can not be null");
        this.delaySeconds = delaySeconds;
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler can not be null");
        this.executor = Objects.requireNonNull(executor, "executor can not be null");
    }

    // Runs the task on the executor and schedules the next run only after it finished, so runs never overlap
    @Override
    public void run() {
        try {
            executor.execute(() -> {
                try {
                    task.run();
                } catch (RuntimeException e) {
                    LOG.error("Failed to execute scheduled task '{}'", name, e);
                } finally {
                    schedule();
                }
            });
        } catch (RejectedExecutionException e) {
            LOG.debug("Executor rejected task '{}', stopping", name);
        }
    }

    @SuppressWarnings("java:S1452")
    public ScheduledFuture<?> schedule() {
        try {
            var future = scheduler.schedule(this, delaySeconds, TimeUnit.SECONDS);
            scheduledFuture = future;
            return future;
        } catch (RejectedExecutionException e) {
            LOG.debug("Scheduler rejected task '{}', stopping", name);
            return null;
        }
    }

    @SuppressWarnings("java:S1452")
    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }
}
