package io.mangoo.scheduler;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Schedule {
    private final String clazz;
    private final String method;
    private final String runAt;
    private final ScheduledFuture<?> scheduledFuture;

    private Schedule(String clazz, String method, String runAt, ScheduledFuture<?> scheduledFuture) {
        this.clazz = Objects.requireNonNull(clazz, "clazz cannot be null");
        this.method = Objects.requireNonNull(method, "method cannot be null");
        this.runAt = Objects.requireNonNull(runAt, "runAt cannot be null");
        this.scheduledFuture = Objects.requireNonNull(scheduledFuture, "scheduledFuture cannot be null");
    }

    public static Schedule of(String clazz, String method, String runAt, ScheduledFuture<?> scheduledFuture) {
        return new Schedule(clazz, method, runAt, scheduledFuture);
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    public LocalDateTime next() {
        return LocalDateTime.now().plusSeconds(scheduledFuture.getDelay(TimeUnit.SECONDS));
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
