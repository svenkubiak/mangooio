# Getting started 🚀

This guide takes you from an empty machine to a running mangoo I/O application. You will generate a project with the official Maven archetype, start it in development mode, and get a tour of the files the archetype creates and why they look the way they do.

The archetype generates a small application that already renders a page, talks to MongoDB, and has one test. Routing, templating, sessions, authentication, the scheduler, i18n, and secret placeholders are wired up. You can change them from there.

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
  -DarchetypeVersion=11.0.0
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

`mvn mangooio:run` does not compile. It forks a child JVM in **dev** mode and watches `target/classes`. When the compiled classes change, because the IDE built them or you ran `mvn compile`, the watcher waits for the writes to settle (500 ms by default) and restarts the process. A debugger port is opened only when `-Dmangoo.jpdaPort` is greater than 0.

On first start you should see something like:

```
HTTP connector listening @localhost:9090
mangoo I/O application started in 9051 ms in dev mode as instance 'localhost-a1b2c3'. Enjoy.
```

The host in that line is `connector.http.host` from `config.yaml`. The archetype sets `localhost`, not `127.0.0.1`. The instance id is the hostname plus six hex characters.

Open [http://localhost:9090](http://localhost:9090). The sample page renders "Hello World!" from a Freemarker template. Visit `/persons` to see MongoDB persistence in action: the archetype seeds three `Person` documents into an embedded MongoDB instance on startup, and the `/persons` route lists them straight from the database.

The archetype sets `connector.http.port` to **9090** for both `dev` and `test`. That is a deliberate choice, not an accident: it means the port your browser hits while developing is the same one your integration tests hit, so "works on my machine" and "works in the test suite" mean the same thing. For production you configure connectors explicitly in `config.yaml` (see [Configuration](configuration.md) and [Operating](operating.md)).

Import the project into your IDE as a normal Maven module. Run tests with `mvn test`; they boot the app in **test** mode through `TestRunner`, using the same port and database configuration as `dev`, so a passing test suite is a decent predictor of what you will see in the browser.

## What happens on startup

The order matters once you add your own startup code:

1. **Mode.** `dev` when the Maven plugin sets `-Dapplication.mode=dev`, `test` when `TestRunner` calls `Application.start(Mode.TEST)` and that property is unset, otherwise `prod`. `java -jar` calls `main()`, which starts prod. Any mode value other than `dev` or `test` is prod.
2. **Injector.** Building the core Guice module constructs `Config`, which parses `config.yaml`, merges `environments.<mode>` over `default`, and resolves `env{}`, `arg{}`, and `file{}`. Then `app.Module` is loaded by that exact class name. It needs a public no-arg constructor. Bindings in `app.Module` override the core bindings.
3. **`applicationInitialized()`.** Config values can be read and injection works. Validation has not run yet, and there are no routes. A bad secret still reaches this hook. The process aborts in the next step, and the later hooks do not run.
4. **Validation.** Secret lengths, SameSite, SMTP in prod, and the other startup checks.
5. **Classpath scan**, started on a virtual thread: `@Collection`, `@Run`, and `Subscriber` types.
6. **`initializeRoutes()`**, while that scan is still running.
7. Routes are installed, the scan is joined, the message broker starts, and the connectors start listening.
8. **Datastore health check.** A failed ping aborts startup. The port may already be open. Readiness is still `503`.
9. **`applicationStarted()`.** Seed data and cache warming go here, which is what the archetype does with the sample `Person` rows. The port is open before this method returns, so a client that does not wait for readiness can arrive early.
10. The startup log line is written, and `/@health/ready` starts returning `200`.
11. On shutdown, after open requests are drained, **`applicationStopped()`** runs while the MongoDB clients are still open. See [Operating](operating.md#graceful-shutdown).

See [Bootstrap](bootstrap.md) for details on each hook.

## Hot compile

In development mode, recompiling (IDE auto-build or `mvn compile`) triggers a restart so you can refresh the browser without restarting the JVM by hand. This convenience comes with one sharp edge: controller methods that take request parameters rely on **parameter names being present in the compiled bytecode**, and Java does not keep them by default.

For example, a controller method like this:

```java
public Response show(long id) {
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
    public void applicationInitialized() {
    }

    @Override
    public void applicationStarted() {
        datastore.save(new Person("Richard M.", "Whittaker", 33));
        datastore.save(new Person("Kitty D.", "Glenn", 45));
        datastore.save(new Person("Raul E.", "Kuhn", 46));
    }

    @Override
    public void applicationStopped() {
    }
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

Read this as "start from `default`, then apply whatever `environments.dev` overrides." So in `dev` mode the effective HTTP port is `9090`, not `8080`, even though `8080` is what `default` says. The archetype generated random cookie secrets and keys for `dev` and `test`; `prod` reads them with `env{}`, see [Secrets](secrets.md).

**`templates/`** mirrors controller and method names: `ApplicationController/index.ftl` is what `Response.ok().render()` renders when it is called from `index()`.

**`translations/`** holds `messages.properties` and locale-specific variants for [Internationalization](internationalization.md).

## Next steps

Work through these topics in order, or jump straight to what you need:

| Topic | Guide | What you will learn |
|---|---|---|
| Settings and secrets | [Configuration](configuration.md), [Secrets](secrets.md) | Keys, modes, env vars, secret files, HTTPS |
| URLs and lifecycle | [Routing](routing.md), [Bootstrap](bootstrap.md) | Mapping paths, static files, SSE |
| Request handling | [Controllers](controllers.md) | Parameters, validation, `Response` |
| HTML | [Templating](templating.md) | Freemarker variables, CSRF tags, i18n |
| APIs | [Working with JSON](working-with-json.md) | Request/response JSON |
| Login and cookies | [Authentication](authentication.md), [Sessions](sessions.md) | Auth cookie, session data |
| Database | [Persistence](persistence.md) | Entities, queries, multiple connections |
| Quality | [Testing](testing.md) | `TestRequest`, `TestBrowser` |
| Production | [Operating](operating.md) | JAR, Docker, secrets in prod |

When you are ready to deploy, read [Operating](operating.md) for fat-JAR packaging, secrets in production, and Log4j configuration exclusions. ✅
