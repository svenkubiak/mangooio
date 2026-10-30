package io.mangoo.admin;

import io.mangoo.async.EventBus;
import io.mangoo.core.Application;
import io.mangoo.interfaces.InstanceRegistry;
import io.mangoo.models.InstanceStatus;
import io.mangoo.models.Metrics;
import io.mangoo.utils.internal.MangooUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/** Reports the status of this instance to the InstanceRegistry every ten seconds while the admin dashboard is enabled. */
@Singleton
public class InstanceHeartbeat {
    private static final Logger LOG = LogManager.getLogger(InstanceHeartbeat.class);
    private static final long INTERVAL_SECONDS = 10;
    private static final long TIMEOUT_SECONDS = 2;
    private final InstanceRegistry instanceRegistry;
    private final Metrics metrics;
    private final Instant started;
    private ScheduledExecutorService executor;

    @Inject
    public InstanceHeartbeat(InstanceRegistry instanceRegistry, Metrics metrics) {
        this.instanceRegistry = Objects.requireNonNull(instanceRegistry, "instanceRegistry can not be null");
        this.metrics = Objects.requireNonNull(metrics, "metrics can not be null");
        this.started = Instant.now().minus(Application.getUptime());
    }

    public synchronized void start() {
        if (executor != null) {
            return;
        }

        executor = Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().daemon().name("mangooio-instance-heartbeat").factory());
        executor.scheduleAtFixedRate(this::report, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Stops reporting and removes this instance, so the other instances no longer list it. Waits a few seconds at most,
     * so an unavailable MongoDB does not delay the shutdown; the entry then expires a minute after the last heartbeat.
     */
    public synchronized void stop() {
        if (executor == null) {
            return;
        }

        executor.shutdownNow();
        try {
            // A report still running would otherwise add this instance again after the removal
            if (executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                Thread.ofVirtual().start(() -> instanceRegistry.remove(Application.getInstanceId()))
                        .join(Duration.ofSeconds(TIMEOUT_SECONDS));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executor = null;
    }

    public InstanceStatus current() {
        var runtime = Runtime.getRuntime();
        long heapMax = runtime.maxMemory() == Long.MAX_VALUE ? runtime.totalMemory() : runtime.maxMemory();
        int connections = Stream.concat(
                        Application.getServerSentEventConnections().values().stream(),
                        Application.getWebSocketConnections().values().stream())
                .mapToInt(Integer::intValue)
                .sum();
        Metrics.Window window = metrics.getWindow();

        return new InstanceStatus(
                Application.getInstanceId(),
                MangooUtils.getVersion(),
                started,
                Instant.now(),
                runtime.totalMemory() - runtime.freeMemory(),
                heapMax,
                connections,
                window.requests(),
                window.errors(),
                window.avgRequestTime(),
                Application.getInstance(EventBus.class).getFailedEvents());
    }

    // An exception would cancel all further runs of the scheduled task
    private void report() {
        try {
            instanceRegistry.report(current());
        } catch (RuntimeException e) {
            LOG.error("Failed to report the status of this instance", e);
        }
    }
}
