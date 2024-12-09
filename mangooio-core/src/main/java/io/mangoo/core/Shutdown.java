package io.mangoo.core;

import jakarta.inject.Singleton;
import io.mangoo.interfaces.MangooBootstrap;

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
    
    @Override
    public void run() {
        invokeLifecycle();
        stopUndertow();
        stopScheduler();
        stopEmbeddedMongoDB();
    }
}