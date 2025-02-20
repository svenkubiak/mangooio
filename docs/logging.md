mangoo I/O uses Log4j2 for logging. If you are familiar with Log4j2, creating a new logger instance is trivial.

```java
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

...

private static final Logger LOG = LogManager.getLogger(MyClass.class); 
```

The integration of Log4j2 is close to the standard usage of Log4j2. You are not bound to use any specific file extensions for configuring your logging. The integration follows the [automatic configuration](https://logging.apache.org/log4j/2.x/manual/configuration.html) of Log4j2, looking for specific files during the startup process. In order to configure Log4j2 to specific environment, your can use the following standard:

```
log4j2-test.*
```

for dev and test. And

```
log4j2.*
```

for production environments. 

This makes it important to filter the test Log4j2 test configuration during the JAR build. If the Log4j2 test configuration is not filtered during JAR build, it will become active in production. Filter any Log4j2 configuration that should not be in a production environment by using the maven-jar-plugin with the following configuration:

```properties
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



