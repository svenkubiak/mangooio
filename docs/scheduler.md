mangoo I/O uses plain Java methods for creating and executing periodic tasks. The integration comes in two flavours: You can either create a repeating task (e.g. every 3m) or schedule a cron task. 

## Reaping task

To create a new task, create a simple Pojo and use the @Run annotation on the method you want to be executed.

```java
public class InfoJob {
    @Run(at = "Every 3m")
    public void execute() {
        //Do nothing for now
    }
}
```

The "at" parameter configures the repeating of the task. You can either use

* s = seconds ("Every 5s")
* m = minutes ("Every 15m")
* h = hours ("Every 4h")

## Cron task

To create a new cron task, create a simple Pojo and use the @Run annotation on the method you want to be executed.

```java
public class InfoJobCron {
    @Run(at = "0/1 * * * *")
    public void execute() {
        //do nothing for now
    }
}
```