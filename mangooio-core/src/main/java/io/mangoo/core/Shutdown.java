package io.mangoo.core;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooSchedulerException;
import io.mangoo.interfaces.MangooLifecycle;
import io.mangoo.managers.ExecutionManager;
import io.mangoo.providers.CacheProvider;
import io.mangoo.scheduler.Scheduler;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class Shutdown extends Thread {
    private static final Logger LOG = LogManager.getLogger(Shutdown.class);
    private final Scheduler scheduler;
    
    @Inject
    public Shutdown(Scheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler, Required.SCHEDULER.toString());
    }
    
    @Override
    public void run() {
        invokeLifecycle();
        closeCaches();
        stopScheduler();
        stopUndertow();
        stopExecutionManager();
    }

    private void invokeLifecycle() {
        Application.getInstance(MangooLifecycle.class).applicationStopped();        
    }

    private void stopExecutionManager() {
        Application.getInstance(ExecutionManager.class).shutdown();
    }
    
    private void stopScheduler() {
        try {
            this.scheduler.shutdown();
        } catch (MangooSchedulerException e) {
            LOG.error("Failed to stop scheduler during application shutdown", e);
        }
    }
    
    private void stopUndertow() {
        Application.stopUndertow();
    }
    
    private void closeCaches() {
        Application.getInstance(CacheProvider.class).close();
    }
}