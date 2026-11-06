# Logging

Application code uses **Log4j2** through the standard API, nothing framework-specific to learn. The framework and Undertow log through the same pipeline, so one `log4j2.xml` controls verbosity for both your controllers and the HTTP stack underneath them. A `log4j2.yaml` or `log4j2.json` is ignored, as Log4j needs Jackson 2 for these formats and mangoo I/O ships Jackson 3.

Use a **static logger per class**. It is cheap to create and easy to filter selectively in configuration once you have more than a couple of classes. Avoid logging secrets, full session cookies, or raw passwords, even at debug level, since debug logging has a way of ending up enabled in production during an incident.

```java
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AccountService {
    private static final Logger LOG = LogManager.getLogger(AccountService.class);
}
```

## Configuration files

Log4j2 [automatic configuration](https://logging.apache.org/log4j/2.x/manual/configuration.html) applies:

- `log4j2-test.*` for tests (and anything that puts that file first on the classpath)
- `log4j2.*` for production

Keep `log4j2-test.xml` out of the fat JAR, or you risk shipping test-level verbosity to production. The archetype already excludes it via `maven-jar-plugin`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>**/log4j2-test*</exclude>
        </excludes>
    </configuration>
</plugin>
```

With several instances, add the instance id to the pattern, so a central log shows which instance wrote a line. It is available as the system property `mangooio.instance.id` before Log4j reads its configuration:

```xml
<PatternLayout pattern="%d{HH:mm:ss.SSS} [${sys:mangooio.instance.id}] [%t] %-5level %logger{36} - %msg%n" />
```

mangoo I/O stops Log4j itself as the last step of the [graceful shutdown](operating.md#graceful-shutdown), so the shutdown is logged completely; Log4j's own shutdown hook is disabled.

On startup, mangoo I/O checks that every appender referenced by a logger actually exists and has started. Warnings from that check are treated as configuration errors rather than ignored, so a broken logging config fails loudly instead of silently dropping log lines.
