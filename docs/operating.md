# Operating

mangoo I/O applications ship as a **fat JAR** built with the Maven shade plugin. One process runs Undertow, your controllers, and the scheduler, so there is no separate WAR deployment step to manage. Production runs use **Java 25**, **prod** mode, and an external MongoDB instance; embedded MongoDB is strictly for dev and test.

Plan for secrets living outside the JAR: they come from the host or orchestrator through `env{}`, `arg{}` or `file{}`, not from the artifact you build, and the application refuses to start in prod mode if a framework secret is written literally into `config.yaml`. The same `config.yaml` structure applies everywhere; only the active environment section and where the secrets come from actually change.

Run the packaged application:

```shell
java -Dapplication.mode=prod -jar myapp.jar
```

Provide the [secrets](secrets.md) in production, for example as environment variables for keys set to `env{}`:

```shell
export APPLICATION_SECRET='exactly-64-bytes................................................'
export SESSION_COOKIE_SECRET='exactly-64-bytes................................................'
java -Dapplication.mode=prod -jar /opt/myapp/myapp.jar
```

Set `application.mode=prod`, which is also the default if you leave it unset. Configure HTTP and/or HTTPS connectors as needed, and do not enable embedded MongoDB.

In prod mode the application refuses to start when it runs as root (UID 0). Run it as an unprivileged user, which limits the damage of any code execution vulnerability. Make sure that user can read the secrets directory if you use `file{}`.

## Startup and classpath scanning

On startup the classpath is scanned for `@Run` jobs, `@Collection` models and `Subscriber` implementations, so these classes can live in any package of your application. Known library packages such as `org.apache`, `com.google`, `io.undertow` or `com.mongodb` are excluded from the scan to keep the startup fast, therefore do not place application classes in these packages. The excluded packages are logged on debug level.

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
environment=JAVA_TOOL_OPTIONS="-Xms512m -Xmx1024m",APPLICATION_SECRETS_PATH="/etc/myapp/secrets"
```

Note that `JAVA_OPTS` set in `environment=` is not read by `java` unless you also put it on the `command` line yourself. `JAVA_TOOL_OPTIONS`, by contrast, is picked up automatically.

## Docker

```dockerfile
FROM eclipse-temurin:25-jre
RUN groupadd --system app && useradd --system --gid app app
WORKDIR /app
COPY target/myapp.jar myapp.jar
USER app
ENTRYPOINT ["java", "-Dapplication.mode=prod", "-jar", "/app/myapp.jar"]
```

Containers run as root unless the image or the compose file sets a user, so keep the `USER` line, otherwise the application does not start in prod mode. Pass heap settings through `JAVA_TOOL_OPTIONS`. Docker secrets end up in `/run/secrets`, where `file{}` reads them. Use the long syntax and set `target` to the config key, so the file name inside the container does not depend on which characters Compose allows in secret names:

```yaml
services:
  myapp:
    secrets:
      - source: app_secret
        target: application.secret
      - source: session_secret
        target: session.cookie.secret
secrets:
  app_secret:
    file: ./secrets/application.secret
  session_secret:
    file: ./secrets/session.cookie.secret
```

```yaml
application:
  secret: file{}
session:
  cookie:
    secret: file{}
```

Without Swarm, a Compose secret is a bind mount of the file: it keeps owner and permissions of the host, and the `uid`, `gid` and `mode` options of the long syntax only apply in Swarm. The file must therefore be readable by the container user (`app` above) and by nobody else, for example with `chmod 0440` and a group the container user belongs to, matched by numeric id. Docker Swarm stores secrets encrypted and mounts them on tmpfs; Podman does the same with `podman secret create` and `--secret <name>,target=<config key>`.

Without Compose, mount a directory with one file per key read-only, `-v /etc/myapp/secrets:/run/secrets:ro`, or point `APPLICATION_SECRETS_PATH` to another directory. Environment variables with `env{}` work as well, preferably with `--env-file` rather than `-e`, but stay visible in `docker inspect` and to every child process; see [Secrets](secrets.md).

## Health checks

Two endpoints answer load balancer and orchestrator probes. They are answered before metrics, cookies and filters, return no body and need no configuration:

| Endpoint | `200` | `503` |
|----------|-------|-------|
| `/@health/live` | the process is running | - |
| `/@health/ready` | the startup is complete | during startup and from the beginning of the shutdown |

MongoDB is deliberately not part of readiness. If it became unavailable, every instance would report not ready at the same time and the load balancer would have no target left. A startup without a reachable MongoDB fails anyway.

```yaml
livenessProbe:
  httpGet:
    path: /@health/live
    port: 8080
readinessProbe:
  httpGet:
    path: /@health/ready
    port: 8080
```

## Graceful shutdown

On `SIGTERM` the application

1. reports not ready on `/@health/ready`,
2. starts no further `@Run` tasks,
3. closes all SSE and WebSocket connections, clients reconnect to another instance,
4. answers new requests with `503` and waits for the open ones,
5. stops Undertow and waits for running `@Run` tasks, interrupting them at the deadline,
6. stops the `MessageBroker` and calls `applicationStopped()` of your `Bootstrap`, while the datastores are still open,
7. closes the MongoDB connections, stops telemetry and Log4j.

Steps 4 and 5 share `application.shutdown.timeout`, 20 seconds by default. The application does not wait for the load balancer to notice that the instance is going away; add that delay in front, for example with a Kubernetes `preStop` hook, and keep `terminationGracePeriodSeconds` above `preStop` plus `application.shutdown.timeout`:

```yaml
terminationGracePeriodSeconds: 40
containers:
  - name: myapp
    lifecycle:
      preStop:
        sleep:
          seconds: 10
```

## Reverse proxy

Behind a reverse proxy or load balancer the client address of a request and the scheme reported by `Request.getScheme()` are those of the proxy. Set `connector.proxy: true` to take them from the `X-Forwarded-For`, `X-Forwarded-Proto`, `X-Forwarded-Host` and `X-Forwarded-Port` headers instead. Only enable it if every request passes a proxy that overwrites these headers, otherwise clients can set any address.

## Running several instances

Several instances of one application can run behind a load balancer. Session, flash, form and CSRF token live in signed and encrypted cookies and authentication cookies are stateless, so a request can reach any instance. Every instance logs its instance id at startup, the hostname with a random suffix, and the [admin dashboard](administration.md) and the OpenTelemetry resource (`service.instance.id`) carry it as well; add it to the log pattern with `${sys:mangooio.instance.id}`, see [Logging](logging.md). The dashboard metrics are per instance, the OpenTelemetry metrics are aggregated by the collector, see [Observability](observability.md).

* **Secrets**: all instances need the same secrets; pass them with `env{}`, `arg{}` or `file{}`, see [Secrets](secrets.md).
* **Clocks**: authentication cookies are validated with the clock of the instance and a tolerance of 30 seconds, so keep the clocks in sync with NTP.
* **MongoDB**: use one external MongoDB for all instances. Embedded MongoDB starts a separate database per instance, the application warns about it in prod mode. A replica set or sharded cluster is configured with `persistence.mongo.url`, see [Persistence](persistence.md).
* **Scheduler**: `@Run` runs on every instance. A job that must run once per schedule has no `@Run`; expose it as a route protected for example with [`ApiKeyFilter`](filters.md#built-in-filters) and call it from an external trigger such as a Kubernetes `CronJob` through the load balancer. Delivery is at least once, a retry after a timeout runs the job again, so keep such jobs idempotent and start long jobs asynchronously with a `202` response. Set `scheduler.enable: false` only if no job has to run on every instance.
* **Startup logic**: `applicationStarted()` runs on every instance at the same time, so migrations and seed data must tolerate concurrent runs.
* **Cache**: the application cache is local to each instance. Bind your own `Cache` implementation, for example backed by Redis, if entries or counters must be shared.
* **[EventBus](async.md)**: events are processed by the subscribers of the instance that published them.
* **Server-Sent Events**: an event sent with [`ServerSentEventManager`](sse.md) reaches the clients of every instance through the `MessageBroker` (capped collection `mangooio_messages`, best effort, no replay); the connections stay local to an instance.
* **WebSockets**: the framework holds no WebSocket connections. Use `io.mangoo.interfaces.MessageBroker` to publish to all instances and subscribe on each instance to forward to its own connections.
* **Authentication state**: the failed login budget, used TOTP codes, the pending second factor of the admin login and the revocation of authentication cookies (`authentication.blacklist`) are kept in MongoDB (`mangooio_attempts`, `mangooio_claims`, `mangooio_revocations`) and shared by all instances. Reads go to the primary and writes are acknowledged by the majority, whatever `persistence.mongo.url` sets. If MongoDB is unavailable, logins, second factors and authenticated requests are refused. MongoDB 4.2 or later is required. All collections and the required database role are listed in [Framework collections](persistence.md#framework-collections).
* **Admin dashboard**: every instance reports its status every ten seconds to `mangooio_instances`, the dashboard lists all instances with their requests and errors of the last five minutes; an instance without heartbeat disappears after a minute. Clearing a cache and resetting the metrics run on every instance through the `MessageBroker`. All other figures are those of the instance that serves the page.
* **Indexes**: a changed `@Indexed` definition fails the index creation, and a new instance does not start. During a rolling deploy, drop the old index before the new version starts.

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

See [Observability](observability.md) for metrics and tracing, and [Logging](logging.md) for Log4j.
