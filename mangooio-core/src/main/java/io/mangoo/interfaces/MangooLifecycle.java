package io.mangoo.interfaces;

/**
 *
 * @author svenkubiak
 *
 */
public interface MangooLifecycle {
    /**
     * Executed before Bootstrapping starts, right after Framework main method is invoked
     * Neither Google Guice injector or any other Framework helpers are available at this time!
     */
    void applicationInvoked();
    /**
     * Executed after config is loaded and Google Guice injector is initialized
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