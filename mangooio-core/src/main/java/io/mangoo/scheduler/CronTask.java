package io.mangoo.scheduler;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CronTask implements Runnable {
    private static final Logger LOG = LogManager.getLogger(CronTask.class);
    private final Class<?> clazz;
    private final String methodName;
    private final ExecutionTime executionTime;
    
    public CronTask(Class<?> clazz, String methodName, String cron) {
        Objects.requireNonNull(cron, NotNull.CRON);
        this.clazz = Objects.requireNonNull(clazz, NotNull.CLASS);
        this.methodName = Objects.requireNonNull(methodName, NotNull.METHOD);
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
        } catch (Exception e) {
            LOG.error("Failed to execute scheduled cron task on class '{}' with annotated method '{}'", clazz.getName(), methodName, e);
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