# Scheduler

mangoo I/O enables task scheduling using plain Java methods. It supports two types of scheduled tasks: periodic (e.g., every 3 minutes) and cron-based execution.

## Repeating Task

To create a repeating task, define a simple POJO and annotate the method with `@Run` specifying the execution interval.

```java
public class InfoJob {
    @Run(at = "Every 3m")
    public void execute() {
        // Task logic here
    }
}
```

### Interval Configuration

Use the `at` parameter to specify the execution frequency:

- **s** = seconds (e.g., `"Every 5s"`)
- **m** = minutes (e.g., `"Every 15m"`)
- **h** = hours (e.g., `"Every 4h"`)

## Cron Task

To create a cron-based task, define a POJO and annotate the method with `@Run`, providing a cron expression.

```java
public class InfoJobCron {
    @Run(at = "0/1 * * * *")
    public void execute() {
        // Task logic here
    }
}
```

### Cron Expression

Cron expressions follow the standard format: `"seconds minutes hours day month day-of-week"`.

This approach provides flexible scheduling for tasks that require execution at specific times or intervals.
