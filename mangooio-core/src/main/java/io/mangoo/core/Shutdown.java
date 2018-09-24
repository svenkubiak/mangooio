package io.mangoo.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Singleton;
import com.netflix.governator.lifecycle.LifecycleManager;

import io.mangoo.exceptions.MangooSchedulerException;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.providers.CacheProvider;
import io.mangoo.scheduler.Scheduler;
import io.mangoo.services.ConcurrentService;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class Shutdown extends Thread {
    private static final Logger LOG = LogManager.getLogger(Shutdown.class);

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
        closeLifecycleManager();
    }

    private static void invokeLifecycle() {
        Application.getInstance(MangooBootstrap.class).applicationStopped();
    }

    private static void stopExecutionManager() {
        Application.getInstance(ConcurrentService.class).shutdown();
    }

    private static void stopScheduler() {
        Scheduler scheduler = Application.getInstance(Scheduler.class);
        try {
            if (scheduler != null && scheduler.isInitialize()) {
                scheduler.shutdown();
            }
        } catch (MangooSchedulerException e) {
            LOG.error("Failed to stop scheduler during application shutdown", e);
        }
    }

    private static void stopUndertow() {
        Application.stopUndertow();
    }

    private static void closeCaches() {
        Application.getInstance(CacheProvider.class).close();
    }

    private static void closeLifecycleManager() {
        Application.getInstance(LifecycleManager.class).close();
    }
}