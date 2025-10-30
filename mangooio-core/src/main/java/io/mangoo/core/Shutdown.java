package io.mangoo.core;

import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.utils.internal.Trace;
import jakarta.inject.Singleton;

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
            Application.getInstance(Trace.class).shutdown();
        }
    }
    
    @Override
    public void run() {
        invokeLifecycle();
        stopUndertow();
        stopScheduler();
        stopEmbeddedMongoDB();
        stopTelemetry();
    }
}