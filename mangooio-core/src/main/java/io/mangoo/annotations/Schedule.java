package io.mangoo.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Scheduler annotation for scheduling jobs
 *
 * @author svenkubiak
 *
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Schedule {
    /**
     * Schedules the method either at a given rate (e.g. Every 1s, Every 5m, Every 8h, Every 1d)
     * or a given UNIX cron (e.g. 0/1 * * * *)
     * 
     * @see <a href="https://en.wikipedia.org/wiki/Cron">https://en.wikipedia.org/wiki/Cron</a>
     * 
     * @return Rate or UNIX cron for execution
     */
    String at();
    
    /**
     * The initial delay in seconds before the a scheduled method is first executed
     * Default is 0
     * 
     * @return Delay in seconds before first execution
     */
    long delay() default 0;
}