# Getting started 🚀

This guide takes you from an empty machine to a running mangoo I/O application. You will generate a project with the official Maven archetype, start it in development mode, and get a tour of the files the archetype creates and why they look the way they do.

mangoo I/O bundles the things almost every web application eventually needs: routing, templating, sessions, authentication, a scheduler, i18n, and a way to keep secrets out of your config file. The archetype wires all of that up for you in a working skeleton, so instead of starting from a blank `main()` method you start from a small app that already renders a page, talks to a database, and passes a test. The idea is that you can explore routing, persistence, and testing right away, without writing scaffolding first.

## Prerequisites

Install a JDK and Maven and confirm the versions:

- **Maven** 3.9.10 or higher
- **Java** 25 or higher

```shell
java --version
mvn --version
```

mangoo I/O uses modern Java language features (virtual threads, pattern matching, and so on) and relies on the module path where required. Older JDKs simply will not compile the framework, so there is no fallback mode for Java 17 or 21.

## Create an application

Generate a new project from the archetype. Replace the version with the latest release from [Maven Central](https://central.sonatype.com/artifact/io.mangoo/mangooio):

```shell
mvn archetype:generate \
  -DarchetypeGroupId=io.mangoo \
  -DarchetypeArtifactId=mangooio-maven-archetype \
  -DarchetypeVersion=10.11.0
```

Maven will ask you a few questions interactively:

```
Define value for property 'groupId': com.example
Define value for property 'artifactId': mangoo-demo
Define value for property 'version' 1.0-SNAPSHOT: :
Define value for property 'package' com.example: :
Define value for property 'application-name': mangoo-demo
```

- **groupId** is your organization or package prefix, for example `com.example`.
- **artifactId** becomes the Maven module name and the folder the project lands in.
- **application-name** shows up in `config.yaml`, mostly as a prefix for cookie names (`mangoo-demo-session`, `mangoo-demo-authentication`, and so on), so pick something that will not collide once you deploy several apps side by side.

Once the archetype has generated the project, build it and start it:

```shell
cd mangoo-demo
mvn clean package
mvn mangooio:run
```

`mvn mangooio:run` does more than just launch a JVM. It forks your application as a separate process (so your terminal stays usable and you can attach a debugger to it), sets the system property that puts the app into **dev mode**, and starts a file watcher on your build output and source directories. When you save a Java file, the watcher notices the change, waits briefly for other saves to settle, and restarts the forked process automatically. That is the whole "save and refresh the browser" workflow: there is no separate build step you need to run by hand.

On first start you should see something like:

```
HTTP connector listening @127.0.0.1:9090
mangoo I/O application started in 9051 ms in dev mode. Enjoy.
```

Open [http://localhost:9090](http://localhost:9090). The sample page renders "Hello World!" from a Freemarker template. Visit `/persons` to see MongoDB persistence in action: the archetype seeds three `Person` documents into an embedded MongoDB instance on startup, and the `/persons` route lists them straight from the database.

The archetype sets `connector.http.port` to **9090** for both `dev` and `test`. That is a deliberate choice, not an accident: it means the port your browser hits while developing is the same one your integration tests hit, so "works on my machine" and "works in the test suite" mean the same thing. For production you configure connectors explicitly in `config.yaml` (see [Configuration](configuration.md) and [Operating](operating.md)).

Import the project into your IDE as a normal Maven module. Run tests with `mvn test`; they boot the app in **test** mode through `TestRunner`, using the same port and database configuration as `dev`, so a passing test suite is a decent predictor of what you will see in the browser.

## What happens on startup

Understanding the boot sequence helps once you start adding your own initialization code, because it tells you exactly which hook to use and in what order things become available:

1. **Mode** is decided first: `dev` (set by the Maven plugin), `test` (set by `TestRunner`), or `prod` (the default when you run the packaged JAR directly with `java -jar`).
2. **Config** loads next. `config.yaml` is parsed, and the `environments.<mode>` block is merged on top of `default`, key by key, so values you do not override in an environment simply fall back to the default.
3. **Vault** runs if `application.vault.enable` is `true`. A `vault.p12` keystore is created (on first run) or opened, and any config value written as `vault{}` is resolved through it. This exists so that cookie secrets and similar sensitive values never have to sit in `config.yaml` as plain text.
4. **Guice** builds the injector. Your `app.Module` class runs and binds `MangooBootstrap` to your `Bootstrap` implementation, plus whatever other services you have added.
5. **`applicationInitialized()`** is called on your bootstrap class. At this point config and dependency injection are ready, but no routes exist yet and the server is not listening.
6. **Routes** are registered by your bootstrap's `initializeRoutes()` method: controllers, static files, and any SSE or WebSocket endpoints.
7. **Classpath scan** picks up annotated classes: persistence entities (`@Collection`), scheduled jobs (`@Run`), and event subscribers.
8. **Connectors** start listening (HTTP, and HTTPS if you configured it).
9. **`applicationStarted()`** is called once the server is actually accepting connections. This is the right place to seed data, warm caches, or kick off background work, exactly what the archetype's `Bootstrap` does when it saves the sample `Person` records.
10. On shutdown, **`applicationStopped()`** runs, giving you a hook to close resources cleanly.

See [Bootstrap](bootstrap.md) for details on each hook.

## Hot compile

In development mode, saving a Java file triggers a recompile so you can refresh the browser without restarting the JVM by hand. This convenience comes with one sharp edge: controller methods that take request parameters rely on **parameter names being present in the compiled bytecode**, and Java does not keep them by default.

For example, a controller method like this:

```java
public Response show(@Param("id") long id) {
    ...
}
```

needs the compiler to actually record that the parameter is called `id`. Without that information, reflection only sees generic names like `arg0`, and mangoo I/O cannot match the `id` query parameter to the right method argument, so it silently comes back as the default value instead of what the user sent.

Maven already passes `-parameters` to the compiler for you (it is set in the archetype's `pom.xml`), so `mvn mangooio:run` just works. If your IDE compiles and runs the project itself (for example when you hit the run or debug button instead of going through Maven), you need to enable the same flag there:

**Eclipse:** Settings → Java → Compiler → enable "Store information about method parameters (usable via reflection)".

**IntelliJ IDEA:** Settings → Build, Execution, Deployment → Compiler → Java Compiler → Additional command line parameters: `-parameters`.

⚠️ If you forget this, the symptom is confusing: the app starts fine, pages render, but path and query parameters quietly show up as `null` or `0` only when you run from the IDE. If that happens and `mvn mangooio:run` works fine, this flag is almost always the reason.

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

**`app.Bootstrap`** implements `MangooBootstrap`. This is where routes get mapped and where the lifecycle hooks from the previous section live. The generated version looks like this:

```java
@Singleton
public class Bootstrap implements MangooBootstrap {
    private Datastore datastore;

    @Inject
    public Bootstrap(Datastore datastore) {
        this.datastore = Objects.requireNonNull(datastore, "datastore can not be null");
    }

    @Override
    public void initializeRoutes() {
        Bind.controller(ApplicationController.class).withRoutes(
                On.get().to("/").respondeWith("index"),
                On.get().to("/persons").respondeWith("persons")
        );

        Bind.pathResource().to("/assets/");
        Bind.fileResource().to("/robots.txt");
    }

    @Override
    public void applicationStarted() {
        datastore.save(new Person("Richard M.", "Whittaker", 33));
        datastore.save(new Person("Kitty D.", "Glenn", 45));
        datastore.save(new Person("Raul E.", "Kuhn", 46));
    }

    // applicationInitialized() and applicationStopped() are left empty,
    // ready for you to fill in
}
```

`Bootstrap` is a regular Guice-managed singleton, so it can constructor-inject a `Datastore` (or any other service you have bound) just like a controller can.

**`app.Module`** extends Guice's `AbstractModule`. Its only required job is binding `MangooBootstrap` to your bootstrap class:

```java
@Singleton
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MangooBootstrap.class).to(Bootstrap.class);
    }
}
```

That single binding is how mangoo I/O finds your application code at startup. Add further `bind(...)` calls here for your own services.

**`controllers.*`** are plain classes, no generated base class, no interface to implement. Methods return `Response` and can take injected parameters (see [Controllers](controllers.md)).

**`models.*`** hold persistence entities. The sample `Person` uses `@Collection(name = "persons")` and extends `Entity` to get a MongoDB `ObjectId` for free.

**`config.yaml`** uses a `default` section plus one block per environment. Here is the relevant slice from the archetype, trimmed down:

```yaml
default:
  connector:
    http:
      port: 8080

environments:
  dev:
    connector:
      http:
        port: 9090
    persistence:
      mongo:
        embedded: true
        port: 29019
```

Read this as "start from `default`, then apply whatever `environments.dev` overrides." So in `dev` mode the effective HTTP port is `9090`, not `8080`, even though `8080` is what `default` says. The archetype also enables the [vault](secrets.md) and references cookie secrets and keys as `vault{}`; a `vault.p12` file appears in your project root the first time you start the app.

**`templates/`** mirrors controller and method names: `ApplicationController/index.ftl` is what `Response.ok().render()` renders when it is called from `index()`.

**`translations/`** holds `messages.properties` and locale-specific variants for [Internationalization](internationalization.md).

## Next steps

Work through these topics in order, or jump straight to what you need:

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

When you are ready to deploy, read [Operating](operating.md) for fat-JAR packaging, environment variables for the vault, and Log4j configuration exclusions. ✅
