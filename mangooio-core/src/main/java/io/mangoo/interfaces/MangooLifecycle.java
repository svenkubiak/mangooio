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
    
    /**
     * Executed after forcible signal of JVM shutdown has been send
     */
    public void applicationStopped();
    
    /**
     * Executed after the completion of a request
     * 
     * @param url The URL that was requested
     * @param status The returned HTTP status
     * @param processTime The time the request took processing in milliseconds
     * @param bytesSend The bytes send in the response
     */
    public void requestCompleted(String url, int status, int processTime, long bytesSend);
}