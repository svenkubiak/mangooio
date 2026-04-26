package io.mangoo.core;

import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.utils.internal.Trace;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class Shutdown extends Thread {
    private static final Logger LOG = LogManager.getLogger(Shutdown.class);

    public Shutdown() {
        // Empty constructor for Google Guice
    }

    private void invokeLifecycle() {
        try {
            Application.getInstance(MangooBootstrap.class).applicationStopped();
        } catch (Exception e) {
            LOG.error("Error invoking lifecycle shutdown", e);
        }
    }

    private void stopScheduler() {
        try {
            if (Application.getInstance(Config.class).isSchedulerEnabled()) {
                LOG.info("Stopping Scheduler");
                Application.getScheduledExecutorService().shutdown();
                Application.getExecutorService().shutdown();
            }
        } catch (Exception e) {
            LOG.error("Error stopping Scheduler", e);
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

    private void stopEmbeddedMongoDB() {
        try {
            if (Application.getInstance(Config.class).isPersistenceEnabled()) {
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

    @Override
    public void run() {
        invokeLifecycle();
        stopScheduler();
        stopEmbeddedMongoDB();
        stopTelemetry();
        stopUndertow();
        stopLogger();
    }
}