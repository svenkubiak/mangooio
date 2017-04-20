package io.mangoo.test.utils;

import java.util.stream.IntStream;

/**
 * 
 * @author svenkubiak
 *
 */
public class ConcurrentRunner {
    private Runnable runnable;
    private int threads = 10;
    
    public static ConcurrentRunner create() {
        return new ConcurrentRunner();
    }

    /**
     * Adds the runnable to the runner
     * 
     * @param runnable The runnable to execute
     * @return ThreadRunner instance
     */
    public ConcurrentRunner withRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }
    
    /**
     * Sets the number of threads to used when executed, defaults to 10
     * 
     * @param threads The number of threads
     * @return ThreadRunner instance
     */
    public ConcurrentRunner withThreads(int threads) {
        this.threads = threads;
        return this;
    }
    
    /**
     * Starts instances of the given runnable depending on the
     * number of threads to execute
     */
    public void run() {
        IntStream.range(0, threads).parallel().forEach(i -> {
            Thread thread = new Thread(this.runnable);
            thread.start();
        });
    }
}