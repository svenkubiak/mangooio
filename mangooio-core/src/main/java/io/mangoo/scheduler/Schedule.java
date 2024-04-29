package io.mangoo.scheduler;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Schedule {
    private final String clazz;
    private final String method;
    private final String runAt;
    private final ScheduledFuture<?> scheduledFuture;
    private final boolean cron;

    private Schedule(String clazz, String method, String runAt, ScheduledFuture<?> scheduledFuture, boolean cron) {
        this.clazz = Objects.requireNonNull(clazz, "clazz cannot be null");
        this.method = Objects.requireNonNull(method, "method cannot be null");
        this.runAt = Objects.requireNonNull(runAt, "runAt cannot be null");
        this.scheduledFuture = Objects.requireNonNull(scheduledFuture, "scheduledFuture cannot be null");
        this.cron = cron;
    }

    public static Schedule of(String clazz, String method, String runAt, ScheduledFuture<?> scheduledFuture, boolean cron) {
        return new Schedule(clazz, method, runAt, scheduledFuture, cron);
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    public LocalDateTime next() {
        if (cron) {
            ExecutionTime executionTime = ExecutionTime.forCron(new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)).parse(runAt));
            long seconds = executionTime
                    .timeToNextExecution(ZonedDateTime.now())
                    .orElse(Duration.ofSeconds(-1))
                    .getSeconds();

            return LocalDateTime.now().plusSeconds(seconds);
        } else {
            return LocalDateTime.now().plusSeconds(scheduledFuture.getDelay(TimeUnit.SECONDS));
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
