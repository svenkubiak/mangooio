package io.mangoo.managers;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.inject.Singleton;

import io.mangoo.utils.ConfigUtils;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class ExecutionManager {
    private ExecutorService executorService = Executors.newFixedThreadPool(ConfigUtils.getExecutionPool());
    
    public <T> Future<T> submit(Callable<T> callable) {
        return this.executorService.submit(callable);
    }
    
    public <T> Future<T> submit(Runnable runnable, T result) {
        return this.executorService.submit(runnable, result);
    }
    
    public Future<?> submit(Runnable runnable) {
        return this.executorService.submit(runnable);
    }
    
    public void execute(Runnable runnable) {
        this.executorService.execute(runnable);
    }
}