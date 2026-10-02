# Scheduler

Background jobs run inside the same JVM as your web app, no separate Quartz XML or system cron daemon required. Annotate any **public method without parameters** with **`@Run`** and a schedule expression (the application refuses to start otherwise), and **Classgraph** discovers those methods at startup as long as `scheduler.enable` is `true`, which it is by default.

Use fixed-delay expressions such as `Every 3m` or `Every 1d` for recurring work like cleanup tasks, cache warming, or report generation. Jobs share Guice injection with the rest of the application, so you can inject services and `Datastore` exactly like you would in a controller.

## Fixed rate

```java
import io.mangoo.annotations.Run;

public class InfoJob {
    @Run(at = "Every 3m")
    public void execute() {
        // task
    }
}
```

Units after `Every`:

- `s` seconds (`Every 5s`)
- `m` minutes (`Every 15m`)
- `h` hours (`Every 4h`)
- `d` days (`Every 1d`)

The `Every` prefix is case-insensitive, so `every` and `Every` both work.

The interval is a delay between the end of one run and the start of the next, so runs of the same job never overlap. A run that takes longer than the interval simply delays the next one. Cron jobs behave the same way: the next run is scheduled once the current one has finished.

## Cron

Use a **5-field UNIX cron** expression: minute, hour, day-of-month, month, day-of-week.

```java
public class NightlyJob {
    @Run(at = "0 2 * * *")
    public void execute() {
        // 02:00 every day
    }
}
```

This is not Quartz cron; notably, there is no seconds field.

Jobs run on a platform thread pool. Disable scheduling entirely with:

```yaml
scheduler:
  enable: false
```
