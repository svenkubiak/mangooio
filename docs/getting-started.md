# Getting Started

## Prerequisites

Before setting up a **mangoo I/O** application, ensure that the following dependencies are installed:

- **Maven** (version **3.9.0** or higher)
- **Java** (version **21** or higher)

### Verifying Java Installation

To check your installed Java version, run:

```shell
$ java --version
...
java 21.0.6 2025-01-21 LTS
Java(TM) SE Runtime Environment (build 21.0.6+8-LTS-188)
Java HotSpot(TM) 64-Bit Server VM (build 21.0.6+8-LTS-188, mixed mode, sharing)
```

### Verifying Maven Installation

To verify Maven, run:

```shell
$ mvn --version
...
Apache Maven 3.9.9
Java version: 21.0.6, vendor: Oracle Corporation, runtime:
Default locale: en_US, platform encoding: UTF-8
```

Once both dependencies are confirmed, you are ready to create your first **mangoo I/O** application.

---

# Creating Your First Application

**mangoo I/O** provides a **Maven archetype** to quickly generate a new project. Execute the following command:

```shell
mvn archetype:generate -DarchetypeGroupId=io.mangoo -DarchetypeArtifactId=mangooio-maven-archetype -DarchetypeVersion=$LATEST
```

You will be prompted to enter:

- **Group ID**
- **Artifact ID**
- **Application name** (mandatory)

### Compiling the Project

Navigate to your newly created project directory and compile it:

```shell
mvn clean package
```

### Running the Application in Development Mode

Start your application in **development mode** using:

```shell
mvn mangooio:run
```

Once dependencies are downloaded, you should see the **mangoo I/O** startup message confirming a successful launch:

```
10:55:26.399 [main] INFO  io.mangoo.core.Application - HTTP connector listening @127.0.0.1:9090
10:55:26.407 [main] INFO  io.mangoo.core.Application - mangoo I/O application started in 9051 ms in dev mode. Enjoy.
```

If you see the **mangoo I/O** logo, your application has started successfully. Otherwise, check the error message for troubleshooting.

### Accessing Your Application

Open your browser and visit:

```
http://localhost:9090
```

Your **mangoo I/O** application is now running. You can import the Maven project into your preferred IDE for further development.

---

# Enabling Hot-Compiling in Development Mode

**mangoo I/O** supports **hot-compiling** in development mode, meaning changes to source files are reflected instantly (typically within a second). However, Java requires specific compiler flags for this to work.

### Configuring Your IDE

#### **Eclipse**

Enable the following setting:

```
Settings -> Compiler -> Check "Store information about method parameter (usable via reflection)"
```

#### **IntelliJ IDEA**

Add the following compiler flag:

```
Settings -> Java Compiler -> Additional line parameters: "-parameters"
```

Without this setting, **mangoo I/O** will not correctly pass request parameters to controllers in development mode.

!!! note
    This setting is only required in your IDE. When building via Maven, the necessary flags are already configured in the **Maven compiler plugin**.

---

# Application Structure

A **mangoo I/O** application follows a structured layout. After creating a new project using the Maven archetype, your application structure will look like this:

```
.
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   ├── app
│   │   │   │   ├── Bootstrap.java
│   │   │   │   └── Module.java
│   │   │   └── controllers
│   │   │       └── ApplicationController.java
│   │   └── resources
│   │       ├── config.yaml
│   │       ├── files
│   │       │   ├── assets
│   │       │   │   ├── javascripts
│   │       │   │   │   └── javascript.min.js
│   │       │   │   └── stylesheets
│   │       │   │       └── stylesheet.min.css
│   │       │   └── robots.txt
│   │       ├── log4j2.xml
│   │       ├── templates
│   │       │   ├── ApplicationController
│   │       │   │   └── index.ftl
│   │       │   └── layout.ftl
│   │       └── translations
│   │           ├── messages.properties
│   │           ├── messages_de.properties
│   │           └── messages_en.properties
│   └── test
│       └── java
│           └── controllers
│               └── ApplicationControllerTest.java
```

---

# Next Steps

Your **mangoo I/O** application is now set up. Next, explore:

- Defining routes and controllers
- Working with templates and rendering views
- Handling JSON-based APIs
- Configuring authentication and security
- Integrating databases and persistence layers