package io.mangoo.scheduler;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import io.mangoo.core.Application;
import io.mangoo.enums.Required;

public class CronTask implements Runnable {
    private static final Logger LOG = LogManager.getLogger(CronTask.class);
    private final Class<?> clazz;
    private final String methodName;
    private final ExecutionTime executionTime;
    
    public CronTask(Class<?> clazz, String methodName, String cron) {
        Objects.requireNonNull(cron, Required.CRON.toString());
        this.clazz = Objects.requireNonNull(clazz, Required.CLASS.toString());
        this.methodName = Objects.requireNonNull(methodName, Required.METHOD.toString());
        this.executionTime = ExecutionTime.forCron(new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)).parse(cron));
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        try {
            long delay = delay();
            if (delay > 0) {
                Task task = new Task(clazz, methodName);
                Application.getScheduler().schedule(task, delay, TimeUnit.SECONDS).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Failed to execute scheduled cron task on class '" + clazz.getName() + "' with annotated method '" + methodName + "'", e);
        }
        
        run();
    }
    
    private long delay() throws ExecutionException, InterruptedException {
        if (secondsToNextExecution() == 0) {
            try (ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory())) {
                executor.schedule(() -> {}, 1, TimeUnit.SECONDS).get();
            }
        }

        return secondsToNextExecution();
    }

    private long secondsToNextExecution() {
        return executionTime
                .timeToNextExecution(ZonedDateTime.now())
                .orElse(Duration.ofSeconds(-1))
                .getSeconds();
    }
}