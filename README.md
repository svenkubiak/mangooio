[![Maven Central](https://img.shields.io/maven-central/v/io.mangoo/mangooio)](https://mvnrepository.com/artifact/io.mangoo/mangooio)
![SemVer](https://img.shields.io/badge/SemVer-2.0.0-green)
[![Buy Me a Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-%F0%9F%8D%BA-yellow)](https://buymeacoffee.com/svenkubiak)

# mangoo I/O

**mangoo I/O** is a lean, opinionated full-stack Java web framework that scales out securely, no assembly required. It builds HTTP applications, everything from server-rendered HTML sites to JSON APIs, on [Undertow](http://undertow.io/) and follows a classic MVC style: you map URLs to controller methods, return a `Response`, and optionally render Freemarker templates or send JSON.

Controllers live in a package you configure once, templates follow a predictable path, and sessions, flash, CSRF, and locale are method parameters. Routes are Java code in `app.Bootstrap`. Configuration is one YAML file. Dependency injection is [Google Guice](https://github.com/google/guice). The application module has to be named `app.Module`.

If you are new to the project, start with [Getting started](https://svenkubiak.github.io/mangooio/getting-started/). It walks through the Maven archetype, the project layout, and the first request.

Each release is validated against a large [SonarQube](http://www.sonarqube.org/) rule set. The core module stays compact: test support (`mangooio-test`) and tooling (`mangooio-maven-plugin`) live in their own artifacts.

## How a request flows

Every HTTP request passes through Undertow, then any filters you have registered, then a controller method you mapped in `app.Bootstrap`:

1. **Routing.** `Bootstrap.initializeRoutes()` registers URLs. A route points to a controller class and a method name.
2. **Bindings.** The framework injects path parameters, query values, JSON bodies, and objects such as `Request`, `Session`, `Form`, or `Authentication` into the method signature, based on the parameter types and names you declare.
3. **Response.** The method returns `Response.ok()`, `Response.redirect(...)`, or similar. You choose whether to render a template, send JSON, or just return a status.

```java
public Response index(Request request, Session session) {
    return Response.ok().render("username", session.get("username"));
}
```

Templates, cookies, and security headers are handled by the framework once you return the response.

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

## Features

### Development

- **Convention over configuration.** Controller package, template paths, and translation bundles follow fixed conventions.
- **Restart on compile in dev mode.** `mvn mangooio:run` does not compile. It restarts the process after `target/classes` changes, once the writes have settled (500 ms by default). Templates reload without a restart.
- **Programmatic routing.** All routes live in `Bootstrap.java`. There is no separate routes file.
- **Maven archetype.** Generates a working app with generated dev and test secrets, sample persistence, and a first test already in place.

### HTTP and security

- **Fluent `Response` API.** Status codes, headers, cookies, redirects, HTML, JSON, and binary bodies share one builder-style API.
- **Client-side sessions and flash.** Session and flash data live in encrypted and signed JWT cookies. Any instance with the same secrets, cookie names, and `application.name` can serve the next request. There is no sticky session and no shared session store.
- **Form validation.** The `Form` object uses the same validation helpers as regular request handling, with messages sourced from `messages.properties`.
- **CSRF and origin checks.** Filters for token validation and allowed `Origin` headers integrate with Freemarker directives.
- **Cookie authentication.** You verify credentials however you like; the framework issues and validates the auth cookie. Optional TOTP is available for two-factor login. Lockouts, a second factor, form limits, and Argon2 hashing are part of the framework. Token revocation across instances stays off until `authentication.blacklist` is set.

### Data and background work

- **MongoDB datastore.** Inject `Datastore`, map entities with `@Collection`, and query using the native driver API.
- **Built-in cache.** A Caffeine cache in the process. Login lockouts are kept in MongoDB when persistence is on, so instances share them.
- **`@Run` scheduler.** Fixed-delay (`Every 5m`) or cron-style jobs on ordinary classes, discovered through a classpath scan at startup, on every instance.
- **EventBus.** `publish` returns immediately. Subscribers for that payload then run one after another on a virtual thread, on the instance that published. Cross-instance delivery is the `MessageBroker`, used by SSE.
- **Asynchronous email.** `Mail.send()` does not block the request thread. Mail is sent on a virtual thread.

### Operations

- **Admin dashboard.** An optional `/@admin` UI for cache stats, the scheduler, and a few security helpers. In prod the cookie requires HTTPS.
- **Health and shutdown.** `/@health/live` and `/@health/ready` are always on. Shutdown starts with readiness going to `503` and waits up to `application.shutdown.timeout` (20 seconds).
- **Metrics and tracing.** Dashboard metrics are off until `metrics.enable`. OpenTelemetry export is off until `otlp.enable`.
- **Server-Sent Events** reach clients on other instances through MongoDB. WebSocket routes are Undertow handlers on the local instance.
- **Fat-JAR deployment.** The shade plugin produces a single runnable JAR. The [operating guide](https://svenkubiak.github.io/mangooio/operating/) covers supervisord, Docker, probes, cookies, and the request-size limits.

## Libraries

Here are some used libraries and their purpose in mangoo I/O.

* [Maven](https://maven.apache.org/): Dependency management, build system, packaging
* [Undertow](http://undertow.io/): Web Server
* [MongoDB](https://www.mongodb.com/): Persistence
* [Google Guice](https://github.com/google/guice): Dependency injection
* [Classgraph](https://github.com/classgraph/classgraph): Classpath scanning
* [Caffeine](https://github.com/ben-manes/caffeine): Caching
* [Log4j](http://logging.apache.org/log4j/2.x/): Logging
* [Freemarker](http://freemarker.org/): Template engine
* [Jackson](https://github.com/FasterXML/jackson): JSON handling
* [JUnit](https://junit.org/), [Mockito](https://site.mockito.org/): Testing
* [Bouncy Castle](https://www.bouncycastle.org/): Strong hashing and cryptography
* And many more …

## By the numbers

* 1,394 Sonar rules
* 10,000+ lines of core code
* 5,500+ commits
* 1,000+ unit tests
* 11+ years development maturity
* 223 cups of coffee (estimated)

## Resources

**Homepage**   
[https://github.com/svenkubiak/mangooio](https://github.com/svenkubiak/mangooio)

**Documentation**   
[https://svenkubiak.github.io/mangooio](https://svenkubiak.github.io/mangooio)  

**Changelog**   
[https://svenkubiak.github.io/mangooio/changelog/](https://svenkubiak.github.io/mangooio/changelog/)  

**Migrations**   
[https://svenkubiak.github.io/mangooio/migrations/](https://svenkubiak.github.io/mangooio/migrations/)

**Support**   
[https://github.com/svenkubiak/mangooio/issues](https://github.com/svenkubiak/mangooio/issues)
