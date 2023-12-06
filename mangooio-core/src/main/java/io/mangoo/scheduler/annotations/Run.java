package io.mangoo.scheduler.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Run {
    /**
     * Schedules a method either at a given rate (e.g. Every 1s, Every 5m, Every 8h, Every 1d)
     * or a given UNIX cron (e.g. 0/1 * * * *)
     * 
     * @see <a href="https://en.wikipedia.org/wiki/Cron">https://en.wikipedia.org/wiki/Cron</a>
     * 
     * @return Rate or UNIX cron for execution
     */
    String at();
}