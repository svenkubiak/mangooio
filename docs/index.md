# Home 👋

**mangoo I/O** is a full-stack Java web framework for building HTTP applications, everything from server-rendered HTML sites to JSON APIs. It is built on [Undertow](http://undertow.io/) and follows a classic MVC style: you map URLs to controller methods, return a `Response`, and optionally render Freemarker templates or send JSON.

The framework favors **convention over configuration**. Controllers live in a package you configure once, templates follow a predictable path, and cross-cutting concerns like sessions, flash messages, CSRF tokens, and locale are just available as method parameters, no extra wiring required. At the same time, mangoo I/O tries hard not to be "magic": routes are plain Java code, configuration is a single YAML file you can read top to bottom, and dependency injection is just [Google Guice](https://github.com/google/guice), not a custom container reinventing what Guice already does well.

If you are new to the project, start with [Getting started](getting-started.md). It walks you through the Maven archetype, the default project layout, and your first request.

Compared with servlet-only stacks or the larger "batteries included" platforms, mangoo I/O is aimed at teams that want routing they can `grep` for, one configuration file instead of a dozen annotations spread across the codebase, and as little ceremony as possible for the web tasks that come up every day, without giving up mainstream, well-maintained libraries underneath. You stay close to the HTTP model: one method per URL, a return type you can actually read, and filters short enough to understand in an afternoon.

## How a request flows

At a high level, every HTTP request passes through Undertow, then any filters you have registered, then a controller method you mapped in `app.Bootstrap`:

1. **Routing.** `Bootstrap.initializeRoutes()` registers URLs. A route points to a controller class and a method name, nothing more indirect than that.
2. **Bindings.** The framework injects path parameters, query values, JSON bodies, and objects such as `Request`, `Session`, `Form`, or `Authentication` directly into the method signature, based on the parameter types and annotations you declare.
3. **Response.** The method returns `Response.ok()`, `Response.redirect(...)`, or similar. You choose whether to render a template, send JSON, or just return a status.

A minimal example ties these three steps together:

```java
public Response index(Request request, Session session) {
    return Response.ok().render("username", session.get("username"));
}
```

Templates, cookies, and security headers are handled by the framework once you return the response, so controller code stays focused on what your application actually does rather than on plumbing.

## What you get out of the box

mangoo I/O ships as a single Maven dependency (`mangooio-core`) plus optional modules for testing and the Maven plugin. You do not need to assemble a servlet container, wire a template engine, or pick a JSON library yourself. The defaults are already chosen and already wired together, which matters most in the first week of a new project, when you would otherwise be comparing four templating libraries before writing a single line of business logic.

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

You can replace or extend pieces where it matters: custom filters, extra Guice bindings, even your own persistence layer if you decide not to use MongoDB. But most applications run comfortably on the defaults, and that is very much the point.

## Stack and quality

The runtime stack is deliberately small and built entirely on widely used, independently maintained libraries, so you are never debugging framework-specific reinventions of problems the Java ecosystem already solved:

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
- **Hot compile in dev mode.** `mvn mangooio:run` watches your sources and recompiles changes quickly, usually within about a second, so the edit-save-refresh loop stays fast.
- **Programmatic routing.** All routes live in `Bootstrap.java`. There is no separate routes file that can drift out of sync with the code that actually handles the request.
- **Maven archetype.** Generates a working app with vault-backed cookie keys, sample persistence, and a first test already in place.

### HTTP and security

- **Fluent `Response` API.** Status codes, headers, cookies, redirects, HTML, JSON, and binary bodies all share one builder-style API, so you are not switching mental models depending on what you are returning.
- **Client-side sessions and flash.** Session and flash data live in signed JWT cookies, which fits a share-nothing deployment model where any instance can serve any request without sticky sessions or a shared store.
- **Form validation.** The `Form` object uses the same validation helpers as regular request handling, with messages sourced from `messages.properties` so error text stays translatable.
- **CSRF and origin checks.** Filters for token validation and allowed `Origin` headers integrate directly with Freemarker directives, so protecting a form is a template tag, not a manual token dance.
- **Cookie authentication.** You verify credentials however you like; the framework issues and validates the auth cookie for you. Optional TOTP is available for two-factor login.

### Data and background work

- **MongoDB datastore.** Inject `Datastore`, map entities with `@Collection`, and query using the native driver API rather than a framework-specific abstraction on top of it.
- **Built-in cache.** Named Caffeine caches cover application data, authentication lockout, and optional blacklist entries out of the box.
- **`@Run` scheduler.** Fixed-rate (`Every 5m`) or cron-style jobs on ordinary classes, discovered automatically through a classpath scan at startup.
- **EventBus.** Publish events to `Subscriber` implementations that run on virtual threads, so a slow subscriber does not block the publisher.
- **Asynchronous email.** Jakarta Mail integration where `Mail.send()` does not block the request thread, so sending a confirmation email never adds latency to the response the user is waiting on.

### Operations

- **Admin dashboard.** An optional `/@admin` UI for cache stats, scheduler overview, and a few security helpers, useful when you need a quick answer without opening a debugger.
- **Metrics and tracing.** Request metrics feed the dashboard, and optional OpenTelemetry export over OTLP hooks into whatever observability stack you already run.
- **Fat-JAR deployment.** The shade plugin produces a single runnable JAR; see [Operating](operating.md) for supervisord and Docker examples.

## Documentation map

The sidebar groups topics by concern. A typical learning path looks like this:

1. **[Getting started](getting-started.md)**: install Java 25 and Maven, generate a project, run it locally.
2. **[Configuration](configuration.md)** and **[Secrets](secrets.md)**: `config.yaml`, modes, vault, environment variables, HTTPS.
3. **[Bootstrap](bootstrap.md)** and **[Routing](routing.md)**: lifecycle hooks and URL mapping.
4. **[Controllers](controllers.md)** and **[Templating](templating.md)**: request handling, parameters, Freemarker.
5. **[Working with JSON](working-with-json.md)**: APIs and POJO binding.
6. **[Authentication](authentication.md)**, **[Sessions](sessions.md)**, **[CSRF](csrf.md)**: security building blocks.
7. **[Persistence](persistence.md)**: MongoDB entities and queries.
8. **[Testing](testing.md)** and **[Operating](operating.md)**: tests against a running app and production deployment.

Reference pages cover [filters](filters.md), [forms](forms.md), [flash](flash.md), [caching](caching.md), [emails](emails.md), [scheduler](scheduler.md), [SSE](sse.md), [async](async.md), [i18n](internationalization.md), [utils](utils.md), [logging](logging.md), [administration](administration.md), and [observability](observability.md).

## Upgrading

When you move between major or minor versions, read the [changelog](changelog.md) and [migrations](migrations.md) first. Breaking changes, for example the move from Cryptex to the vault, or from Paseto to JWT in the 10.x line, are documented there with pointers to whichever guide reflects the current behavior.

## Resources

- **Source and issues:** [github.com/svenkubiak/mangooio](https://github.com/svenkubiak/mangooio)
- **Maven coordinates:** `io.mangoo:mangooio-core` (see Maven Central for the latest version)
