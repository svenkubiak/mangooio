package io.mangoo.scheduler;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Schedule {
    private final String clazz;
    private final String method;
    private final String runAt;
    // A rescheduling task gets a new future for every run, so the current one is looked up on access
    private final Supplier<ScheduledFuture<?>> scheduledFuture;
    private final boolean cron;

    private Schedule(String clazz, String method, String runAt, Supplier<ScheduledFuture<?>> scheduledFuture, boolean cron) {
        this.clazz = Objects.requireNonNull(clazz, "clazz cannot be null");
        this.method = Objects.requireNonNull(method, "method cannot be null");
        this.runAt = Objects.requireNonNull(runAt, "runAt cannot be null");
        this.scheduledFuture = Objects.requireNonNull(scheduledFuture, "scheduledFuture cannot be null");
        this.cron = cron;
    }

    public static Schedule of(String clazz, String method, String runAt, ScheduledFuture<?> scheduledFuture, boolean cron) {
        Objects.requireNonNull(scheduledFuture, "scheduledFuture cannot be null");
        return new Schedule(clazz, method, runAt, () -> scheduledFuture, cron);
    }

    public static Schedule of(String clazz, String method, String runAt, Supplier<ScheduledFuture<?>> scheduledFuture, boolean cron) {
        return new Schedule(clazz, method, runAt, scheduledFuture, cron);
    }

    @SuppressWarnings("java:S1452")
    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture.get();
    }

    public LocalDateTime next() {
        if (cron) {
            var executionTime = ExecutionTime.forCron(new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)).parse(runAt));
            return executionTime
                    .nextExecution(ZonedDateTime.now())
                    .map(ZonedDateTime::toLocalDateTime)
                    .orElse(null);
        } else {
            return LocalDateTime.now().plusSeconds(getScheduledFuture().getDelay(TimeUnit.SECONDS));
        }
    }

    public String getRunAt() {
        return runAt;
    }

    public String getMethod() {
        return method;
    }

    public String getClazz() {
        return clazz.replace("class", "").trim();
    }
}
