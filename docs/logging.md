# Logging

mangoo I/O utilizes **Log4j2** for logging. If you are familiar with Log4j2, creating a new logger instance is straightforward:

```java
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

...

private static final Logger LOG = LogManager.getLogger(MyClass.class);
```

## Log4j2 Configuration

The integration follows the [automatic configuration](https://logging.apache.org/log4j/2.x/manual/configuration.html) mechanism of Log4j2, which looks for specific configuration files during startup.

### Environment-Specific Configuration

To configure Log4j2 for different environments, use the following naming conventions:

- **Development and Testing:**
  ```
  log4j2-test.*
  ```
- **Production:**
  ```
  log4j2.*
  ```

### Filtering Test Configurations in Production

It is crucial to filter out test configurations during the JAR build process to prevent them from becoming active in a production environment. This can be achieved using the **maven-jar-plugin** with the following configuration:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.1.0</version>
    <configuration>
        <excludes>
            <exclude>**/log4j2-test*</exclude>
        </excludes>
    </configuration>
</plugin>
```

This ensures that any Log4j2 test configurations are not included in the final production JAR.
