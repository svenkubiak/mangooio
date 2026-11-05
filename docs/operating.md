# Operating

mangoo I/O applications ship as a **fat JAR** built with the Maven shade plugin. One process runs Undertow, your controllers, and the scheduler, so there is no separate WAR deployment step. Production runs use **Java 25** and **prod** mode, against an external MongoDB. Embedded MongoDB still starts in prod, but the application logs a warning: each instance gets its own database and shares no lockouts, revocations, or events with the others.

Secrets come from the host through `env{}`, `arg{}`, or `file{}`. In prod mode the application refuses to start when a framework secret is written literally into the `config.yaml` on the classpath. A file passed with `-Dapplication.config` is not checked that way. Empty values pass. A fallback written inside `env{...}` still counts as a literal. The checked keys are `application.secret`, `application.admin.secret`, `application.api.key`, the session, authentication, and flash cookie secrets and keys, `smtp.password`, `connector.https.password`, and any `persistence.*.mongo.password`. `application.admin.password` is an Argon2 hash and is not in that list. The YAML shape is the same in every environment; what changes is the active block and where the secret values come from.

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

`java -jar` starts in prod mode. `-Dapplication.mode` accepts `dev` and `test`, case-insensitively. Any other value, including `staging`, is prod. `mvn mangooio:run` forces dev mode on the child process.

Configure at least one HTTP or HTTPS connector. A connector starts only when both host and port are set. Zero connectors exits the process. HTTPS reads a PKCS12 file from `connector.https.keystore` and speaks TLS 1.3 only. The alias is required only when the file has more than one key entry.

On an operating system whose name does not start with `Windows`, prod mode runs `id -u` and exits when the uid is `0`. Run the process as an unprivileged user, and give that user read access to the secrets directory if you use `file{}`. If `id` is missing, the failure is logged and startup continues.

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
RUN groupadd --system --gid 10001 app && useradd --system --uid 10001 --gid app app
WORKDIR /app
COPY target/myapp.jar myapp.jar
USER app
ENTRYPOINT ["java", "-Dapplication.mode=prod", "-jar", "/app/myapp.jar"]
```

Containers run as root unless the image or the compose file sets a user. Keep the `USER` line. On an image that has `id` and starts as uid 0, prod mode exits. Pass heap settings through `JAVA_TOOL_OPTIONS`. Docker secrets land in `/run/secrets`, which is the default directory for `file{}`. Override it with `APPLICATION_SECRETS_PATH` or `-Dapplication.secrets.path`. A secret file may be at most 64 KiB, and a trailing newline is stripped. See [Secrets](secrets.md#docker-compose) for the compose file, `config.yaml`, and permissions.

Environment variables with `env{}` work as well, preferably with `--env-file` rather than `-e`, but stay visible in `docker inspect` and to every child process.

## Health checks

Two endpoints answer load balancer and orchestrator probes. They sit in front of metrics, cookies, and filters, return no body, send `Cache-Control: no-store`, and need no configuration. Only `GET` and `HEAD` are probes. Any other method falls through to normal routing. Security headers are not added.

| Endpoint | `200` | `503` |
|----------|-------|-------|
| `/@health/live` | the connector accepts the connection, including during shutdown | - |
| `/@health/ready` | startup has finished | during startup and from the first step of shutdown |

Readiness flips to true only after Undertow is listening, the shutdown hook is registered, `Datastore.isHealthy()` has succeeded, and `applicationStarted()` has returned. Before the port listens, a probe fails at TCP, not with `503`.

MongoDB is not part of the readiness flag. If persistence is on (the default) and the ping fails, the process exits 1 after the port is already open, while readiness is still `503`. `persistence.enable: false` makes `isHealthy()` return true without a client. A MongoDB outage after startup does not take the instance out of rotation. If it did, every instance would fail the probe at once and the load balancer would have nowhere to send traffic.

The port in the sample below is `8080`, the archetype `default`. The generated `environments.dev`, `test`, and `prod` blocks set the port to `9090`. Probe the port you actually bind.

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

On `SIGTERM` or `SIGINT` the JVM shutdown hook runs, in this order:

1. Reports not ready on `/@health/ready` and starts the shutdown clock.
2. Stops the admin heartbeat. That step is capped at 2 seconds, so a dead MongoDB does not hold the shutdown. A failed delete leaves the row in `mangooio_instances` until `expireAt`, one minute after the last heartbeat.
3. Stops scheduling further `@Run` tasks. A task that is already running is not interrupted yet.
4. Closes SSE connections, including `/@admin/stream`, and WebSocket connections (`GOING_AWAY`, reason `Server shutdown`). Clients reconnect to another instance.
5. Answers new requests with `503` and waits for the ones already in flight, using whatever is left of the deadline.
6. Stops Undertow, then waits for running `@Run` tasks. At the deadline those tasks are interrupted. The log lines are `Open requests did not finish within application.shutdown.timeout` and `Scheduled tasks did not finish within application.shutdown.timeout and are interrupted`.
7. Stops the `MessageBroker` and calls `applicationStopped()` while the MongoDB clients are still open.
8. Closes the clients, stops embedded MongoDB if it was started, flushes OpenTelemetry (5 seconds), and stops Log4j. Log4j's own shutdown hook is disabled so this order holds.

`application.shutdown.timeout` is 20 seconds by default and starts at step 1, so the heartbeat stop and the connection close already consume it. A negative value is treated as 0. The application does not wait for the load balancer to notice that the instance is going away. Add that delay in front, for example with a Kubernetes `preStop` hook, and keep `terminationGracePeriodSeconds` above `preStop` plus `application.shutdown.timeout`:

```yaml
terminationGracePeriodSeconds: 40
containers:
  - name: myapp
    lifecycle:
      preStop:
        sleep:
          seconds: 10
```

## Running several instances

Several instances of one application can run behind a load balancer. Session data and the CSRF token live in the session cookie. A kept form lives in the flash cookie. Both cookies, and the authentication cookie, are encrypted and signed JWTs, so a request can reach any instance. Every instance needs the same secrets, the same cookie names, and the same `application.name`, which is the JWT issuer.

Every instance logs its id at startup: `HOSTNAME` if that variable is set, otherwise the host part of the JVM runtime name, plus a dash and six hex characters. The [admin dashboard](administration.md) and the OpenTelemetry resource (`service.instance.id`) carry the same id. Add it to the log pattern with `${sys:mangooio.instance.id}`, see [Logging](logging.md). Dashboard metrics are per instance. OpenTelemetry metrics are aggregated by the collector, see [Observability](observability.md).

* **Secrets.** Pass them with `env{}`, `arg{}`, or `file{}`. See [Secrets](secrets.md).
* **Clocks.** Cookies are checked against the clock of the instance that receives them, with 30 seconds of skew on `exp`, `nbf`, and `iat`. New tokens are issued with `nbf` set 30 seconds in the past. Keep the clocks in sync with NTP.
* **MongoDB.** Use one external MongoDB for all instances. Embedded MongoDB starts a separate database per instance, and prod mode only warns about that. A replica set or sharded cluster is `persistence.mongo.url`. See [Persistence](persistence.md). MongoDB older than 4.2 fails startup. Framework collections use a primary read preference and majority write concern, whatever the connection string says. Application queries on `Datastore` do not.
* **Scheduler.** `@Run` runs on every instance. A job that must run once has no `@Run`. Expose it as a route protected with [`ApiKeyFilter`](filters.md#built-in-filters) and call it from an external trigger, such as a Kubernetes `CronJob`, through the load balancer. A retry after a timeout runs the job again, so keep it idempotent and answer a long job with `202`. Set `scheduler.enable: false` only when no job should run on the instances. Overlapping runs of the same job do not happen: the next run is scheduled after the previous one finishes. The first run waits for the full interval. It does not run at startup.
* **Startup logic.** `applicationStarted()` runs on every instance. Migrations and seed data have to tolerate concurrent runs. The port is already open while that method runs. Readiness stays `503` until it returns, which keeps a probe from sending traffic, but a client that already knows the address can arrive before the seed inserts.
* **Cache.** The application cache is local to each instance. Bind your own `Cache` if entries or counters must be shared.
* **[EventBus](async.md).** Subscribers run on the instance that published the event, one after another on a single virtual thread. The publisher is not blocked. The next subscriber is.
* **Server-Sent Events.** [`ServerSentEventManager.send`](sse.md) delivers to local connections and, when persistence is on, inserts into the capped collection `mangooio_messages` (16 MiB). Other instances tail that collection from the end. There is no replay. A failed insert means the other instances never see the event. The connections stay on the instance that accepted them.
* **WebSockets.** The framework holds the connections and closes them on shutdown. There is no fan-out. Publish on `io.mangoo.interfaces.MessageBroker` and subscribe on each instance to forward to its own channels. SSE and WebSocket routes are public: no authentication, no filters, no CSRF. See [Routing](routing.md).
* **Authentication state.** Failed logins, used TOTP codes, and the pending admin second factor are shared through MongoDB (`mangooio_attempts`, `mangooio_claims`) when persistence is on. If MongoDB is down, a login or a second factor is refused. Revocation of authentication cookies is separate and off by default. Set `authentication.blacklist` to store revocations in `mangooio_revocations` and to reject a revoked cookie. With the default `false`, a MongoDB outage does not invalidate cookies that are already issued. In-memory stores, used when `persistence.enable` is `false`, are wiped on restart: a lock or a revocation is forgotten. See [Framework collections](persistence.md#framework-collections).
* **Admin dashboard.** Every instance reports every ten seconds while the dashboard is enabled. The list shows the last heartbeat of each instance. An instance with no heartbeat for 30 seconds is marked stale and drops out of the list after a minute. Cache clear and metrics reset are published on the `MessageBroker` and run on every instance. The blocks under the instance list are the instance that served the page. Request counts stay at zero unless `metrics.enable` is true, and the error count is HTTP 5xx only.
* **Indexes.** A changed `@Indexed` definition fails index creation and the new instance does not start. During a rolling deploy, drop the old index before the new version starts. `persistence.mongo.schema: validate` creates nothing and exits with the mongosh commands that are missing.

## Reverse proxy

`connector.proxy: true` inserts Undertow's proxy-peer handler, so the client address and `Request.getScheme()` follow `X-Forwarded-For` and `X-Forwarded-Proto`. The default is `false`. Turn it on only when every request passes a proxy that overwrites `X-Forwarded-For`, `X-Forwarded-Proto`, `X-Forwarded-Host`, and `X-Forwarded-Port`. Otherwise a client can set any address.

## Cookies

Session, authentication, flash, and locale cookies are `HttpOnly` and `Path=/`. Session and authentication `SameSite` must be `Strict` or `Lax`. Anything else, including `None`, fails startup. Flash and the locale cookie are always `Strict`. `Secure` defaults to false for session and authentication. The flash cookie follows `session.cookie.secure`. There is no `flash.cookie.secure` key. The archetype writes one, and it is ignored.

Startup logs a warning, also shown on the admin dashboard, when `Secure` is false, when a cookie name is still the default (`mangooio-auth`, `mangooio-session`, `mangooio-flash`), or when a cookie secret or key equals `application.secret`. The warning is logged in every mode.

There is no sliding session expiry. Rewriting the session cookie keeps the original `exp`.

The admin cookie is documented in [Administration](administration.md). In prod it is named `__Host-mangooio-admin` and is `Secure`, so a deployment on plain HTTP will not keep the operator signed in.

## Request size

These limits are applied before a controller runs:

| Limit | Default | Configurable |
|---|---|---|
| `undertow.maxentitysize` | 4 MiB (`4194304`) | yes |
| Undertow parameters | 1000 | no |
| Undertow header size | 8192 bytes | no |
| `form.maxfilesize` | 5 MiB per file | yes |
| Form files | 10 | no |
| Form parameters | 1000 | no |
| Form value length | 10 000 characters | no |
| Flash cookie name plus value | 4096 bytes | no |

Worker and I/O thread counts are Undertow's own defaults. The framework does not set them.

A form that exceeds the file, parameter, or value limits throws before the controller runs. The response is HTTP 400. An HTML client still receives the built-in 500 page. A JSON `Content-Type` receives `{"error":"<reason>"}`. The body is rejected earlier by `undertow.maxentitysize`, which at the default of 4 MiB is smaller than `form.maxfilesize`. Raise the body limit before a 5 MiB upload can succeed, and raise the proxy's body limit as well.

`smtp.protocol: smtp` fails startup in prod. `smtps` and `smtptls` are allowed. Connect and read timeouts default to 10 seconds and 30 seconds. `0` is rejected. Argon2 concurrency, memory, and the `503` returned when every hashing slot is busy are described in [Configuration](configuration.md#argon2-hashing).

## Global response headers

Controller responses, the 404 fallback, error pages, and authentication redirects send:

| Header | Value |
|---|---|
| `X-Content-Type-Options` | `nosniff` |
| `X-Frame-Options` | `DENY` |
| `X-XSS-Protection` | `1` |
| `Referrer-Policy` | `no-referrer` |
| `Server` | `Undertow` |

`Content-Security-Policy` and `Feature-Policy` are registered as empty strings and are not sent until you set a non-blank value. A blank value is dropped.

These headers are not applied to SSE, WebSocket handshakes, static files from `Bind.pathResource()` and `Bind.fileResource()`, or the early 400 for a repeated query key or, with `application.parameter.strict`, a query key that collides with a path parameter.

Override or add to them in `Bootstrap`:

```java
import io.mangoo.constants.Header;
import io.mangoo.core.Server;

Server.header(Header.CONTENT_SECURITY_POLICY, "default-src 'self'");
```

## Maven plugin

```shell
mvn mangooio:run
```

The plugin does not compile. It watches `target/classes` (and any extra directories you configure) and restarts a child JVM after the files settle. The settle delay is `mangoo.settleDownMillis`, default 500 ms. The child is `io.mangoo.core.Application` with `-Dapplication.mode=dev`. Optional `mangoo.jvmArgs`, and `mangoo.jpdaPort` for JDWP with `suspend=n`. A port of `0`, the default, opens no debugger.

`templates/**/*.ftl`, `*.less`, `*.sass`, and `assets/**` are excluded from the watch. In dev mode Freemarker reloads a template about once a second, so a template change does not need a process restart. The shade plugin that builds the fat JAR is in the archetype POM, not a goal of this plugin. There is no framework Docker image.

See [Observability](observability.md) for metrics and tracing, and [Logging](logging.md) for Log4j.
