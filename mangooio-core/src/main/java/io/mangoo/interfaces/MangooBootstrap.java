package io.mangoo.interfaces;

public interface MangooBootstrap {
    
    void initializeRoutes();
    
    /**
     * Called after the config is loaded and the Guice injector is initialized.
     */
    void applicationInitialized();

    void applicationStarted();
    
    /**
     * Called from the JVM shutdown hook.
     */
    void applicationStopped();
}