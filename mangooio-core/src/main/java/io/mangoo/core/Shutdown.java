package io.mangoo.core;

import com.google.inject.Singleton;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.reactive.Stream;
import io.mangoo.reactive.beta.EventBusHandler;

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
        closeStreams();
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

    private static void closeStreams() {
        Application.getInstance(Stream.class).close();
    }

    private static void closeEventBus() {
        Application.getInstance(EventBusHandler.class).shutdown();;
    }
}