# Logging

mangoo I/O uses Log4j2.

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

Keep `log4j2-test.xml` out of the fat JAR. The archetype already excludes it via `maven-jar-plugin`:

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

On startup mangoo I/O checks that appenders referenced by loggers exist and are started. Warnings from that check are treated as configuration errors.
