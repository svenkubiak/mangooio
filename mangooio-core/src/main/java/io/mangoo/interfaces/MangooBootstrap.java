package io.mangoo.interfaces;

/**
 * 
 * @author svenkubiak
 *
 */
public interface MangooBootstrap {
    
    /**
     * Place all routes for your application in this method
     */
    void initializeRoutes();
    
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