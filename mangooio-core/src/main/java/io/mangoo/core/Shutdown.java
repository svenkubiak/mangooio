package io.mangoo.core;

import com.google.inject.Singleton;
import io.mangoo.async.EventBusHandler;
import io.mangoo.interfaces.MangooBootstrap;

@Singleton
public class Shutdown extends Thread {
    public Shutdown() {
        // Empty constructor for Google Guice
    }

    @Override
    public void run() {
        invokeLifecycle();
        stopUndertow();
        stopScheduler();
        stopEmbeddedMongoDB();
        closeEventBus();
    }

    private static void invokeLifecycle() {
        Application.getInstance(MangooBootstrap.class).applicationStopped();
    }

    private static void stopScheduler() {
        if (Application.getInstance(Config.class).isSchedulerEnabled()) {
            Application.getScheduler().shutdown();
            Application.getExecutor().shutdown();
        }
    }

    private static void stopUndertow() {
        Application.stopUndertow();
    }
    
    private static void stopEmbeddedMongoDB() {
        Application.stopEmbeddedMongoDB();
    }

    private static void closeEventBus() {
        Application.getInstance(EventBusHandler.class).shutdown();;
    }
}