package io.mangoo.core;

import com.google.inject.Singleton;

import io.mangoo.cache.CacheProvider;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.services.ConcurrentService;

/**
 * 
 * @author svenkubiak
 *
 */
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
        stopExecutionManager();
        closeCaches();
    }

    private static void invokeLifecycle() {
        Application.getInstance(MangooBootstrap.class).applicationStopped();
    }

    private static void stopExecutionManager() {
        Application.getInstance(ConcurrentService.class).shutdown();
    }

    private static void stopScheduler() {
        if (Application.getInstance(Config.class).isSchedulerEnabled()) {
            Application.getScheduler().shutdown();            
        }
    }

    private static void stopUndertow() {
        Application.stopUndertow();
    }

    private static void closeCaches() {
        Application.getInstance(CacheProvider.class).close();
    }
}