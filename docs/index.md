# Home 👋

**mangoo I/O** is a lean, opinionated full-stack Java web framework that scales out securely, no assembly required. It builds HTTP applications, everything from server-rendered HTML sites to JSON APIs, on [Undertow](http://undertow.io/) and follows a classic MVC style: you map URLs to controller methods, return a `Response`, and optionally render Freemarker templates or send JSON.

Controllers live in a package you configure once, templates follow a predictable path, and sessions, flash, CSRF, and locale are method parameters. Routes are Java code in `app.Bootstrap`. Configuration is one YAML file. Dependency injection is [Google Guice](https://github.com/google/guice). The application module has to be named `app.Module`.

If you are new to the project, start with [Getting started](getting-started.md). It walks through the Maven archetype, the project layout, and the first request. The libraries underneath are Undertow, Guice, Freemarker, Jackson, the MongoDB driver, Caffeine, and Log4j2.

## How a request flows

At a high level, every HTTP request passes through Undertow, then any filters you have registered, then a controller method you mapped in `app.Bootstrap`:

1. **Routing.** `Bootstrap.initializeRoutes()` registers URLs. A route points to a controller class and a method name, nothing more indirect than that.
2. **Bindings.** The framework injects path parameters, query values, JSON bodies, and objects such as `Request`, `Session`, `Form`, or `Authentication` directly into the method signature, based on the parameter types and names you declare.
3. **Response.** The method returns `Response.ok()`, `Response.redirect(...)`, or similar. You choose whether to render a template, send JSON, or just return a status.

A minimal example ties these three steps together:

```java
public Response index(Request request, Session session) {
    return Response.ok().render("username", session.get("username"));
}
```

Templates, cookies, and security headers are handled by the framework once you return the response, so controller code stays focused on what your application actually does rather than on plumbing.

## What you get out of the box

mangoo I/O ships as `mangooio-core`, plus `mangooio-test` and the Maven plugin. Undertow, Freemarker, and Jackson are already wired. You can replace a binding in `app.Module`, including `Datastore`, when the default is the wrong one.

| Area | Built-in support |
|---|---|
| HTTP server | Undertow (HTTP and optional HTTPS) |
| Templates | Freemarker |
| JSON | Jackson |
| Persistence | MongoDB (Java sync driver) |
| Cache | Caffeine |
| DI | Google Guice |
| Logging | Log4j2 |
| Scheduling | `@Run` on plain Java methods |
| Real-time | Server-Sent Events; local WebSocket routes |
| Security | Encrypted cookies, CSRF, CORS, optional TOTP |

Custom filters and extra Guice bindings are the normal way to extend this. Most applications stay on the defaults above.

## Stack and quality

The runtime stack is small and built on libraries that are maintained outside this project:

- **Undertow** for non-blocking I/O and low overhead
- **Google Guice** for constructor injection and testability
- **Freemarker** for server-side HTML
- **MongoDB Java driver** for document storage with POJO codecs
- **Caffeine** for in-process caching
- **Log4j2** for logging
- **Jackson** (with Blackbird) for JSON

Each release is validated against a large [SonarQube](http://www.sonarqube.org/) rule set. The core module is kept intentionally compact: functionality that only matters for tests (`mangooio-test`) or for tooling (`mangooio-maven-plugin`) lives in its own artifact instead of bloating what every production app has to ship.

## Features in more detail

### Development

- **Convention over configuration.** Controller package, template paths, and translation bundles follow fixed conventions, so you spend less time on boilerplate and more time writing the parts unique to your app.
- **Restart on compile in dev mode.** `mvn mangooio:run` does not compile. It restarts the process after `target/classes` changes, once the writes have settled (500 ms by default). The JVM start is on top of that. Templates reload without a restart.
- **Programmatic routing.** All routes live in `Bootstrap.java`. There is no separate routes file that can drift out of sync with the code that actually handles the request.
- **Maven archetype.** Generates a working app with generated dev and test secrets, sample persistence, and a first test already in place.

### HTTP and security

- **Fluent `Response` API.** Status codes, headers, cookies, redirects, HTML, JSON, and binary bodies all share one builder-style API, so you are not switching mental models depending on what you are returning.
- **Client-side sessions and flash.** Session and flash data live in encrypted and signed JWT cookies. Any instance with the same secrets, cookie names, and `application.name` can serve the next request. There is no sticky session and no shared session store.
- **Form validation.** The `Form` object uses the same validation helpers as regular request handling, with messages sourced from `messages.properties` so error text stays translatable.
- **CSRF and origin checks.** Filters for token validation and allowed `Origin` headers integrate directly with Freemarker directives, so protecting a form is a template tag, not a manual token dance.
- **Cookie authentication.** You verify credentials however you like; the framework issues and validates the auth cookie for you. Optional TOTP is available for two-factor login.

### Data and background work

- **MongoDB datastore.** Inject `Datastore`, map entities with `@Collection`, and query using the native driver API rather than a framework-specific abstraction on top of it.
- **Built-in cache.** A Caffeine cache in the process. Login lockouts are kept in MongoDB when persistence is on, so instances share them. Token revocation is separate and off until `authentication.blacklist` is true.
- **`@Run` scheduler.** Fixed-delay (`Every 5m`) or cron-style jobs on ordinary classes, discovered automatically through a classpath scan at startup.
- **EventBus.** `publish` returns immediately. Subscribers for that payload then run one after another on a virtual thread, on the instance that published. Cross-instance delivery is the `MessageBroker`, used by SSE.
- **Asynchronous email.** Jakarta Mail integration where `Mail.send()` does not block the request thread, so sending a confirmation email never adds latency to the response the user is waiting on.

### Operations

- **Admin dashboard.** An optional `/@admin` UI for cache stats, the scheduler, and a few security helpers. In prod the cookie requires HTTPS.
- **Health and shutdown.** `/@health/live` and `/@health/ready` are always on. Shutdown starts with readiness going to `503` and waits up to `application.shutdown.timeout` (20 seconds).
- **Metrics and tracing.** Dashboard metrics are off until `metrics.enable`. OpenTelemetry export is off until `otlp.enable`.
- **Fat-JAR deployment.** The shade plugin produces a single runnable JAR. [Operating](operating.md) covers supervisord, Docker, probes, cookies, and the request-size limits.

## Documentation map

The sidebar groups topics by concern. A typical learning path looks like this:

1. **[Getting started](getting-started.md)**: install Java 25 and Maven, generate a project, run it locally.
2. **[Configuration](configuration.md)** and **[Secrets](secrets.md)**: `config.yaml`, modes, environment variables, secret files, HTTPS.
3. **[Bootstrap](bootstrap.md)** and **[Routing](routing.md)**: lifecycle hooks and URL mapping.
4. **[Controllers](controllers.md)** and **[Templating](templating.md)**: request handling, parameters, Freemarker.
5. **[Working with JSON](working-with-json.md)**: APIs and POJO binding.
6. **[Authentication](authentication.md)**, **[Sessions](sessions.md)**, **[CSRF](csrf.md)**: security building blocks.
7. **[Persistence](persistence.md)**: MongoDB entities and queries.
8. **[Testing](testing.md)** and **[Operating](operating.md)**: tests against a running app and production deployment.

Reference pages cover [filters](filters.md), [forms](forms.md), [flash](flash.md), [caching](caching.md), [emails](emails.md), [scheduler](scheduler.md), [SSE](sse.md), [async](async.md), [i18n](internationalization.md), [utils](utils.md), [logging](logging.md), [administration](administration.md), and [observability](observability.md).

## Upgrading

When you move between major or minor versions, read the [changelog](changelog.md) and [migrations](migrations.md) first. Breaking changes, for example the removal of the vault in 11.0, or from Paseto to JWT in the 10.x line, are documented there with pointers to whichever guide reflects the current behavior.

## Resources

- **Source and issues:** [github.com/svenkubiak/mangooio](https://github.com/svenkubiak/mangooio)
- **Maven coordinates:** `io.mangoo:mangooio-core` (see Maven Central for the latest version)
