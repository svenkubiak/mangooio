# Requirements

mangoo I/O requires Maven in at least version 3.9.0 and Java in at least version 21.

To verify that you have Java installed correctly, the version check should look something like this

```bash
$ java --version
...
java 21.0.6 2025-01-21 LTS
Java(TM) SE Runtime Environment (build 21.0.6+8-LTS-188)
Java HotSpot(TM) 64-Bit Server VM (build 21.0.6+8-LTS-188, mixed mode, sharing)
```

Accordingly, the Maven check should look like something this

```bash
$ mvn --version
...
Apache Maven 3.9.9 
Java version: 11.0.1, vendor: Oracle Corporation, runtime: 
Default locale: de_DE, platform encoding: UTF-8
```

If all requirements are fulfilled, you are read to go to create your first mangoo I/O application.

# Your first application

mangoo I/O comes with a ready-to-use Maven archetype, which will get your first application started in no time. To create your first application execute the following Maven command:

```bash
mvn archetype:generate -DarchetypeGroupId=io.mangoo -DarchetypeArtifactId=mangooio-maven-archetype -DarchetypeVersion=LATEST
```

You will be prompted for a group and artifact id. You also have to set an application name which is mandatory. 

Once the archetype generation is finished, change in your newly created project directory and execute the following command:

```bash
mvn clean package
```

Once the compilation is finished, you can start the development \(dev\) mode, by executing the following maven command

```bash
mvn mangooio:run
```

Once Maven has downloaded all required dependencies you should see the mangoo I/O logo showing you that your new application has started successfully in dev mode.

```bash
10:55:26.398 [main] INFO  io.mangoo.core.Application - 
                                                ___     __  ___  
 _ __ ___    __ _  _ __    __ _   ___    ___   |_ _|   / / / _ \ 
| '_ ` _ \  / _` || '_ \  / _` | / _ \  / _ \   | |   / / | | | |
| | | | | || (_| || | | || (_| || (_) || (_) |  | |  / /  | |_| |
|_| |_| |_| \__,_||_| |_| \__, | \___/  \___/  |___|/_/    \___/ 
                          |___/                                  


https://github.com/svenkubiak/mangooio | @mangoo_io

10:55:26.399 [main] INFO  io.mangoo.core.Application - HTTP connector listening @127.0.0.1:9090
10:55:26.407 [main] INFO  io.mangoo.core.Application - mangoo I/O application started in 9051 ms in dev mode. Enjoy.
```

Whenever you see the mangoo I/O logo your application has started successfully. Otherwise you will see an error, showing what went wrong when mangoo I/O tried to start.

Now open your default web browser an say hello to your first mangoo I/O application by opening the following URL:

```
http://localhost:9090
```

Now your are ready to import the Maven project in your IDE of choice.

# IMPORTANT: Using hot-compiling in dev mode

When in dev mode mangoo I/O supports hot-compiling. This means, that when you change a source file in your IDE of choice the changes are available more or less instantly \(in most cases in less than a second\). As mangoo I/O relies on Java 11, it is important that the files are compiled with the correct flags specific to Java 11. To be more precise, mangoo I/O relies on the parameter flag that enables lookup of method parameters within your IDE. Unfortunatlly, this setting is disabled by default in popular IDEs.

If you are using **Eclipse**, please make sure that you have checked the following option:

```
Settings -> Compiler -> Check "Store information about method parameter (usable via reflection)"
```

If you are using **IntelliJ**, please make sure that you have checked the following option:

```
Settings -> Java Compiler -> Add additional line parameters: -> "-parameters" (without the quotes)
```

If you don’t do this, mangoo I/O won’t pass request parameters to your controller and they will be passed as "null" when in dev mode and compiled and used within your IDE.

**This is only required in your IDE of choice, as the build via Maven sets the flags via the Maven compiler plugin.**

## Structure of an application

If you have created a new mangoo I/O application using the archtype, this is the basic file structure of your application

```
.
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   ├── app
│   │   │   │   ├── Bootstrap.java
│   │   │   │   └── Module.java
│   │   │   └── controllers
│   │   │       └── ApplicationController.java
│   │   └── resources
│   │       ├── config.props
│   │       ├── files
│   │       │   ├── assets
│   │       │   │   ├── javascripts
│   │       │   │   │   └── javascript.min.js
│   │       │   │   └── stylesheets
│   │       │   │       └── stylesheet.min.css
│   │       │   └── robots.txt
│   │       ├── log4j2-test.xml
│   │       ├── log4j2.xml
│   │       ├── templates
│   │       │   ├── ApplicationController
│   │       │   │   └── index.ftl
│   │       │   └── layout.ftl
│   │       └── translations
│   │           ├── messages.properties
│   │           ├── messages_de.properties
│   │           └── messages_en.properties
│   └── test
│       └── java
│           └── controllers
│               └── ApplicationControllerTest.java
```

mangoo I/O has the following convention-over-configuration:

By convention the application **must** have a package src/main/java/app with the following two classes

```bash
Bootstrap.java
Module.java
```

The Bootstrap class is used to configure the routes of our application and has convienient methods that can be used for hooking into different framework startup processes. The Module class is used for your custom Google Guice bindings as mangoo I/O ships with dependency injection via Google Guice.

It is recommended to have the controllers in a controller package. However, this is not required as the mapping is done in the Bootstrap class.

The application must have also a package src/main/resources where all your non java files are located. The following files and folders are mandator by convention-over-configuration

```
/files
/templates
/translations
config.props
log4j2.xml
```

The /files folder contains all static files \(e.g. robots.txt or Javascript/Stylesheet assets\). The /templates folder contains all templates of your application as mangoo I/O ships with Freemarker as defaul template engine.

By convention the /templates folder has a layout.ftl file which contains the basic layout of your application. If you have a controller that renders a template, each controller class must have by convention-over-configuration a \(case-sensitive\) corresponding folder inside the /templates folder, where the method name of each controller must equal the template name, ending with a .ftl suffix. If you are not rendering any template from your controller \(e.g. if you are just sending or recieving JSON\), than this is of course optional as no template is rendered.

The /translations folder contains all translation files of your application. Each file starts with “messages”, followed by a “\_” , the language shortcut and a .properties suffix. Even if you have no translations in your application, by convention, there has to be at least a messages.properties file in your /translations folder.
