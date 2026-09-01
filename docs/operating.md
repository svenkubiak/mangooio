# Operating

The Maven shade plugin packages a fat JAR. Run it on Java 25:

```shell
java -Dapplication.mode=prod -jar myapp.jar
```

Provide [vault](secrets.md) location and password in production:

```shell
export APPLICATION_VAULT_PATH=/var/lib/myapp
export APPLICATION_VAULT_SECRET='at-least-64-characters-of-entropy............................'
java -Dapplication.mode=prod -jar /opt/myapp/myapp.jar
```

Set `application.mode=prod` (the default if unset). Configure HTTP and/or HTTPS connectors. Do not enable embedded MongoDB.

## Supervisord

```ini
[program:myapp]
command=/usr/bin/java -Dapplication.mode=prod -jar /opt/myapp/myapp.jar
directory=/opt/myapp
autostart=true
autorestart=true
stderr_logfile=/var/log/myapp.err.log
stdout_logfile=/var/log/myapp.out.log
user=myuser
environment=JAVA_TOOL_OPTIONS="-Xms512m -Xmx1024m",APPLICATION_VAULT_PATH="/var/lib/myapp",APPLICATION_VAULT_SECRET="change-me"
```

`JAVA_OPTS` in `environment=` is not read by `java` unless you put it on the `command` line. `JAVA_TOOL_OPTIONS` is picked up automatically.

## Docker

```dockerfile
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/myapp.jar myapp.jar
ENTRYPOINT ["java", "-Dapplication.mode=prod", "-jar", "/app/myapp.jar"]
```

Pass heap settings with `JAVA_TOOL_OPTIONS`. Mount `vault.p12` and set `APPLICATION_VAULT_PATH` / `APPLICATION_VAULT_SECRET`.

## Global response headers

Default headers include `X-Content-Type-Options`, `X-Frame-Options`, `X-XSS-Protection`, `Referer-Policy`, and `Server`. Override or add them in `Bootstrap`:

```java
import io.mangoo.constants.Header;
import io.mangoo.core.Server;

Server.header(Header.CONTENT_SECURITY_POLICY, "default-src 'self'");
```

## Maven plugin

Development:

```shell
mvn mangooio:run
```

Minify `.js` and `.css` under `src/main/resources/files` (skips names that already contain `min`):

```shell
mvn mangooio:minify
```

See [Observability](observability.md) for metrics and tracing, and [Logging](logging.md) for Log4j.
