# Getting started

This guide takes you from an empty machine to a running mangoo I/O application. You will generate a project with the official Maven archetype, start it in development mode, and get a short tour of the files the archetype creates.

mangoo I/O targets **Java 25** and **Maven 3.9.10+**. The archetype configures a vault for cookie secrets, embedded MongoDB for local dev, and a sample controller with Freemarker templates—enough to explore routing, persistence, and tests without writing scaffolding first.

## Prerequisites

Install a JDK and Maven and confirm the versions:

- **Maven** 3.9.10 or higher
- **Java** 25 or higher

```shell
java --version
mvn --version
```

mangoo I/O uses modern Java language features and the module path where required; older JDKs are not supported.

## Create an application

Generate a new project from the archetype. Replace the version with the latest release on [Maven Central](https://central.sonatype.com/artifact/io.mangoo/mangooio):

```shell
mvn archetype:generate \
  -DarchetypeGroupId=io.mangoo \
  -DarchetypeArtifactId=mangooio-maven-archetype \
  -DarchetypeVersion=10.11.0
```

The generator asks for:

- **groupId** — Your organization or package prefix (for example `com.example`).
- **artifactId** — The Maven module name and default folder name.
- **application name** — Used in configuration and cookie names.

Then build and run:

```shell
cd your-artifact-id
mvn clean package
mvn mangooio:run
```

The Maven plugin sets **dev mode**, compiles your sources, starts Undertow, and watches for file changes. On first start you should see something like:

```
HTTP connector listening @127.0.0.1:9090
mangoo I/O application started in 9051 ms in dev mode. Enjoy.
```

Open [http://localhost:9090](http://localhost:9090). The sample page renders “Hello World!” from a Freemarker template. Visit `/persons` to see MongoDB persistence in action—the archetype seeds a few `Person` documents on startup.

The archetype sets `connector.http.port` to **9090** in `dev` and `test`. For production you configure connectors explicitly in `config.yaml` (see [Configuration](configuration.md) and [Operating](operating.md)).

Import the project into your IDE as a normal Maven module. Run tests with `mvn test`; they use `TestRunner` to boot the app in **test** mode on the same port configuration.

## What happens on startup

Understanding the boot sequence helps when you add your own initialization code:

1. **Mode** — `dev` (Maven plugin), `test` (`TestRunner`), or `prod` (default for `java -jar`).
2. **Config** — `config.yaml` is loaded; `default` merges with `environments.<mode>`.
3. **Vault** — If enabled, `vault.p12` is created or loaded; cookie keys referenced as `vault{}` are resolved.
4. **Guice** — `app.Module` binds your `Bootstrap` and any custom services.
5. **Routes** — `Bootstrap.initializeRoutes()` registers controllers, static files, and optional SSE/WebSocket paths.
6. **Classpath scan** — Entities (`@Collection`), scheduled jobs (`@Run`), and event subscribers are registered.
7. **Connectors** — HTTP (and HTTPS if configured) start listening.

Hooks in `MangooBootstrap` let you run code after config load (`applicationInitialized`), after the server is up (`applicationStarted`), and on shutdown (`applicationStopped`). See [Bootstrap](bootstrap.md).

## Hot compile

In development mode, saving a Java file triggers a recompile so you can refresh the browser without restarting the JVM. Controller methods that take request parameters rely on **parameter names** at runtime. Maven already passes `-parameters` to the compiler; your IDE must do the same if you run or debug from there.

**Eclipse:** Settings → Java → Compiler → enable “Store information about method parameters (usable via reflection)”.

**IntelliJ IDEA:** Settings → Build, Execution, Deployment → Compiler → Java Compiler → Additional command line parameters: `-parameters`.

Without this flag, path and query parameters may not bind correctly when you run from the IDE, even though `mvn mangooio:run` works.

## Project layout

The archetype produces a layout that matches framework conventions:

```
.
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   ├── app
    │   │   │   ├── Bootstrap.java    # Routes and lifecycle
    │   │   │   └── Module.java       # Guice bindings
    │   │   ├── controllers
    │   │   │   └── ApplicationController.java
    │   │   └── models
    │   │       └── Person.java       # MongoDB entity
    │   └── resources
    │       ├── config.yaml           # Application configuration
    │       ├── files                 # Static assets (e.g. robots.txt)
    │       ├── log4j2.xml
    │       ├── log4j2-test.xml
    │       ├── templates             # Freemarker (.ftl)
    │       └── translations          # i18n message bundles
    └── test
        └── java
            └── controllers
                └── ApplicationControllerTest.java
```

**`app.Bootstrap`** implements `MangooBootstrap`. This is where you call `Bind.controller(...)` and map URLs. **`app.Module`** extends Guice’s `AbstractModule` and binds `MangooBootstrap` to your bootstrap class. Add further `bind(...)` calls for application services.

**`controllers.*`** are plain classes. Methods return `Response` and can take injected parameters (see [Controllers](controllers.md)). There is no generated base class.

**`models.*`** hold persistence entities. The sample uses `@Collection(name = "persons")` and extends `Entity` for the MongoDB `ObjectId` id.

**`config.yaml`** uses a `default` section plus `environments.dev|test|prod`. The archetype enables the [vault](secrets.md) and references `vault{}` for session, authentication, and flash cookie keys. A `vault.p12` file appears in the project root on first run.

**`templates/`** mirrors controller and method names: `ApplicationController/index.ftl` is rendered by `Response.ok().render()` from `index()`.

**`translations/`** holds `messages.properties` and locale-specific variants for [Internationalization](internationalization.md).

## Next steps

Work through these topics in order, or jump to what you need:

| Topic | Guide | What you will learn |
|---|---|---|
| Settings and secrets | [Configuration](configuration.md), [Secrets](secrets.md) | Keys, modes, vault, env vars, HTTPS |
| URLs and lifecycle | [Routing](routing.md), [Bootstrap](bootstrap.md) | Mapping paths, static files, SSE |
| Request handling | [Controllers](controllers.md) | Parameters, validation, `Response` |
| HTML | [Templating](templating.md) | Freemarker variables, CSRF tags, i18n |
| APIs | [Working with JSON](working-with-json.md) | Request/response JSON |
| Login and cookies | [Authentication](authentication.md), [Sessions](sessions.md) | Auth cookie, session data |
| Database | [Persistence](persistence.md) | Entities, queries, multiple connections |
| Quality | [Testing](testing.md) | `TestRequest`, `TestBrowser` |
| Production | [Operating](operating.md) | JAR, Docker, vault in prod |

When you are ready to deploy, read [Operating](operating.md) for fat-JAR packaging, environment variables for the vault, and Log4j configuration exclusions.
