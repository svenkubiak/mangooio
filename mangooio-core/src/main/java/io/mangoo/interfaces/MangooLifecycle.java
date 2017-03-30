package io.mangoo.interfaces;

/**
 *
 * @author svenkubiak
 *
 */
public interface MangooLifecycle {
    /**
     * Executed after config is loaded and injector is initialized
     *
     */
    void applicationInitialized();

    /**
     * Executed after the application is completely started
     */
    void applicationStarted();
    
    /**
     * Executed after forcible signal of JVM shutdown has been send
     */
    void applicationStopped();
}