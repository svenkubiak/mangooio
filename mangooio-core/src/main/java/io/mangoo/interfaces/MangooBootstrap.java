package io.mangoo.interfaces;

public interface MangooBootstrap {
    void initializeRoutes();
    void applicationInitialized();
    void applicationStarted();
    void applicationStopped();
}