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
    public void applicationInitialized();

    /**
     * Executed after the application is completely started
     */
    public void applicationStarted();
}