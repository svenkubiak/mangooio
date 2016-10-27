package io.mangoo.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Singleton;

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
    
    public Shutdown() {
        //Empty constructor for Google Guice
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
        Scheduler scheduler = Application.getInstance(Scheduler.class);
        try {
            if (scheduler != null && scheduler.isInitialize()) {
                scheduler.shutdown();                
            }
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