# Scheduler

Annotate any public method with `@Run`. Classgraph finds those methods at startup when `scheduler.enable` is `true` (the default).

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
