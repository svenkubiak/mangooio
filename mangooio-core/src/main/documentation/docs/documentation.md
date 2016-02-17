# Overview

Development of mangoo I/O started in mid 2015 out of the interest on
how difficult it would be to create an intuitive, developer friendly,
full stack web framework in Java from scratch. After I had a fresh new breeze of
java development for the web with the [Play
Framwork - Version 1](https://www.playframework.com), contributions to the
[Ninja Framework](http://www.ninjaframework.org) and having seen a lot of
cumbersome "Enterprise" applications, I thought it was time for yet
another full stack java web framework.

For me, developing mangoo I/O will always be about having a developer friendly,
easy to understand web framework with a small learning curve for the Java ecosystem.

At its core, mangoo I/O is a classic MVC-Framework. The foundation of mangoo I/O is the high performant
[Undertow](http://undertow.io) web server from JBoss. On top of that,
standard, production ready java libraries are used - no reinventing of the
wheel, no bytecode manipulation, no magic whatsoever. The main reason for using Undertow was, that
it is based on non-blocking I/O in the form of
[XNIO](http://xnio.jboss.org). And although Undertow does support the
servlet API, one is not bound to use it in any way, giving a Java developer
the opportunity to work fully stateless.

mangoo I/O is inspired by the [Ninja
Web Framework](http://www.ninjaframework.org). Although the mangoo I/O core is a complete custom implementation, some ideas and methodologies were re-used.

Here are some key features of mangoo I/O in a nutshell:

* Intuitive convention-over-configuration
* Highly scalable using a share-nothing stateless architecture
* Hot-Compiling development mode for high productivity
* Easy to use template engine
* Support for Web Sockets
* Support for Server-Sent Events
* Simple and self-explaining form handling and validation
* Plain scheduling for recurring tasks
* Easy handling of JSON in- and output
* Build-in asset minification in development mode
* Flexible testing tools
* Build-in authentication tools
* Support for OAuth with Twitter, Google and Facebook
* Simple Deployment and CI-Integration
* i18N Internationalization

One main focus of mangoo I/O was to have a good and well documented code
base. Therefore, mangoo I/O is constantly checked against
http://www.sonarqube.org[SonarQube] with a rule set of more than 600
quality rules.

## Libraries

Here are some used libraries and their purpose in mangoo I/O.

* Maven - Dependency management, built-system, packaging
* Undertow - Web Server
* Google Guice - Dependency injection
* Log4j 2 - Logging
* Freemarker - Template engine
* Google Guava, Hazlecast - Caching
* Quartz Scheduler - Scheduling
* Boon JSON - JSON parser
* SnakeYaml - Configuration handling
* JUnit - Testing
* JBcrypt - Strong cryptography and authentication
* And many more ...

### Documentation versioning

This documentation always comes from the latest stable tag of the GitHub
repository. However, you can change the version of the documentation by
simple adding the version number to the URL. If you want, for example, the documentation of version
1.0.0 re-open this page with the following URL


    https://mangoo.io/documentation/1.0.0

# Getting started

## Preconditions

mangoo I/O requires
[Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
(at least 1.8) and [Maven](https://maven.apache.org) (at least 3.3.1).

### Checking Java

To make sure that you have Java correctly installed use the following
command


    java -version


This should output the Java version information as follows

    java version "1.8.0_45"
    Java(TM) SE Runtime Environment (build 1.8.0_45-b14)
    Java HotSpot(TM) 64-Bit Server VM (build 25.45-b02, mixed mode)


### Checking Maven

To make sure that you have Maven correctly installed use the following
command

    mvn -version


This should output the Maven version information as follows

    Apache Maven 3.3.3 (...)
    Java version: 1.8.0_45, vendor: Oracle Corporation
    Java home: /Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home/jre
    Default locale: de_DE, platform encoding: UTF-8
    ...

Now you are ready to create your first mangoo I/O application!

## Creating your first mangoo I/O application

mangoo I/O comes with a ready-to-use Maven archetype, to get your first
application started in no time. To create your first application execute
the following Maven command:

    mvn archetype:generate -DarchetypeGroupId=io.mangoo -DarchetypeArtifactId=mangooio-maven-archetype

You will be prompted for a group and artifact id. You also have to set an application name and an application secret.
Make your secret is "secure", as some functions, like sessions and authentication rely on it. Please note, that the
application secret has to be at least 16 characters.

Once the archetpye generation is finished, change in your newly created project directory and execute the following command:

    mvn clean package

Once the compilation is finished, you can start the development (dev) mode, by executing the following maven
command

    mvn mangooio:run

Once Maven has downloaded all required dependencies you should see the
mangoo I/O logo showing you that your new application has started
successfully in dev mode.

                                                    ___     __  ___
     _ __ ___    __ _  _ __    __ _   ___    ___   |_ _|   / / / _ \
    | '_ ` _ \  / _` || '_ \  / _` | / _ \  / _ \   | |   / / | | | |
    | | | | | || (_| || | | || (_| || (_) || (_) |  | |  / /  | |_| |
    |_| |_| |_| \__,_||_| |_| \__, | \___/  \___/  |___|/_/    \___/
                              |___/
    https://mangoo.io | @mangoo_io | 1.0.0

    mangoo I/O application started @127.0.0.1:8080 in 442 ms in dev mode. Enjoy.


Whenever you see the mangoo I/O logo your application has started successfully.

Now open your default web browser an say hello to your first mangoo I/O
application by opening the following URL

    http://localhost:8080


> ## Using hot-compiling in dev mode
> When in dev mode mangoo I/O supports hot-compiling. This means, that when you change a source file in your IDE
> of choice the changes are available more or less instantly (in most cases in less than a second).
> As mangoo I/O relies on Java 8, it is important that the files are compiled with the correct flags specific to Java 8.
> To be more precise, mangoo I/O relies on the parameter flag that enables easy lookup of method parameters.
>
> If you are using *Eclipse*, please make sure that you have checked the following option: +
> Settings -> Compiler -> Check "Store information about method parameter (usable via reflection)"
>
> If you are using *IntelliJ*, please make sure that you have checked the following option: +
> Settings -> Java Compiler -> Add additional line parameters: -> "-parameters" (without the qoutes)
>
> If you don't do this, mangoo I/O won't pass request parameters to your controller when in dev mode.
>
> This is only required in dev mode, as compilation in all other modes is done via Maven
> and the correct flags are set via the Maven compiler plugin.

## Basic structure of a mangoo I/O application

If you have created a new mangoo I/O application via the maven archetype,
this is the basic structure of a the application

	.
	├── pom.xml
	└── src
	    └── main
	        ├── java
	        │   ├── conf
	        │   │   ├── Lifecycle.java
	        │   │   ├── Module.java
	        │   └── controllers
	        │       └── ApplicationController.java
	        └── resources
	            ├── files
	            ├── application.yaml
	            ├── routes.yaml
	            ├── log4j2.xml
	            ├── templates
	            │   ├── ApplicationController
	            │   │   └── index.ftl
	            │   └── layout.ftl
	            └── translations
	                ├── messages.properties
	                ├── messages_de.properties
	                └── messages_en.properties


mangoo I/O comes with the following convetion-over-configuration:

By convention the application must have a package src/main/java/conf with the
following classes

	Lifecycle.java
	Module.java

The Lifecycle class is used for hooking into the application startup process.
The Module class is used for your custom Google Guice bindings and the Routes
class contains you mapped request to controllers and methods, assets, etc.

The application must have a package src/main/resources with the
following files and folders

	/files
	/templates
	/translations
	application.yaml
	log4j2.xml
	routes.yaml

The /files folder contains all static files (e.g. robots.txt or JS/CSS
assets) - see [handling static files and assets](#handling-static-files-and-assets) for more
information on serving static files and assets. The /templates folder contains all templates
of your application. By convention the /templates folder has a
layout.ftl file which contains the basic layout of your application.
Each controller class must have a (case-sensitive) corresponding
sub-folder inside the /templates folder, where the method name of each
controller must equal the template name, ending with a .ftl suffix. If you
are not rendering any template from your controller (e.g. if you are just sending JSON),
than this is of course optional.

The /translations folder contains all translation files of your
application. Each file starts with "messages", followed by a "_"  and the language
and a .properties suffix. Even if you have no translations in your
application, by convention there has to be at least a
messages.properties file in your /translations folder. Even if this file is empty.

It is recommended to have the controllers in a controller package, but not required
as the mapping is done in the Routes class and mangoo I/O doesn't require this.

If you don't provide a log4j2.xml file, then Log4j2
will fallback to a default configuration provided with the Log4j2 base package.

# Configuration

mangoo I/O relies on one configuration file for your hole application.
The application.yaml file is located in the src/main/resources folder,
along with all other files, that are not Java classes. You can add and
customize settings, simply by adding an appropriate value in the
application.yaml, for example

	application:
	      name   : myValue

The application.yaml uses [YAML](https://de.wikipedia.org/wiki/YAML) for setting
the configuration values.

There is a number of default properties which configure a mangoo I/O
application. See [Configuration options](#configuration-options),
for all configuration options and there default values.

Config values are accessed with a dot-notation in mangoo I/O. If you have
a config value like

	application:
	      minify:
	           js    : true
	           css   : true

this would be accessible by the following keys

	application.minify.js
	application.minify.css

To access configuration values you have three options for retrieving the Config class.
You can either inject the Config class via constructor or member variable. Or you can invoke
the static helper method in the Application class - which is the recommended way.

Injection via member variable

	@Inject
	private Config config;

Injection via constructor variable

	@Inject
	private MyClass(Config config) {
	    //do something
	}

Static access via helper method

	private static final Config CONFIG = Application.getConfig();

You can access a configuration value, either by a given key or predefined defaults
from mangoo I/O.

	config.getString("application.minify.js");
	config.getString(Key.APPLICATION_MINIFY_JS);

By default mangoo I/O will uses the given application.yaml from the resources folder, but
you can pass an absolute path to the executable JAR, like


	... -Dapplication.config=/path/to/config/application.yaml


This will tell mangoo I/O to use this file instead of the application.yaml in the resource folder.

## Modes

By convention, mangoo I/O offers three configuration modes: **dev**,
**test** and **prod**. The dev mode is automatically activated, when you
start your mangoo I/O application for development with Maven for local development.

	mvn mangooio:run

The test mode is automatically activated when executing unit test and using the
mangoo I/O test utilities. The prod mode is activated by default when no
other mode is given. You can overwrite this programatically, by setting a system property

	System.setProperty("application.mode", "dev");

or by passing a system property to the executable JAR

	... -Dapplication.mode=dev

## Mode configuration

You can create mode specific configuration by prefixing a configuration
value.

	default:
	    application:
	        host       : localhost
	        port       : 8080

	test:
	    application:
	        port       : 10808

	dev:
	    application:
	        port       : 2342


If no mode specific configuration is available, mangoo I/O will look up
the default configuration. If mangoo I/O can find a environment specific value
(e.g. dev.application.port) value it will overwrite the default value (e.g. default.application.port).

By convention all default values are for the prod mode and will be overwritten, when
a mode specific value is found. This keeps the configuration values to a minimum.

# Lifecycle

In some cases it is useful to hook into the startup process of a mangoo
I/O application (e.g. for starting a database connection). For this cases
mangoo I/O offers the Lifecycle class, which can be found in the /conf
package of your application. Here is an example of how the Lifecycle
class may look like.

	package conf;

	import com.google.inject.Singleton;

	import io.mangoo.interfaces.MangooLifecycle;

	@Singleton
	public class Lifecycle implements MangooLifecycle {

	    @Override
	    public void applicationInitialized() {
	        // Do nothing for now
	    }

	    @Override
	    public void applicationStarted() {
	        // Do nothing for now
	    }
	}


> The Lifecycle class doesn't have to be named
> "Lifecycle", but the class must implement the MangooLifecycle interface and
> you have to bind the implementation using Google Guice in your Module
> class. The module class is also located in the /conf package in your
> application. This class can also hold other custom Google Guice bindings.

	package conf;

	import io.mangoo.interfaces.MangooAuthenticator;
	import io.mangoo.interfaces.MangooRequestFilter;
	import io.mangoo.interfaces.MangooLifecycle;

	import com.google.inject.AbstractModule;
	import com.google.inject.Singleton;

	import filters.MyGlobalFilter;

	@Singleton
	public class Module extends AbstractModule {
	    @Override
	    protected void configure() {
	        bind(MangooLifecycle.class).to(Lifecycle.class);
	    }
	}

## Routes

One of the main pieces of a mangoo I/O application is the mapping of
requests URLs to controllers classes and their methods. Whether you are rendering a
template, sending JSON or just sending a HTTP OK, every request has to
be mapped. This mapping is done in the route.yaml file, which you'll
find in the /src/main/resources folder of your application. Here is an
example of how a routing might look like.

	- GET:    /    ->    ApplicationController.index

This example maps a GET request to "/" to the index
method in the ApplicationController class. Thus, when you open your
browser and open the "/" URL the index method in the
ApplicationController class will be called.

A route definition always starts with the request method, followed by the URL an "->" and the
controller class with its corresponding method.

You can use the following request methods to defined your mappings

	- GET:    ...
	- POST:   ...
	- PUT:    ...
	- HEAD:   ...
	- DELETE: ...

The underlying Undertow server handles all request by using non-blocking I/O. However, there might
be situations where you need a long running request. To allow blocking in a request, simply at the
@blocking annotation to your request mapping.

	- GET:    /    ->    ApplicationController.index    @blocking

Please note, that the @blocking annotation is only available for the above mentioned request methods.

## Serving static files

If you want to serve static files (e.g. assets) you can map those files from your routes.yaml
accordingly. You can map ether a specific file or a complete folder and all its sub-content.

	- FILE:    /robots.txt
	- PATH:    /assets/

The file or path mapping is bound to the /files folder which you'll find in the src/main/resoureces
folder in your application. The above mappings would server the files accordingly.

	/src/main/resources/files/robots.txt
	/src/main/resources/files/assets/

## Server-Sent Events and WebSockets

Mappings for Server-Sent Events and WebSockets are also defined in the routes.yaml. As the
Server-Sent Event is a uni-directional protocol, it does not have a controller it is mapped to.

	- SSE:    /serversentevent
	- WSS:    /websocket          -> WebSocketController

A WebSocket controller ships with pre-defined controller methods, thus the method mapping is absolet.

There migh be situation where your Server-Sent Events and/or WebSockets are only available for authenticated
users. If this is the case, you can simply add the @authentication annotation to your mappings.

	- SSE:    /serversentevent                             @authentication
	- WSS:    /websocket          -> WebSocketController   @authentication

This will require an authentication cookie in the request to the Server-Sent Event or WebSocket, whic is
based on the build-in authentication mechanism. If the request does not have such a cookie, the
Server-Sent Event or WebSocket connection will be rejected.

# Controllers

Every controller method, whether it renders a template, sends JSON or
just returns a HTTP Status, must return a Response object. This is handled by
using the Response class of mangoo I/O. Here is an example of how a
controller method may look like.

	public Response index() {
	    return Response.withOk();
	}

By convention mangoo I/O will lookup a template name index.ftl in the following way

	/src/main/resources/templates/CONTROLLER_NAME/index.ftl

With the previously mapped request, a request to "/" will render the
index.ftl template and send the template along with a HTTP Status OK to
the client.

## Response timer

Some times it can be useful to check how much time a request spends in the code, from
the time the request comes an and the response is send. mangoo I/O enables you a specific
header for this case, which is disabled by default. If you enable the following option
in your application.yaml

	application:
	    timer: true

an additional X-Response-Header will be added to every response.

	X-Response-Header: 2 ms

This works for all mapped controller routes, except resources, websockets, binary content and
server sent events.

# Request and query parameters

mangoo I/O makes it very easy to handle request or query parameter. Lets
imagine you have the following mapping in your Routes class.

	- GET:    /user/{id}    ->    ApplicationController.index

Note the {id} in the URL, that defines that this part of the URL is a
request parameter.

Now lets imagine you execute the following request

	/user/1?foo=bar

For this example we are also added a query parameter.

To access both the request and query parameter, you can
simply add the names of the parameters along with the data type to your
controller method

	public Response index(int id, String foo) {
	    //Do something useful with id and foo
	    return Response.withOk();
	}

The following method parameters are available in mangoo I/O controller methods by default and can
be used as a request or query parameter.

	String
	Integer/int
	Float/float
	Double/double
	Long/long
	LocalDate
	LocalDateTime

> Double and Float values are always passed with "." delimiter, either if you pass the query or request parameter with "," delimiter.

All parameters are parsed case-sensitive, which means, that if you have a method parameter "localDateTime" you have to map the
request-parameter accordingly, e.g. /foo/{localDateTime}.

> LocalDate is parsed as ISO_LOCAL_DATE "yyyy-MM-dd", and LocalDateTime is parsed as ISO_LOCAL_DATE_TIME "yyyy-MM-ddThh:mm:ss".

The following classes can also be used directly in controller methods, but can not be used as a request or query parameter

	Request
	Session
	Form
	Flash
	Authentication

# Request values

The request class is a special object which can be passed into a controller method. It enables you
access to header and URL values a long with additional information about the request. To gain
access to the request object, simply pass it to your controller method.

	public Response index(Request request) {
	    //Do something useful with the request
	    return Response.withOk();
	}

The request class is also useful when you have multiple query or request parameter which you don't want to
name in your controller method header. To access a query or request parameter simply call the getter for
the parameter.

	public Response index(Request request) {
	    String foo = request.getParameter("foo");
	    return Response.withOk();
	}

## Request validation

As an additional feature on the request object, you can validate incoming parameters. Just like
[Form handling](#form-handling) you can access a Validator class, which can perform specific checks on the request
parameter.

	public Response index(Request request) {
	  request.validation().email("foo");
	  request.validation().required("bar");

	  if (!request.validation().hasErrors()) {
	     //Handle request
	  } else {
	     //Do nothing
	  }
	  ...
	}

With this validation you can check an incoming request and return specific error messages, for e.g. as JSON.

	public Response index(Request request) {
	  request.validation().email("foo");
	  request.validation().required("bar");

	  if (!request.validation().hasErrors()) {
	     //Handle request
	  } else {
	     return Response.withBadRequest()
	        .andJSONBody(request.validation.getErrors());
	  }
	  ...
	}

The error messages for the request use the same key as the form handling. Check
the documentation on [Form handling](#form-handling) for more information on how to customize
the specific error messages.

# Request handling

The heart of mangoo I/O (and probably of all web frameworks) is the handling of requests.
As mangoo I/O is based on Undertow for serving request, this is done by so called handlers.
mangoo I/O has a number of handlers which all perform a specific task when a request is served.
The handlers are chained to each other from the first receive of a request until sending out the response.

A DispatcherHandler is created at framework startup for each mapped controller from the routes.yaml
file, waiting to receive a request. From the DispatcherHandler the request chain is as follows:

	LocalHandler
	InboundCookiesHandler
	FormHandler
	RequestHandler
	OutboundCookiesHandler
	ResponseHandler

By using Google Guice features you have the option to customized each handler and change the request chain
for your own needs.

To overwrite a handler, first bind the handler to you custom handler in your Modules class.

	bind(LocaleHandler.class).to(MyLocaleHandler.class);

In your custom handler you need to extend the handler class and overwrite the methods from the default
handlers as you want.

	public class MyLocaleHandler extends LocaleHandler {

	    @Override
	    public void handleRequest(HttpServerExchange exchange) throws Exception {
			//do something different
	    }

	    @Override
	    protected void nextHandler(HttpServerExchange exchange) throws Exception {
	        //call another handler than the default one
	    }
	}

# Form handling

To access a form submitted to a controller class, you can simply pass
the mangoo I/O Form class. Here is an example of how this might look
like

	public Response index(Form form) {
	    ...
	}

The Form class offers you convenient methods for accessing form values from you template.

	public Response index(Form form) {
	    File file = form.getFile();
	    List<File> = form.getFiles();
	    String firstname = form.get("firstname");
	    ...
	}

> The Form class is only available if the request is mapped as a POST or PUT method.

The Form class is automatically available in the template so you don't
have to pass the class to your template.

## Form validation

Lets image you have the following form in a template

	<form method="/save" method="post">
	    <input type="text" name="firstname" />
	    <input type="text" name="lastname" />
	    <input type="text" name="email" />
	</form>

No lets imagine that you want to validate, that the firstname and
lastname from the request is not empty. mangoo I/O offers some convenient
functions to validate the submitted form values.

	public Response form(Form form) {
	    form.validation().email("email");
	    form.validation().required("firstname");
	    form.validation().required("lastname");

	    if (!form.validation().hasErrors()) {
	        //Handle form
	    } else {
	        //Do nothing
	    }

	    ...
	}

With the form class you can check if a field exists, check an eMail
address, etc. The hasErrors() method shows you if the form is valid and
can be handled or not.

mangoo I/O supports the following validations out of the box

* Required
* Minimum
* Maximum
* Match (case-insensitive)
* Exact match (case-sensitive)
* E-Mail
* IPv4
* IPv6
* Range
* Regular expression
* Numeric

## Showing error messages in a template

To show an error in a template, simply check for an error on a spcific field

	<#if form.hasError("myField")> ... </#if>

This is useful if you want to change the CSS style or display an error
message when the submitted form is invalid.

To display a form specific error you can use the error method on a form field

	${form.getError("myField")}

This will display e.g.

	Firstname can not be blank

The error messages are defined in your messages.properties file (or for
each language). There are some default error messages, but they can be
overwritten with custom error messages. If you overwrite a
validation message you have to use the appropriate prefix

	validation.required={0} is required
	validation.min={0} must be at least {1} characters
	validation.max={0} can be max {1} characters
	validation.exactMatch={0} must exactly match {1}
	validation.match={0} must match {1}
	validation.email={0} must be a valid eMail address
	validation.ipv4={0} must be a valid IPv4 address
	validation.ipv6={0} must be a valid IPv6 address
	validation.range={0} must be between {1} and {2} characters
	validation.url={0} must be a valid URL
	validation.regex={0} is invalid
	validation.numeric={0} must be an numeric value

The prefix follows the field type (email, required, match, etc.) for the
message.

## CSRF Protection

mangoo I/O allows you to retrieve an authenticity token for protection
against [CSRF](https://de.wikipedia.org/wiki/Cross-Site-Request-Forgery).
You can either obtain a prefilled hidden input field or the token itself.

To get the prefilled hidden input field, use the following tag in your
template

	<@authenticityForm/>

To get the token, use the following tag in your template

	<@authenticityToken/>

If you use either the form or the token you might want to check the
token in your controller. mangoo I/O offers a filter for checking the
correctness of the token. Just add the following filter to your
controller class or method.

	FilterWith(AuthenticityFilter.class)

If the token is invalid the request will be redirected to a default
403 Forbidden page.

# Sessions

With a [Shared
nothing architecture](http://en.wikipedia.org/wiki/Shared_nothing_architecture) in mind mangoo I/O uses a so called client-side
session. This means, that all information for a specific user is stored
on the client-side inside a cookie. The big advantage of this concept
is, that you can scale your application very easy, because nothing
connects a specific user to a specific mangoo I/O instance. The
downside of this architecture is, that you can only stored limited data
in the cookie (around 4k of data).

To make use of the mangoo I/O session, you can just pass the Session
class into your controller method.

	package controllers;

	import io.mangoo.routing.Response;
	import io.mangoo.routing.bindings.Session;

	public class SessionController {
	    public Response session(Session session) {
	        session.add("foo", "this is a session value");
	        return Response.withOk().andEmptyBody();
	    }
	}

The Session class offers you some convenient methods for adding, deleting
or completly erasing session data.

By default the session cookie has a lifespan of one day (86400 seconds). This, a long
with the name of the cookie, can be configure using the following
properties in the application.yaml

	cookie:
	  expires  : 86400

## Session data in templates

To access the Session values, simply call the appropriate key in your
template.

	${session.foo}

The Session class is automatically available in the template so you
don't have to pass the class to the template via a controller.

## Session encryption

By default the values in the client-side cookie are signed with the
application secret using SHA2 (SHA-512), making manipulation of the
values very difficult. The security of the client-side cookie can be further
increased by using AES encryption. To activate cookie encryption of the
session cookie, set the following property in your application.yaml

	cookie:
	  encryption  : true

The encryption strength is based on the length of your
application.secret configured in your application.yaml. If your
application.secret is more or equal than 32 characters, AES-256 will be
used. If you secret is more or equal than 24 characters, AES-192 will be
used. If your secret is more or equal than 16 characters AES-128 will
be used. The mangoo I/O framework will automatically determine and use
the strongest key possible.

## Session cookie versioning

As mentioned, the session data is stored at the client side in a cookie.

There may be situations where you are required to invalidate this cookie at the client side. For
this situations, mangoo I/O offers you the ability of cookie versioning. Basically, cookie
versioning is based on an additional number which is stored in the cookie. Once this number
changes, the signing of the cookie fails and the user is required to create a new cookie
by creating a new session. By default, this version number starts with 0. You can increase or change
this number by setting the appropriate property in the application.yml.

	application:
	      cookie:
	          version : 1

Once you set this new version, the session cookie on the client side will be invalidated and
the user gets a new session cookie with the current version.

WARN:
Be very careful with this feature, as it directly effects the experience of your users.

# Flash

Specially when working with forms it is useful to pass certain
informations (e.g. error- or success messages) to the next request. To
do this in a stateless environment, mangoo I/O uses the Flash class. This
is basically the same mechanism as a session, but all informations
are stored in a special flash cookie which is disposed once the request is finished.

	package controllers;

	import io.mangoo.routing.Response;
	import io.mangoo.routing.bindings.Flash;

	public class FlashController {
	    public Response flash(Flash flash) {
	        flash.success("this is a success");
	        flash.warning("this is a warning");
	        flash.error("this is an error");
	        flash.add("foo", "bar");

	       return Response.withRedirect("/");
	    }
	}

The Flash class has three convenient methods for the commonly used
scenarios: success, warning and error. This methods will automatically
create a key "success", "warning" or "error" in the flash class. Besides
that, you can pass custom values to the flash class.

## Flash in templates

To access the flash values, simply call the appropriate key in your
template.

	${flash.success}
	${flash.warning}
	${flash.error}
	${flash.foo}

The Flash class is automatically available in the template so you don't
have to pass the class to the template via a controller.

# Authentication

mangoo I/O comes with two authentication implementations out of the box: HTTP Basic
authentication and custom authentication where you have a custom login
and authentication process ready to use.

## Basic authentication

The HTTP Basic authentication in mangoo I/O uses a predefined filter:
BasicAuthenticationFilter.class. So the first step to enable Basic
authentication would be to have a annotated controller or method.

	package controllers;

	import io.mangoo.annotations.FilterWith;
	import io.mangoo.authentication.Authentication;
	import io.mangoo.filters.AuthenticationFilter;
	import io.mangoo.routing.Response;

	public class AuthenticationController {

	    @FilterWith(AuthenticationFilter.class)
	    public Response secret() {
	        return Response.withOk();
	    }
	}

To validate credentials passed from the client you need some place to do
this. Therefore you have to bind the authentication process via the Google
Guice configuration in your Module class.

	package conf;

	import io.mangoo.interfaces.MangooAuthenticator;

	import com.google.inject.AbstractModule;
	import com.google.inject.Singleton;

	@Singleton
	public class Module extends AbstractModule {
	    @Override
	    protected void configure() {
	        bind(MangooAuthenticator.class).toInstance(
	                (username, password) -> ("foo").equals(username) && ("bar").equals(password)
	        );
	    }
	}

In this example a Java 8 lambda expression is used to validate the
passed credentials. Of course, an instance of the MangooAuthenticator
can be passed in any other way. Just make sure you implement the
MangooAuthenticator interface and bind your implementation via the
Module class.

## Custom authentication

mangoo I/O supports you when a custom registration with a custom login
process is required. Although mangoo I/O does not store any credentials
or user data for you, it gives you some handy functions to make
handling of authentication as easy as possible.

mangoo I/O offers the Authentication class which can be simply injected
into a controller class.

	@Inject
	private Authentication authentication;

The authentication uses [BCrypt](http://de.wikipedia.org/wiki/Bcrypt)
provided by [jBCrypt](http://www.mindrot.org/projects/jBCrypt) for
password hashing. This means, that you don't have to store a salt along
with the user data, just the hashed password. This also means, that you have
to hash the user password with the provided function in the authentication class
and store this hash value along with your user data. This hashed value can be created with
the following method

	getHashedPassword(String password)

After you create the hash of the cleartext password of your user, you
have to store it with your user data. mangoo I/O does not do that for you.

The Authentication class offers convenient functions to perform
authentication. The main methods are

	getAuthenticatedUser()
	authenticate(String password, String hash)
	login(String username, boolean remember)
	login(String username)
	logout()

To perform a check, if a user is authenticated mangoo I/O offers a
predefined filter ready to use on controller classes or methods.

	@FilterWith(AuthenticationFilter.class)

Check the JavaDoc of the Authentication class to get more information on
how the methods work. Also check the custom configuration options for
the Authentication class in link:#c-configuration-options[C.
Configuration options]. All options with the prefix "auth" configure
custom Authentication.

## OAuth

mangoo I/O supports authentication with OAuth for [Twitter](https://twitter.com), [Google](https://google.com) and [Facebook](https://facebook.com)
in a fluent way. You may know this feature as "Sign in with ...". The OAuth feature
integrates smoothly in the previously mentioned Authentication class.

## Preconditions

In order to start an implementation for OAuth with mangoo I/O you first need an
application key and an application secret. Check the developer pages for [Twitter](https://apps.twitter.com), [Google](https://console.developers.google.com) and [Facebook](https://developers.facebook.com)
on how to create an app and get the required informations. Once you have the key and secret
simply add it to you application.yml

	default:
	  application:
	    ...

	  oauth:
	    twitter:
	        key       : ###
	        secret    : ###
	        callback  : http://localhost:8080/authenticate?oauth=twitter
	    google:
	        key       : ###
	        secret    : ###
	        callback  : http://localhost:8080/authenticate?oauth=google
	    facebook:
	        key       : ###
	        secret    : ###
	        callback  : http://localhost:8080/authenticate?oauth=facebook

There is a third required configuration, which you have to defined yousrelf. This is the callback property
which defines the URL where you do the actual authentication and  the login of your user.

[INFO]
As OAuth authentication integrates into mangoo I/O authentication mechanism, you can combine "normal" authentication and OAuth authentication.

### Setting the filters

When working with OAuth authentication in mangoo I/O you have to user two filters: OAuthLoginFilter and OAuthCallbackFilter. We'll start with the OAuthLoginFilter.

Let's imagine that you have a login page where the user can click on a link to open a registration page and login with his account or via OAuth to a supported
mangoo I/O OAuth provider. Let's assume, that this page is available via /login and maps to a method in a controller controller.

	...

	@FilterWith(OAuthLoginFilter.class)
	public Response login() {
	  return Response.withOk();
	}

As you can see, the method is annotated with the OAuthLoginFilter and does nothing fancy - just rendering a template. To enable your
user to start an OAuth authentication, create a link to the same login with a query parameter set to the OAuth provider name.

	<a href="/login?oauth=twitter">Authenticate via Twitter</a>
	<a href="/login?oauth=google">Authenticate via Google</a>
	<a href="/login?oauth=facebook">Authenticate via Facebook</a>

If the user clicks on one of the links, the filter intercepts the link and starts a simple redirect to the OAuth provider to start
the OAuth authentication.

After the user has done the authentication at the OAuth provider, he has to return to your page somehow. This is where the callback
property comes in place. The callback URL is passed, when the user is previously redirected so the OAuth service does now where to
send the user, once OAuth authentication is complete.

Let's assume, that the /authentication URL maps to a method in a controller.

	...

	@FilterWith(OAuthCallbackFilter.class)
	public Response authenticate(Authentication authentication) {
	  if (authentication.hasAuthenticatedUser()) {
	    OAuthUser oAuthUser = authentication.getOAuthUser();
	    if (oAuthUser != null) {
	      String response = oAuthUser.getOAuthReponse();
	      String id = oAuthUser.getId();
	      String username = oAuthUser.getUsername();
	      String picture = oAuthUser.getPicutre();
	    }

	    ...

	    authentication.login(username, false);

	    return Response.withRedirect("/");
	  }

	  return Response.withRedirect("login");
	}

As you can see the authentication method calls the same method (hasAuthenticatedUser) as the normal authentication process. If your
user authenticated successfully via OAuth you also have an additional object called OAuthUser in the authentication class. If
the authentication via OAuth fails or the user did not login via OAuth the OAuthUser object is null.

The OAuthUser is prefilled with some generic information that all supported mangoo I/O OAuth provider support. Please note,
that email is not one of this property. Thus, you have to ask the user for the email address if you require this information
management by storing the OAuth id of the user along with the OAuth provider (@twitter, @goolge or @facebook). But how you handle this
is completely up to you.

You can also access the original OAuthResponse by calling the appropriate getter which gives you a string representation of the
JSON reponse from the OAuth provider.

## Authentication cookie versioning

As mentioned, the authentication data is stored at the client side in a cookie.

There may be situations where you are required to invalidate this cookie at the client side. For
this situations, mangoo I/O offers you the ability of cookie versioning. Basically, cookie
versioning is based on an additional number which is stored in the cookie. Once this number
changes, the signing of the cookie fails and the user is required to create a new cookie
by creating a new authentication. By default, this version number starts with 0. You can increase or change
this number by setting the appropriate property in the application.yml.

	auth:
	  cookie:
	      version : 1

Once you set this new version, the authentication cookie on the client side will be invalidated and
the user gets a new authentication cookie with the new version once he signs in again.

WARN:
Be very careful with this feature, as it directly effects the user experience of your users.

# ETag for dynamic content

"ETag" or "entity tag" enables web application to make use of cached resources by allowing conditional requests from the client.
This is widely used for static resources like CSS or JS files.

mangoo I/O offers this functionality for dynamic content as well. To make use of an entity tag, simply add the
following method at the returning response in your controller method.

> If you are using a front-end HTTP server, please check the documentation on how it
> handles ETag, as the popular nginx web server [deliberately strips ETags](https://thinkingandcomputing.com/2014/09/27/enable-etag-nginx-resources-sent-gzip) once gzip is applied.

	public Response index() {
	    return Response.withOk().andETag();
	}

For more information on how ETag works, click [here](https://en.wikipedia.org/wiki/HTTP_ETag).

# Administrative URLs

mangoo I/O offers administrative URLs, which enables you to check certain application information via a web interface. The
following administrative URLs are available

	/@routes
	/@config
	/@cache
	/@health
	/@scheduler
	/@system
	/@memory

> By default, the administrative URLs are disabled in all modes. You can enable each administrative URL by
> setting the appropriate configuration value in your application.yaml. See [Configuration options](#configuration-options) for more information on this.

## /@routes

Displays a list of all configured routes and their corresponding controllers and methods.

## /@config

Displays a list of all configured properties and their corresponding values.

WARN:
Please be very careful with @config, as it exposes your configuration to the web.
For security reasons the properties "application.secret" is not available via the @config route.

## /@cache

Displays a list of cache statistics including cache hits, cache misses, etc.

## /@health

Displays a simple health check by returning "alive".

## /@scheduler

Displays a list of schedules jobs, their last and their next execution time.

## /@system

Displays a list of all properties for the current JVM.

## /@memory

Displays the current JVM memory usage.

## Authentication for administrative URLs

By default no authentication is enabled for accessing the administrative URLs. This can be
enabled by setting a username and a password in the application.yaml. This will tell the
administrative URLs controller to check for a Basic HTTP authentication.

	application:
		admin:
	    	username : admin
	        password : c7ad44cbad762a5da0a452f9e854fdc1e0e7a52a38015f23f3eab1d80b931dd472634dfac71cd34ebc35d16ab7fb8a90c81f975113d6c7538dc69dd8de9077ec

The password parameter expects a SHA512 hashed value.

# Working with JSON

mangoo I/O uses [boon JSON](https://github.com/boonproject/boon) for parsing JSON. boon is a
[very
fast](http://rick-hightower.blogspot.de/2014/01/boon-json-in-five-minutes-faster-json.htm) JSON handler with its main focus on serializing and deserializing of objects.

## JSON output

Consider for example the following POJO.

	package models;

	public class Person {
	    private String firstname;
	    private String lastname;
	    private int age;

	    public Person(String firstname, String lastname, int age) {
	        this.firstname = firstname;
	        this.lastname = lastname;
	        this.age = age;
	    }

	    public String getFirstname() {
	        return firstname;
	    }

	   public String getLastname() {
	       return lastname;
	    }

	    public int getAge() {
	        return age;
	    }
	}

To create a new person object and send it as a response you can simply can do this in
a controller

	package controllers;

	import io.mangoo.routing.Response;
	import models.Person;

	public class JsonController {
	    public Response render() {
	        Person person = new Person("Peter", "Parker", 24);
	        return Response.withOk().andJsonBody(person);
	    }
	}

The output of the response will look as follows

	{
	    "firstname" : "Peter",
	    "lastname" : "Parker",
	    "age" : 24
	}

## JSON input

To retrieve JSON which is send to your mangoo I/O application you have three options:
automatic object serialization, generic object convertion or working with the raw JSON
string.

## Custom serializer

By default JSON Boon will not write out nulls, empty lists or values that are default
values. If you want a value to be written out even if it is empty, null, false or 0, you
can use the @JsonInclude annotation. If you want a value to be excluded from JSON generation
you can use the @JsonIgnore annotation.

	public class Car {
	    @JsonInclude
	    public String brand = null;

	    @JsonInclude
	    public int doors = 0;

	    @JsonIgnore
	    public String comment = "blablabla";

	    public String foo = "blablabla";

	    public Car() {}
	}


You can customize the JSON serialization by overwriting the JsonSerializer in the JSONUtils class
which is recommended to use, when working with JSON in mangoo I/O.

	JsonSerializerFactory jsonSerializerFactory = new JsonSerializerFactory();
    jsonSerializerFactory.useAnnotations();
    jsonSerializerFactory.useFieldsOnly();
    ...
    JsonSerializer serializer = jsonSerializerFactory.create();

    JsonUtils.withJsonSerializer(serializer);

It is recommended that you customize the serializer when the framework starts using the
lifecycle methods.

### Automatic object convertion

Consider the person class from above and the following JSON send to
mangoo I/O

	{
	    "firstname" : "Petyr",
	    "lastname" : "Baelish",
	    "age" : 42
	}

To handle this JSON with automatic object convertion you can simply do this
in a controller.

	package controllers;

	import io.mangoo.routing.Response;
	import models.Person;

	public class JsonController {
	    public Response parse(Person person) {
	        // TODO Do something with person object
	        ...
	    }
	}

You just pass the object you want to convert from the JSON request and
mangoo I/O automatically makes the serialization to your POJO, making it available
in your controller.

###
If you don't have a POJO and you want to retrieve the JSON content,
mangoo I/O offers you a generic way of retrieving the content through
the object body of a request to a Map<String, Object>.

	package controllers;

	import io.mangoo.routing.Response;
	import io.mangoo.routing.bindings.Request;

	public class MyController {
	    public Response parse(Request request) {
	        Map<String, Object> myjson = request.getBodyAsJsonMap();
	        String foo = json.get("firstname");
	    }
	}

You can also get hold of the JSON using the great [JsonPath](https://github.com/jayway/JsonPath) library.

	package controllers;

	import io.mangoo.routing.Response;
	import io.mangoo.routing.bindings.Request;
	import com.jayway.jsonpath.ReadContext;

	public class MyController {
	    public Response parse(Request request) {
	        ReadContext readContext = request.getBodyAsJsonPath();
	        String foo = readContext.read("$.firstname");
	    }
	}

### Handle raw JSON string

If you don't want mangoo I/O to automatically convert a JSON input you
can also work with the raw JSON string. The body object contains the raw
values of a request. Here is an example

	package controllers;

	import io.mangoo.routing.Response;
	import io.mangoo.routing.bindings.Request;

	public class MyController {
	    public Response parse(Request request) {
	        String body = request.getBody();
	        ...
	    }
	}

# Filters

Filters are a way of executing code before each controller or each
method is executed. To execute a filter before a controller or method,
you can use the @FilterWith annotation.

	@FilterWith(MyFilter.class)

There are two types of filters in mangoo I/O: Controller/Method filters
and a global filter.

## Controller or method filter

As mentioned, a filter can be added to a controller class or method. If
added to a controller class the filter will be exectued on every method
in the class. If added to a method, the filter will only be executed on
that method.

	package controllers;

	import io.mangoo.annotations.FilterWith;
	import io.mangoo.filters.AuthenticityFilter;
	import io.mangoo.routing.Response;

	@FilterWith(MyFilter.class)
	public class MyController {

	    public Response token() {
	        return Response.withOk().andContent("foo", "bar");
	    }

	    @FilterWith(AuthenticityFilter.class)
	    public Response valid() {
	        return Response.withOk().andContent("foo", "bar");
	    }
	}

On the above example, the Filter MyFilter will be executed when the
token() and the valid() method is called. The Filter AuthenticityFilter will
also be called, when the valid() method is called.

You can assign multiple filters to a controller or a method.

	@FilterWith({"MyFirstFilter.class, MySecondFilter.class"})

They are executed in order.

### Creating a filter

A controller or method filter must implement the MangooFilter
interface.

	package mangoo.io.filters;

	import io.mangoo.interfaces.MangooControllerFilter;
	import io.mangoo.routing.bindings.Exchange;

	public class MyFilter implements MangooFilter {

	    @Override
	    public Response filter(Request request, Response response) {
	        //Do nothing for now
	        return response;
	    }
	}

The main method of a filter is the execute method, which receives the
request and response class from mangoo I/O. This classes give you a handy way
of manipulating the response as it is passed to other filters and
merged with the response of your controller, if you don't end the request at some
point in the filter.

All returned response object from your filter are passed to the next filter in the following order:

1. Global filter
2. Controller filters
3. Method filters

> Only the header and content values are merged with the response object returned from your controller.

Here is an example of the AuthenticityFilter which is used for the CSRF checks.

	public class AuthenticityFilter implements MangooControllerFilter {

	    @Override
	    public Response execute(Request request, Response response) {
	        if (!request.authenticityMatches()) {
	            return Response.withForbidden().andBody(Template.DEFAULT.forbidden()).end();
	        }

	        return response;
	    }
	}

As you can see in the example, you can change the status code, a long
with the content of the response inside a filter. The end() method tells mangoo I/O that i should
end the response at this point and should not execute further filters or controllers.

Please note, that you always have to return the response object. Return null will result
most certainly in an exception.

## Global filter

Besides the controller class or method filter, there is a special filter
which can be executed globally. This means, that this filter is called on
every mapped request in the Routes class for controller classes and methods.
This is useful if, for example, you have to
force the language for your application or if you have an application
that does not have any public content and requires authentication for
every request.

A global filter works similar to a controller or method filter, but the
filter has to implement the MangooRequestFilter interface instead.

	package filters;

	import io.mangoo.interfaces.MangooRequestFilter;
	import io.mangoo.routing.bindings.Exchange;

	public class MyGlobalFilter implements MangooRequestFilter {

	    @Override
	    public execute execute(Request request, Response response) {
	        Locale.setDefault(Locale.ENGLISH);
	        return response;
	    }
	}

> There can only be one global filter in your mangoo I/O application.

# Logging

mangoo I/O uses [Log4j2](https://logging.apache.org/log4j/2.x) for logging.
If you are familiar with Log4j2, creating a new logger instance is trivial.

	import org.apache.logging.log4j.Logger;
	import org.apache.logging.log4j.LogManager;

	private static final Logger LOG = LogManager.getLogger(MyClass.class);

You can configure your appenders in the log4j2.xml file located in
src/main/resources which is present by default if you created you project from the
mangoo I/O archtype.

You can always use the default [configuration options](https://logging.apache.org/log4j/2.x/manual/configuration.html)
from log4j2 for loading the configuration file. However, mangoo I/O offers you an additional feature, specially when
working with different environments. When mangoo I/O starts it will look for an environment specific log4j2 configuration
file in the form of

	log4j2.dev.xml

This can of course be set for all modes.

If mangoo I/O does not find such a file, it will fall back to the default [configuration options](https://logging.apache.org/log4j/2.x/manual)configuration.html
from log4j2.

# Caching

mangoo I/O uses [Guava Cache](https://github.com/google/guava/wiki) CachesExplained as default Cache for storing and accessing values
in-memory. To use the cache in your application, simply inject the cache class.

	@Inject
	private Cache cache;

The cache offers some convenient functions for adding and removing values
from the cache.

To use the cache, simply add or remove an entry by a specific key.

	String foo = cache.get("myvalue");

One cool thing about the guava cache, is the option to pass a callable if the value is
not found in the cache.

	// If the key wasn't in the "easy to compute" group, we need to
	// do things the hard way.
	  cache.get("myvalue", new Callable<Value>() {
	    @Override
	    public Value call() throws AnyException {
	      return doThingsTheHardWay(key);
	    }
	  });

## Cache eviction

The Guava eviction of cached data comes in two flavours: eviction after (last) access or eviction after write.
While eviction after write defines a fixed period of time until the entry is removed from the cache, eviction after access
defines a time span after the last access of the entry until it is removed from the cache, keeping high frequent entries in
the cache as long as possible. Thus, you have to manually force and updated if your entry is access often.

Mangoo I/O uses eviction after access as default with a default timespan of 3600 seconds. You can configure both the eviction
method, as well as the timespan in you application.yaml.

	cache:
	    eviction : afterWrite
	    expires  : 3600    

# Scheduling

mangoo I/O uses the [Quartz Scheduler
Framework](http://quartz-scheduler.org) for creating and executing periodic tasks. The integration comes in two
flavors: automatic scheduler start and manual scheduler start. Where the automatic
scheduling is the default way of using the quartz scheduler.

##= Automatic scheduler start

To create a new task, create a simple Pojo that implements the Job interface from
the Quartz package.

	package jobs;

	import org.quartz.Job;
	import org.quartz.JobExecutionContext;
	import org.quartz.JobExecutionException;

	import com.google.inject.Singleton;

	@Singleton
	@Schedule(cron = "0 0 3 * * ?", description = "This is a job description")
	public class MyJob implements Job {

	    @Override
	    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
	        //Do nothing for now
	    }
	}

To schedule the job, add the @Schedule annotation, which requires a cron expression for the execution and an
optional description of the job.

> Scheduling a job only works with cron expression.

By default, mangoo I/O looks up all jobs in a package called "jobs", but this can be
configured by setting the following property in the application.yaml

	scheduler:
		package   : project.my.package

Once mangoo I/O starts, it will automatically pick up the @Schedule annotated classes, adds them
to the scheduler and starts the scheduler.

## Manual scheduler start

Again, start with creating a new task, by creating a simple Pojo, except *without* the @Schedule
annotation

	package jobs;

	import org.quartz.Job;
	import org.quartz.JobExecutionContext;
	import org.quartz.JobExecutionException;

	import com.google.inject.Singleton;

	@Singleton
	public class MyJob implements Job {

	    @Override
	    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
	        //Do nothing for now
	    }
	}

As this job is not scheduled or executed at all right now, you have to
tell the scheduler when to execute the task and to start the scheduler
itself. It is recommended to use the Lifecycle for scheduling tasks and
starting the scheduler.

	package conf;

	import org.quartz.JobDetail;
	import org.quartz.Trigger;

	import jobs.InfoJob;
	import io.mangoo.interfaces.MangooLifecycle;
	import io.mangoo.scheduler.Scheduler;
	import io.mangoo.utils.SchedulerUtils;

	import com.google.inject.Inject;
	import com.google.inject.Singleton;

	@Singleton
	public class Lifecycle implements MangooLifecycle {

	    @Inject
	    private Scheduler scheduler;

	    @Override
	    public void applicationStarted() {
	        JobDetail jobDetail = SchedulerUtils.getJobDetail("MyJobDetail", "MyJobGroup", MyJob.class);
	        Trigger trigger = SchedulerUtils.getTrigger("MyJobTrigger", "MyTriggerGroup", "MyTriggerDescription", "15 15 15 15 * ?");

	        scheduler.schedule(jobDetail, trigger);
	        scheduler.start();
	    }
	}

To schedule the previously defined class, you have to create a JobDetail
and a Trigger which you pass to the scheduler. Once that is done, you
can start the scheduler by simply calling the start method.

## Custom Quartz Scheduler configuration

If you require a custom configuration for quartz inside mangoo I/O you
can use the application.yaml to pass any option to quartz. Simply add the configuration option with the appropriate
prefix org.quartz.

	org:
	  quartz:
	    scheduler.instanceName=Foo
	    scheduler.instanceId=Bar

Check out the
[Quartz
Scheudler configuration documentation](http://quartz-scheduler.org/generated/2.2.2/html/qs-all/#page/Quartz_Scheduler_Documentation_Set%2F_qs_all.1.041.html%23) for more information.

# WebSockets

General information on using WebScokets can be found
[here](http://en.wikipedia.org/wiki/WebSocket). To use WebSockets in
mangoo I/O you have to extend the MangooWebSocket class in your WebSocket controller. Extending this
class offers you the entry points for using WebSockets methods.

	package controllers;

	import io.undertow.websockets.core.BufferedBinaryMessage;
	import io.undertow.websockets.core.BufferedTextMessage;
	import io.undertow.websockets.core.CloseMessage;
	import io.undertow.websockets.core.WebSocketChannel;
	import io.mangoo.interfaces.MangooWebSocket;

	public class WebSocketController extends MangooWebSocket {
	    @Override
	    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
	        //Do nothing for now
	    }

	    @Override
	    protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) {
	        //Do nothing for now
	    }

	    @Override
	    protected void onFullPongMessage(WebSocketChannel channel, BufferedBinaryMessage message) {
	        //Do nothing for now
	    }

	    @Override
	    protected void onCloseMessage(CloseMessage closeMessage,  WebSocketChannel channel) {
	        //Do nothing for now
	    }
	}

To use WebSockets on a specific request you have to map your WebSocket
Controller in the Routes class with an appropriate method.

	- WSS:    /websocket    -> WebSocketController

Now you can start creating an application to access your
WebScoketController at the URL "/websocket". Of course, you can have multiple
WebSocket controllers in your application, each mapped to a specific URL.

As WebSockets are a bi-directional protocol, and the above descripted how to deal with
incoming event, you can also sent outgoing events by using the WebSocketManager.

    @Inject
    private WebSocketManager webSocketManager;

    public void sentEvent() {
        webSocketManager.getChannels("/websocket").forEach(channel -> {
        ...
        });
    }

The above example enables you access to all clients which have an open
WebSocket channel to the URL /websocket.

# Server-Sent Events

To use Server-Sent Event on a specific request you have to map a Server-Sent
Event in your routes.yaml

	- SSE:    /serversentevent

To send outgoing Server-Sent Event data, you can use the ServerEventManager.

    @Inject
    private ServerEventManager serverEventManager;

    public void sentEvent() {
        serverEventManager.getConnections("/serversentevent").forEach(connection -> {
            connection.send("foo");
        });
    }

The above example will send the data to all clients which have an open Servet-Sent Event
connection to the URL /serversentevent.

# Concurrency

Although mangoo I/O is a web framework, there may be situations where you need to postpone
a single, non periodic unit of work in the background and wait for it to finish.

For this purpose mangoo I/O offers the ExecutionManager, whic is just a simple wrapper
around the Java ExecutorService.

    @Inject
    private ExecutionManager executionManager;

    public void doSomething() {
        Future<String> future = executionManager.submit(new MyCallable());
    }

The manager offers some convenient methods for postpone task into the background of
your application. The ExecutorService works with a fixed Thread-Pool size with a default
value of 10. You can change this value via the application.yaml file.

# Utilities

mangoo I/O offers some nice utilities for making some task easy.

## Http-Utilities

For contacting third-party webservices mangoo I/O has integrated the
[HC fluent API](https://hc.apache.org/httpcomponents-client-ga/tutorial/html/fluent.html)
of the Apache HTTP components library.

Fluent-HC enables you to open outgoing HTTP connections

	Request.Get("http://somehost/")
	        .connectTimeout(1000)
	        .socketTimeout(1000)
	        .execute()
	        .returnContent()
	        .asString();

# i18n Internationalization

Translations in mangoo I/O are based on the standard Locale of Java.

The Locale is determined from a specific i18n cookie or, as default, from each
request from the Accept-Language header of the request.
If the Locale can not be determined from the request or the cookie, the default
language of the application from application.yaml will be used. If this configuration is
not set, mangoo I/O will default to "en".

If you want to force the language, you simply set the Locale in a filter
- see [Filters](#filters) for more information on filters.

	Locale.setDefault(Locale.ENGLISH);

You can set a locale cookie by using the CookieBuilder in one of your
controllers.

	public Response localize() {
	    Cookie cookie = CookieBuilder.create()
	            .name(Default.COOKIE_I18N_NAME.toString())
	            .value("en")
	            .build();

	    return Response.withOk().andCookie(cookie);
	}      

mangoo I/O offers you a convenient way of accessing translations. To get
hold of the translations simply inject the Messages class.

	package controllers;

	import com.google.inject.Inject;

	import io.mangoo.i18n.Messages;
	import io.mangoo.routing.Response;

	public class I18nController {

	    @Inject
	    private Messages messages;

	    public Response translation() {
	        messages.get("my.translation");
	        messages.get("my.othertranslation", "foo");
	        ...
	    }
	}

The messages class offers you two methods of retrieving translations
from the resource bundle. In this example a translation is called with
and without passing optional parameters. The corresponding translation
entries in the resource bundle would look like this

	my.translation=This is a translation
	my.othertranslation=This is a translation with the parameter: {0}

Note the {0} which will be replaced by the passed parameter "foo".

## Translation in templates

To access translation in a template, you can us a special tag a long with the key
for your translation.

	${i18n("my.translation")}

To pass a parameter to the translation simply append the parameter

	${i18n("my.othertranslation", "foo")}

If no key is found in the resource bundle the template will output an
empty value.

# Handling static files and assets

There is often a scenario where you have to serve static files or assets
to the client. Take the robots.txt or CSS and JS files for example.
mangoo I/O offers a convenient way of doing this. The src/main/resources
package must contain a folder called /files which is the entry point for
serving static files and assets. To serve a static file or asset you
have to create a mapping in the Routes class. You have to decide
if you want to serve a static file (a so called ResourceFile) or a
complete folder with all its sub-files and sub-folders (a so called
ResourcePath)

	- FILE:    /robots.txt
	- PATH:    /assets/

The above example maps a resource file located in
src/main/resources/files/robots.txt to the request URI /robots.txt and a
resource path located in src/main/resources/files/assets/ to all
requests with the prefix /assets/ in the URI. For example

	http://mydomain.com/robots.txt
	http://mydomain.com/assets/mycss.css

## On-the-fly asset minification

When in dev mode, mangoo I/O offers you the ability to minify CSS and JS
resources on-the-fly, giving a front-end developer the opportunity to
work in the raw CSS and JS files and have the minified version linked in
the default template of your application. Thus, there is no need for
extra minification or post processing before deployment to a production environment.

By default minification of CSS and JS resources is disabled and has to
be enable with the following options

	application:
	    minify:
	          js     : true
	          css    : true

By convention, if on-the-fly minification is activated mangoo I/O will check
for changes in all files ending with .css or .js that have no "min" in their
file name and are located in the following folder

	/src/main/resources/files/assets

Once a file is changed, mangoo I/O will automatically minify the file.
Already minified files, for example jquery.min.js will not be minified
again. The on-the-fly minification will create a file with the same
name, ending with .min.css or .min.js.

Of course you can configure the folder for the CSS and JS files in your
application. See [Configuration options](#configuration-options) for more
information about this.

> There is also an option on automatically GZIP your Assets.

# Testing

mangoo I/O ships with convenient tools for testing your application.
Please note, that these utilities are not part of the core and come with
a additional dependency. This is mainly because you want to set the scope of
this dependency set to "test" in your maven configuration.

	<dependency>
	    <groupId>io.mangoo</groupId>
	    <artifactId>mangooio-test-utilities</artifactId>
	    <version>1.0.0</version>
	    <scope>test</scope>
	</dependency>

## Using the TestSuite

manoo I/O uses a TestSuite for all unit testing. This concept allows you
to start the framework once, execute all unit test and shut the framework
down afterwards. For using a TestSuite you need an entry-point for the
execution which extends the MangooRunner interface.

	package mangoo;

	import io.mangoo.testing.MangooRunner;

	public class TestSuite extends MangooRunner {
	}

This just needs to be an empty class for telling Maven to use this Suite
when tests are executed. You can, of course, use this class to setup your unit
tests, like starting a database, etc.

Add the following plugin to your pom.xml to make Maven aware of your TestSuite class.

	<plugin>
	    <groupId>org.apache.maven.plugins</groupId>
	    <artifactId>maven-surefire-plugin</artifactId>
	    <version>x.x.x</version>
	    <configuration>
	        <includes>
	            <include>**/*TestSuite.java</include>
	        </includes>
	    </configuration>
	</plugin>

By convention, the TestSuite will execute all tests that ends with
"*Test" in their class name.

## Frontend testing

For frontend testing mangoo I/O uses
[FluentLenium](https://github.com/FluentLenium/FluentLenium). Here is an example of how a FluentLenium test might look
like.

	package mangoo.controllers;

	import static org.junit.Assert.assertTrue;
	import io.mangoo.testing.MangooUnit;

	import org.junit.Test;

	public class FluentTest extends MangooFluent {

	    @Test
	    public void title_of_bing_should_contain_search_query_name() {
	        goTo("http://www.bing.com");
	        fill("#sb_form_q").with("FluentLenium");
	        submit("#sb_form_go");
	        assertTrue(title().contains("FluentLenium"));
	    }
	}

## Backend testing

mangoo I/O provides convinent classes to support unit testing your application.

Here is an example of how a unit test with the test utilities might look like.

    ...
    import io.mangoo.test.utils.Request;
    import io.mangoo.test.utils.Response;
    ...

    @Test
    public void testIndex() {
        //given
        Response response = Request.get("/").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }

The most common use case is probably a request-response test with
your application. Therefore, mangoo I/O provides your with a test
utility for Request and Response. You can add authentication, headers, etc.
to the request. Check the fluent API of the Request object for this.

There may be situation where you need to pass the request information
along to the request. For this scenarios mangoo I/O provides you with
the Browser class.

	Browser browser = Browser.open();

The browser class enables you to pass to keep the request information
on the following requests.

Here is an example on how this might look like.

        ...
        import io.mangoo.test.utils.Browser;
        import io.mangoo.test.utils.Request;
        import io.mangoo.test.utils.Response;
        ...

        //given
        Browser browser = Browser.open();

        //when
        Response response = browser.withUri("/dologin")
                .withMethod(Methods.POST)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));

        //when
        response = browser.withUri("/authenticationrequired")
                .withDisableRedirects(true)
                .withMethod(Methods.GET)
                .execute();

The information from the first request, like cookies, etc. will be passed to
the following request, enabling you a browser-like testing of your application.

# Deployment

The full stack architecture of mangoo I/O offers the ability to create a
single JAR file containing all required dependencies, ready to start the
built-in Undertow server.

To create a deployable JAR file, execute the following command

	mvn clean package

The [Maven Shade
Plugin](https://maven.apache.org/plugins/maven-shade-plugin) will generate the JAR file, which you can find in the target
directory once the maven build is complete. By default, the JAR file
will be named "mangooioapp.jar" (if you have created your mangoo I/O project
via the archetype). You can change the name in your pom.xml file in
the Shade Plugin configuration.

	<finalName>mangooioapp</finalName>

After you have deployed the jar to your production environment, you can
start the application by executing the following command

	java -jar app.jar

This will start mangoo I/O in production mode, using the default
configuration from your application.yaml

> Also Undertow is production-ready, it is recommended to use a front-end HTTP
> server such as nginx, Apache, etc. to leverage an easy configuration for
> load-balancing, SSL termination, etc.

## Debian init.d script

The following script is an example of how to start, stop and restart a
mangoo I/O application as a deamon on Debian.

	#!/bin/sh
	### BEGIN INIT INFO
	# Provides:          mangoo I/O
	# Required-Start:    $syslog
	# Required-Stop:     $syslog
	# Default-Start:     2 3 4 5
	# Default-Stop:      0 1 6
	# Short-Description: Start/Stop mangoo I/O Application
	### END INIT INFO

	### CONFIGURATION ###

	NAME=MyApplication
	APPLICATION_PATH=/path/to/application/app.jar

	XMX=128m
	XMS=64m

	DAEMON=/usr/bin/java

	chown www-data:www-data /path/to/application/app.jar
	### CONFIGURATION ###

	PIDFILE=/var/run/$NAME.pid
	USER=www-data

	case "$1" in
	  start)
	        echo -n "Starting "$NAME" ..."
	        start-stop-daemon --start --quiet --make-pidfile --pidfile $PIDFILE --chuid ${USER} --background --exec $DAEMON -- $DAEMON_OPTS
	        RETVAL=$?
	        if [ $RETVAL -eq 0 ]; then
	                echo " Success"
	            else
	                echo " Failed"
	        fi
	        ;;
	  stop)
	        echo -n "Stopping "$NAME" ..."
	        start-stop-daemon --stop --quiet --oknodo --pidfile $PIDFILE
	        RETVAL=$?
	        if [ $RETVAL -eq 0 ]; then
	                echo " Success"
	            else
	                echo " Failed"
	        fi
	        rm -f $PIDFILE
	        ;;
	  restart)
	        echo -n "Stopping "$NAME" ..."
	        start-stop-daemon --stop --quiet --oknodo --retry 30 --pidfile $PIDFILE
	        RETVAL=$?
	        if [ $RETVAL -eq 0 ]; then
	                echo " Success"
	            else
	                echo " Failed"
	        fi
	        rm -f $PIDFILE
	        echo -n "Starting "$NAME" ..."
	        start-stop-daemon --start --quiet --make-pidfile --pidfile $PIDFILE --chuid ${USER} --background --exec $DAEMON -- $DAEMON_OPTS
	        RETVAL=$?
	        if [ $RETVAL -eq 0 ]; then
	                echo " Success"
	            else
	                echo " Failed"
	        fi
	        ;;
	   status)
	        if [ -f $PIDFILE ]; then
	                echo $NAME" is running"
	        else
	                echo $NAME" is NOT not running"
	        fi
	        ;;
	*)
	        echo "Usage: "$1" {start|stop|restart|status}"
	        exit 1
	esac

	exit 0

Place this script in /etc/init.d and use it as follows

	chmod +x /etc/init.d/MyScript
	/etc/init.d/MyScript (start|stop|restart|status)

If you are using Debian, than [Supervisord](http://supervisord.org=) might be an alternative to the init.d Script.

## How to contribute

As mangoo I/O is an open source project hosted on
[GitHub](https://github.com/svenkubiak/mangooio), you are welcome to
contribute to the Framework. Pull requests containing bug fixes or
further enhancements are more than welcome. Please make sure, that your
code is well tested and documented.

If you want to stay up to date on the latest news for mangoo I/O you can
follow the Twitter account [@mangoo_io](https://twitter.com/mangoo_io).

If you find a bug, please open an issue. If you find a security flaw,
please send an eMail to webmaster@mangoo.io so it can be fixed ASAP.

## Extensions

Extensions are a way of adding features to mangoo I/O which are not part
of the core. The most popular example of an extension is persistence.
Here you'll find a list of existing extensions.

MongoDB Extension

* [https://github.com/svenkubiak/mangooio-mongodb-extension](https://github.com/svenkubiak/mangooio-mongodb-extension)

Hibernate Extension

* [https://github.com/svenkubiak/mangooio-hibernate-extension](https://github.com/svenkubiak/mangooio-hibernate-extension)

Mailer Extension

* [https://github.com/svenkubiak/mangooio-mailer-extension](https://github.com/svenkubiak/mangooio-mailer-extension)

If you have created and extension and want it to be listed here, just
add your extension to the above list by editing the documentation.asciidoc
file and create a pull request on the GitHub repository. You can find the documentation file in mangooio-core at
[/src/main/documentation/documentation.asciidoc](https://github.com/svenkubiak/mangooio/tree/master/mangooio-core/src/main/documentation).

## Configuration options

This is an overview of the configuration options for the
application.yaml and their default values, if the properties are not configured
in the application.yaml file.

|*Option name* |*Description* |*Default value* |*Note* |
| ------------ | ------------ | -------------- | -------------- |
|application.secret |The application secret |Random value |Must be at least 16 characters or mangoo I/O won't start|
|application.name |The name of the application |mangooio| - |
|application.language |The default language of the application |en |Used as a fallback value for Locale| - |
|application.minify.js |Wether to minify javascript assets or not |false|Only used in dev mode|
|application.minify.jsfolder |The folder containing js files|/src/main/resources/files/assets/js |Only used in dev mode|
|application.minify.gzipjs |Whether to GZIP JS files or not|false |Only used in dev mode|
|application.minify.css |Wether to minify stylesheet assets or not|false |Only used in dev mode|
|application.minify.cssfolder |The folder containing css files|/src/main/resources/files/assets/css |Only used in dev mode|
|application.minify.gzipcss |Whether to GZIP CSS files or not|false |Only used in dev mode|
|application.host |The address the undertow server is running on|127.0.0.1 |In 99% of all cases, this is the 127.0.0.1|
|scheduler.autostart |Wether to autostart the scheduler or not|true | - |
|scheduler.package |The package containing the quartz scheduler jobs|jobs | - |
|application.port |The port the undertow server is listening on |8080| - |
|application.admin.health |Enable or disable administrative /@health URL |false| - |
|application.admin.routes |Enable or disable administrative /@routes URL |false| - |
|application.admin.system |Enable or disable administrative /@system URL |false| - |
|application.admin.config |Enable or disable administrative /@config URL |false| - |
|application.admin.cache |Enable or disable administrative /@cache URL |false| - |
|application.admin.scheduler |Enable or disable administrative /@scheduler URL |false| - |
|application.admin.username |The username for all administrative URLs | - | - |
|application.admin.password |The password for all administrative URLs as SHA512 hashed value | - | - |
|cookie.version |Sets the version of a session cookie |0| - |
|cookie.name |The name of the session cookie |$application.name-MANGOOIO-SESSION| - |
|cookie.expires |The time in seconds when the session expires |86400| - |
|cookie.encrypt |Whether to encrypt the session cookie or not |false| - |
|cookie.secure |Whether to set the secure flag for the session cookie or not |false| - |
|cache.maxsize |The maximum number of elements in the cache |5000| - |
|execution.threadpool |Number of threads in the ExecutionManager|10| - |
|auth.cookie.name |The name of the authentication cookie|$application.name-MANGOO-AUTH| - |
|auth.cookie.expire |The time in seconds how long the user stays logged in even is the browser is closed |3600| - |
|auth.cookie.remember.expire |The time in seconds how long the user stays logged in if remember is set with true when logging in |1209600| - |
|auth.cookie.encrypt |Whether to encrypt the authentication cookie or not |false| - |
|auth.cookie.version |Sets the version of an authentication cookie |0| - |
|auth.cookie.secure |Whether to set the secure flag for the auth cookie or not |false| - |
|auth.login.redirect |The URL a user is redirected when not logged in| - | - |