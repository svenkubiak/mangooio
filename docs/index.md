# Home

**mangoo I/O** is a full-stack Java web framework for building HTTP applications, from server-rendered HTML sites to JSON APIs. It is built on [Undertow](http://undertow.io/) and follows a classic MVC style: you map URLs to controller methods, return a `Response`, and optionally render Freemarker templates or send JSON.

The framework favors **convention over configuration**. Controllers live in a package you configure once, templates follow a predictable path, and many cross-cutting concerns (sessions, flash messages, CSRF tokens, locale) are available as method parameters without extra wiring. At the same time, mangoo I/O avoids heavy “magic”: routes are defined in Java, configuration is a single YAML file, and dependency injection uses plain [Google Guice](https://github.com/google/guice).

If you are new to the project, start with [Getting started](getting-started.md). It walks you through the Maven archetype, the default project layout, and your first request.

Compared with servlet-only stacks or large “batteries included” platforms, mangoo I/O targets teams that want **explicit routing in Java**, a **single configuration file**, and **minimal ceremony** for common web tasks—without giving up mainstream libraries. You stay close to the HTTP model: one method per URL, clear return types, and filters you can read in an afternoon.

## How a request flows

At a high level, every HTTP request passes through Undertow, optional filters, and then a controller method you mapped in `app.Bootstrap`:

1. **Routing** — `Bootstrap.initializeRoutes()` registers URLs. A route points to a controller class and a method name.
2. **Bindings** — The framework injects path parameters, query values, JSON bodies, and objects such as `Request`, `Session`, `Form`, or `Authentication` into the method signature.
3. **Response** — The method returns `Response.ok()`, `Response.redirect(...)`, or similar. You choose whether to render a template, send JSON, or return a status only.

Templates, cookies, and security headers are handled by the framework once you return the response. That keeps controller code focused on application logic.

## What you get out of the box

mangoo I/O ships as a single Maven dependency (`mangooio-core`) plus optional modules for testing and the Maven plugin. You do not need to assemble a servlet container, wire a template engine, or pick a JSON library—the defaults are already chosen and integrated.

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
| Real-time | Server-Sent Events (SSE) |
| Security | Signed cookies, CSRF, CORS, optional TOTP |

You can replace or extend pieces where it matters (custom filters, Guice bindings, your own persistence layer if you disable MongoDB), but most applications run comfortably on the defaults.

## Stack and quality

The runtime stack is deliberately small and based on widely used libraries:

- **Undertow** for non-blocking I/O and low overhead
- **Google Guice** for constructor injection and testability
- **Freemarker** for server-side HTML
- **MongoDB Java driver** for document storage with POJO codecs
- **Caffeine** for in-process caching
- **Log4j2** for logging
- **Jackson** (with Blackbird) for JSON

Each release is validated against a large [SonarQube](http://www.sonarqube.org/) rule set. The core module stays compact by design; features that belong in tests (`mangooio-test`) or tooling (`mangooio-maven-plugin`) live in separate artifacts.

## Features in more detail

### Development

- **Convention over configuration** — Controller package, template paths, and translation bundles follow fixed conventions so you spend less time on boilerplate.
- **Hot compile in dev mode** — `mvn mangooio:run` watches sources and recompiles changes quickly, usually within about a second.
- **Programmatic routing** — All routes live in `Bootstrap.java`. There is no separate routes file to keep in sync with code.
- **Maven archetype** — Generates a working app with vault-backed cookie keys, sample persistence, and a first test.

### HTTP and security

- **Fluent `Response` API** — Status codes, headers, cookies, redirects, HTML, JSON, and binary bodies share one builder-style API.
- **Client-side sessions and flash** — Session and flash data live in signed JWT cookies, which suits a share-nothing deployment model.
- **Form validation** — The `Form` object includes the same validation helpers as request handling, with messages from `messages.properties`.
- **CSRF and origin checks** — Filters for token validation and allowed `Origin` headers integrate with Freemarker directives.
- **Cookie authentication** — You verify credentials; the framework issues and validates the auth cookie. Optional TOTP for two-factor login.

### Data and background work

- **MongoDB datastore** — Inject `Datastore`, map entities with `@Collection`, query with the native driver API.
- **Built-in cache** — Named Caffeine caches for application data, auth lockout, and optional blacklist entries.
- **`@Run` scheduler** — Fixed-rate (`Every 5m`) or cron-style jobs on ordinary classes; discovered via classpath scan.
- **EventBus** — Publish events to `Subscriber` implementations on virtual threads.
- **Asynchronous email** — Jakarta Mail integration; `Mail.send()` does not block the request thread.

### Operations

- **Admin dashboard** — Optional `/@admin` UI for cache stats, scheduler overview, and security helpers.
- **Metrics and tracing** — Request metrics for the dashboard; optional OpenTelemetry export over OTLP.
- **Fat-JAR deployment** — Shade plugin produces a single runnable JAR; see [Operating](operating.md) for supervisord and Docker examples.

## Documentation map

The sidebar groups topics by concern. A typical learning path:

1. **[Getting started](getting-started.md)** — Install Java 25 and Maven, generate a project, run it locally.
2. **[Configuration](configuration.md)** and **[Secrets](secrets.md)** — `config.yaml`, modes, vault, environment variables, HTTPS.
3. **[Bootstrap](bootstrap.md)** and **[Routing](routing.md)** — Lifecycle hooks and URL mapping.
4. **[Controllers](controllers.md)** and **[Templating](templating.md)** — Request handling, parameters, Freemarker.
5. **[Working with JSON](working-with-json.md)** — APIs and POJO binding.
6. **[Authentication](authentication.md)**, **[Sessions](sessions.md)**, **[CSRF](csrf.md)** — Security building blocks.
7. **[Persistence](persistence.md)** — MongoDB entities and queries.
8. **[Testing](testing.md)** and **[Operating](operating.md)** — Tests against a running app and production deployment.

Reference pages cover [filters](filters.md), [forms](forms.md), [flash](flash.md), [caching](caching.md), [emails](emails.md), [scheduler](scheduler.md), [SSE](sse.md), [async](async.md), [i18n](internationalization.md), [utils](utils.md), [logging](logging.md), [administration](administration.md), and [observability](observability.md).

## Upgrading

When you move between major or minor versions, read the [changelog](changelog.md) and [migrations](migrations.md). Breaking changes (for example the move from Cryptex to the vault, or Paseto to JWT in 10.x) are documented there with pointers to the current guides.

## Resources

- **Source and issues:** [github.com/svenkubiak/mangooio](https://github.com/svenkubiak/mangooio)
- **Maven coordinates:** `io.mangoo:mangooio-core` (see Maven Central for the latest version)
