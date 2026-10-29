package io.mangoo.core;

import io.mangoo.constants.Default;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.manager.ServerSentEventManager;
import io.mangoo.persistence.DatastoreProvider;
import io.mangoo.utils.internal.Trace;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Stops the instance on SIGTERM: readiness goes down, long-lived connections are closed, open requests and running
 * scheduled tasks get application.shutdown.timeout in total, then the lifecycle, telemetry and datastores are stopped.
 */
@Singleton
public class Shutdown extends Thread {
    private static final Logger LOG = LogManager.getLogger(Shutdown.class);
    private long deadline;

    public Shutdown() {
        // Empty constructor for Google Guice
    }

    private void markShuttingDown() {
        Application.markShuttingDown();
        deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(Application.getInstance(Config.class).getApplicationShutdownTimeout());
    }

    // No new run is started, running tasks finish until the deadline
    private void stopScheduling() {
        try {
            if (Application.getInstance(Config.class).isSchedulerEnabled() && Application.getScheduledExecutorService() != null) {
                LOG.info("Stopping Scheduler");
                Application.getScheduledExecutorService().shutdownNow();
                Application.getExecutorService().shutdown();
            }
        } catch (Exception e) {
            LOG.error("Error stopping Scheduler", e);
        }
    }

    private void closeConnections() {
        try {
            LOG.info("Closing server sent event and websocket connections");
            Application.getInstance(ServerSentEventManager.class).closeAll();
            Application.closeWebSockets();
        } catch (Exception e) {
            LOG.error("Error closing server sent event and websocket connections", e);
        }
    }

    private void drainRequests() {
        try {
            LOG.info("Waiting for open requests");
            if (!Application.drain(remainingMillis())) {
                LOG.warn("Open requests did not finish within application.shutdown.timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while waiting for open requests", e);
        } catch (Exception e) {
            LOG.error("Error waiting for open requests", e);
        }
    }

    private void stopUndertow() {
        try {
            LOG.info("Stopping Undertow");
            Application.stopUndertow();
        } catch (Exception e) {
            LOG.error("Error stopping Undertow", e);
        }
    }

    private void awaitScheduledTasks() {
        try {
            var executorService = Application.getExecutorService();
            if (executorService != null && !executorService.awaitTermination(remainingMillis(), TimeUnit.MILLISECONDS)) {
                LOG.warn("Scheduled tasks did not finish within application.shutdown.timeout and are interrupted");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while waiting for scheduled tasks", e);
        } catch (Exception e) {
            LOG.error("Error waiting for scheduled tasks", e);
        }
    }

    private void stopMessageBroker() {
        try {
            LOG.info("Stopping MessageBroker");
            Application.stopMessageBroker();
        } catch (Exception e) {
            LOG.error("Error stopping MessageBroker", e);
        }
    }

    private void invokeLifecycle() {
        try {
            Application.getInstance(MangooBootstrap.class).applicationStopped();
        } catch (Exception e) {
            LOG.error("Error invoking lifecycle shutdown", e);
        }
    }

    private void closeDatastores() {
        try {
            if (Application.getInstance(Config.class).isPersistenceEnabled()) {
                LOG.info("Closing MongoDB connections");
                Application.getInstance(DatastoreProvider.class).close();
            }
        } catch (Exception e) {
            LOG.error("Error closing MongoDB connections", e);
        }
    }

    private void stopEmbeddedMongoDB() {
        try {
            var config = Application.getInstance(Config.class);
            if (config.isPersistenceEnabled() && Boolean.TRUE.equals(config.isMongoEmbedded(Default.PERSISTENCE_PREFIX))) {
                LOG.info("Stopping EmbeddedMongoDB");
                Application.stopEmbeddedMongoDB();
            }
        } catch (Exception e) {
            LOG.error("Error stopping EmbeddedMongoDB", e);
        }
    }

    private void stopTelemetry() {
        try {
            if (Application.getInstance(Config.class).isOtlpEnable()) {
                LOG.info("Stopping Telemetry");
                Trace.shutdown();
            }
        } catch (Exception e) {
            LOG.error("Error stopping Telemetry", e);
        }
    }

    private void stopLogger() {
        LOG.info("Stopping Log4j");
        LogManager.shutdown();
    }

    private long remainingMillis() {
        return Math.max(0, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()));
    }

    @Override
    public void run() {
        markShuttingDown();
        stopScheduling();
        closeConnections();
        drainRequests();
        stopUndertow();
        awaitScheduledTasks();
        stopMessageBroker();
        invokeLifecycle();
        closeDatastores();
        stopEmbeddedMongoDB();
        stopTelemetry();
        stopLogger();
    }
}
