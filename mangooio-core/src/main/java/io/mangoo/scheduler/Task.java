package io.mangoo.scheduler;

import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class Task implements Runnable {
    private static final Logger LOG = LogManager.getLogger(Task.class);
    private final Class<?> clazz;
    private final String methodName;
    
    public Task(Class<?> clazz, String methodName) {
        this.clazz = Objects.requireNonNull(clazz, Required.CLASS);
        this.methodName = Objects.requireNonNull(methodName, Required.METHOD);
    }

    @Override
    public void run() {
        try {
            Object instance = Application.getInstance(clazz);
            instance.getClass().getMethod(methodName).invoke(instance);
        } catch (InvocationTargetException e) {
            LOG.error("Failed to execute scheduled task on class '{}' with annotated method '{}'", clazz.getName(), methodName, e.getCause());
        } catch (Exception e) {
            // The method could not be invoked at all, e.g. it is not public or has parameters
            LOG.error("Failed to invoke scheduled task on class '{}' with annotated method '{}'", clazz.getName(), methodName, e);
        }
    }
}