# Getting started

## Prerequisites

- **Maven** 3.9.10 or higher
- **Java** 25 or higher

```shell
java --version
mvn --version
```

## Create an application

```shell
mvn archetype:generate -DarchetypeGroupId=io.mangoo -DarchetypeArtifactId=mangooio-maven-archetype -DarchetypeVersion=10.11.0
```

Use the latest published version from Maven Central. You will be asked for groupId, artifactId, and application name.

```shell
cd your-artifact-id
mvn clean package
mvn mangooio:run
```

Startup logs look like:

```
HTTP connector listening @127.0.0.1:9090
mangoo I/O application started in 9051 ms in dev mode. Enjoy.
```

Open `http://localhost:9090`. Import the Maven project into your IDE.

The archetype pins `connector.http.port` to **9090** in `dev` and `test`. Production still needs an explicit connector.

## Hot compile

In development, changed sources are recompiled quickly. Controllers need method parameter names, so enable `-parameters`.

**Eclipse:** Settings → Compiler → store information about method parameters.

**IntelliJ IDEA:** Settings → Java Compiler → additional command-line parameters: `-parameters`.

!!! note
    Maven already passes `-parameters`. The IDE setting is only required when you run from the IDE.

## Project layout

After the archetype you have:

```
.
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   ├── app
    │   │   │   ├── Bootstrap.java
    │   │   │   └── Module.java
    │   │   ├── controllers
    │   │   │   └── ApplicationController.java
    │   │   └── models
    │   │       └── Person.java
    │   └── resources
    │       ├── config.yaml
    │       ├── files
    │       │   └── robots.txt
    │       ├── log4j2.xml
    │       ├── log4j2-test.xml
    │       ├── templates
    │       │   ├── ApplicationController
    │       │   │   ├── index.ftl
    │       │   │   └── persons.ftl
    │       │   └── layout.ftl
    │       └── translations
    │           ├── messages.properties
    │           ├── messages_de.properties
    │           └── messages_en.properties
    └── test
        └── java
            └── controllers
                └── ApplicationControllerTest.java
```

`config.yaml` enables the [vault](secrets.md) and uses `vault{}` for cookie keys. A `vault.p12` file is created on first start.

## Next steps

- [Configuration](configuration.md) and [Secrets](secrets.md)
- [Routing](routing.md) and [Controllers](controllers.md)
- [Templating](templating.md) and [Working with JSON](working-with-json.md)
- [Authentication](authentication.md)
- [Persistence](persistence.md)
- [Testing](testing.md)
- [Operating](operating.md)
