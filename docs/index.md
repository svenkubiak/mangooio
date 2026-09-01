# Home

**mangoo I/O** is a full-stack Java web framework on [Undertow](http://undertow.io/). It follows MVC, uses standard libraries, and avoids hidden magic.

Start here: [Getting started](getting-started.md).

## Stack

- MVC on Undertow
- Google Guice for injection
- Freemarker templates
- MongoDB persistence
- Caffeine cache, Log4j2, Jackson

Each build is checked with a large [SonarQube](http://www.sonarqube.org/) rule set.

## Features

**Development**

- Convention over configuration
- Hot compile in development mode
- Programmatic routing
- Maven archetype and `mvn mangooio:run`

**HTTP and security**

- Fluent `Response` API
- Client-side sessions and flash
- Form validation
- CSRF, origin checks, API keys
- Cookie authentication with optional TOTP

**Data and background work**

- MongoDB datastore
- Built-in cache
- `@Run` scheduler
- EventBus
- Asynchronous email

**Operations**

- Admin dashboard
- Optional metrics and OpenTelemetry
- Fat-JAR deployment

See the [changelog](changelog.md) and [migrations](migrations.md) when you upgrade.
