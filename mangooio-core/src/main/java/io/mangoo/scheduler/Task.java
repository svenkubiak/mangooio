package io.mangoo.scheduler;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.core.Application;
import io.mangoo.enums.Required;

/**
 * Proxy class for executing scheduled tasks
 * 
 * @author svenkubiak
 *
 */
public class Task implements Runnable {
    private static final Logger LOG = LogManager.getLogger(Task.class);
    private final Class<?> clazz;
    private final String methodName;
    
    public Task(Class<?> clazz, String methodName) {
        this.clazz = Objects.requireNonNull(clazz, Required.CLASS.toString());
        this.methodName = Objects.requireNonNull(methodName, Required.METHOD.toString());
    }

    @Override
    public void run() {
        try {
            Object instance = Application.getInstance(clazz);
            instance.getClass().getMethod(methodName).invoke(instance);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOG.error("Failed to execute scheduled task on class '%s' with annotated method '%s'", clazz.getName(), methodName, e);
        }
    }
}