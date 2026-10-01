package io.mangoo.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Run {
    /** A fixed rate (e.g. "Every 5m", units s, m, h, d) or a UNIX cron expression (e.g. "0/1 * * * *"). */
    String at();
}