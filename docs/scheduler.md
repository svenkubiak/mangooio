# Scheduler

Background jobs run inside the same JVM as your web app—no separate Quartz XML or cron daemon required. Annotate any **public** method with **`@Run`** and a schedule expression; **Classgraph** discovers those methods at startup when `scheduler.enable` is `true` (the default).

Use fixed-rate expressions such as `Every 3m` or `Every 1d` for recurring work: cleanup tasks, cache warming, report generation. Jobs share Guice injection with the rest of the application, so you can inject services and `Datastore` like in a controller.

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

The prefix is case-insensitive (`every` / `Every`).

## Cron

Use a **5-field UNIX cron** expression (minute hour day-of-month month day-of-week):

```java
public class NightlyJob {
    @Run(at = "0 2 * * *")
    public void execute() {
        // 02:00 every day
    }
}
```

This is not Quartz cron: there is no seconds field.

Jobs run on a platform thread pool. Disable scheduling with:

```yaml
scheduler:
  enable: false
```
