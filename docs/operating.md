# Operating

mangoo I/O applications ship as a **fat JAR** built with the Maven shade plugin. One process runs Undertow, your controllers, and the scheduler, so there is no separate WAR deployment step to manage. Production runs use **Java 25**, **prod** mode, and an external MongoDB instance; embedded MongoDB is strictly for dev and test.

Plan for secrets living outside the JAR: the vault file (`vault.p12`), its password, and any `env{}` / `arg{}` overrides should come from the host or orchestrator, not from the artifact you build. The same `config.yaml` structure applies everywhere; only the active environment section and where the secrets come from actually change.

Run the packaged application:

```shell
java -Dapplication.mode=prod -jar myapp.jar
```

Provide [vault](secrets.md) location and password in production:

```shell
export APPLICATION_VAULT_PATH=/var/lib/myapp
export APPLICATION_VAULT_SECRET='at-least-64-characters-of-entropy............................'
java -Dapplication.mode=prod -jar /opt/myapp/myapp.jar
```

Set `application.mode=prod`, which is also the default if you leave it unset. Configure HTTP and/or HTTPS connectors as needed, and do not enable embedded MongoDB.

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

Note that `JAVA_OPTS` set in `environment=` is not read by `java` unless you also put it on the `command` line yourself. `JAVA_TOOL_OPTIONS`, by contrast, is picked up automatically.

## Docker

```dockerfile
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/myapp.jar myapp.jar
ENTRYPOINT ["java", "-Dapplication.mode=prod", "-jar", "/app/myapp.jar"]
```

Pass heap settings through `JAVA_TOOL_OPTIONS`, and mount `vault.p12` while setting `APPLICATION_VAULT_PATH` / `APPLICATION_VAULT_SECRET` accordingly.

## Global response headers

Default headers include `X-Content-Type-Options`, `X-Frame-Options`, `X-XSS-Protection`, `Referrer-Policy`, and `Server`. Override or add to them in `Bootstrap`:

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

Minify `.js` and `.css` under `src/main/resources/files`, skipping any filename that already contains `min`:

```shell
mvn mangooio:minify
```

See [Observability](observability.md) for metrics and tracing, and [Logging](logging.md) for Log4j.
