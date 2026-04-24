package io.mangoo.core;

import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.utils.internal.Trace;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;

@Singleton
public class Shutdown extends Thread {
    public Shutdown() {
        // Empty constructor for Google Guice
    }

    private static void invokeLifecycle() {
        Application.getInstance(MangooBootstrap.class).applicationStopped();
    }

    private static void stopScheduler() {
        if (Application.getInstance(Config.class).isSchedulerEnabled()) {
            Application.getScheduledExecutorService().shutdown();
            Application.getExecutorService().shutdown();
        }
    }

    private static void stopUndertow() {
        Application.stopUndertow();
    }

    private static void stopEmbeddedMongoDB() {
        Application.stopEmbeddedMongoDB();
    }

    private static void stopTelemetry() {
        if (Application.getInstance(Config.class).isOtlpEnable()) {
            Trace.shutdown();
        }
    }

    private static void stopLogger() {
        LogManager.shutdown();
    }
    
    @Override
    public void run() {
        Thread.ofVirtual().start(Shutdown::invokeLifecycle);
        Thread.ofVirtual().start(Shutdown::stopUndertow);
        Thread.ofVirtual().start(Shutdown::stopScheduler);
        Thread.ofVirtual().start(Shutdown::stopEmbeddedMongoDB);
        Thread.ofVirtual().start(Shutdown::stopTelemetry);
        Thread.ofVirtual().start(Shutdown::stopLogger);
    }
}