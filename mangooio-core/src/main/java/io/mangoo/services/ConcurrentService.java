package io.mangoo.services;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Required;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class ConcurrentService {
    private final ExecutorService executorService;

    @Inject
    public ConcurrentService(Config config) {
        Objects.requireNonNull(config, Required.CONFIG.toString());
        this.executorService = Executors.newFixedThreadPool(config.getApplicationThreadpool());
    }

    /**
     * Submits a value-returning task for execution and returns a Future
     * representing the pending results of the task. The Future's get method
     * will return the task's result upon successful completion.
     *
     * @param <T> JavaDoc requires this (just ignore it)
     * @param callable The callable to submit
     *
     * @return a Future representing pending completion of the task
     */
    public <T> Future<T> submit(Callable<T> callable) {
        return this.executorService.submit(callable);
    }

    /**
     * Submits a Runnable task for execution and returns a Future representing
     * that task. The Future's get method will return the given result upon
     * successful completion.
     *
     * @param <T> JavaDoc requires this (just ignore it)
     * @param runnable the task to submit
     * @param result the result to return
     *
     * @return a Future representing pending completion of the task
     */
    public <T> Future<T> submit(Runnable runnable, T result) {
        return this.executorService.submit(runnable, result);
    }

    /**
     * Executes the given command at some time in the future.
     * The command may execute in a new thread, in a pooled thread,
     * or in the calling thread, at the discretion of the Executor implementation.
     *
     * @param runnable the runnable task
     */
    public void execute(Runnable runnable) {
        this.executorService.execute(runnable);
    }
    
    public void shutdown() {
        this.executorService.shutdown();
    }
}