## From 7.18.0 to 7.19.0
This is a drop-in replacement.

## From 7.17.0 to 7.18.0
This is a drop-in replacement.

## From 7.16.0 to 7.17.0
This is a drop-in replacement.

## From 7.15.0 to 7.16.0
This is a drop-in replacement.

## From 7.14.0 to 7.15.0
This is a drop-in replacement.

## From 7.13.0 to 7.14.0
This is a drop-in replacement.

## From 7.12.0 to 7.13.0
The annotations for @FilterWith and @Run have been moved. Reorganizing imports is advised.

## From 7.11.0 to 7.12.0
This is a drop-in replacement.

## From 7.10.0 to 7.11.0
This is a drop-in replacement.

## From 7.9.0 to 7.10.0
This is a drop-in replacement.

## From 7.8.0 to 7.9.0
This is a drop-in replacement.

## From 7.7.0 to 7.8.0
This is a drop-in replacement.

## From 7.6.0 to 7.7.0
This is a drop-in replacement.

## From 7.5.0 to 7.6.0
This is a drop-in replacement.

## From 7.4.0 to 7.5.0
This is a drop-in replacement.

## From 7.3.0 to 7.4.0
This is a drop-in replacement.

## From 7.2.0 to 7.3.0
With this release the cache implementation was switched from ehCache to Google Guava cache. This was a drop-in replacement and should be in no effect to end-users.

The WebSocket implementation had to be removed and will not be replaced.

The ServerSentEvent implementation had to be completely refactored.

## From 7.1.0 to 7.2.0
This is a drop-in replacement.

## From 7.0.0 to 7.1.0
This is a drop-in replacement.

## From 6.x.x to 7.0.0

mangoo I/O 7.0.0 is a major release and contains code that will break API compatibility. These are the changes you need to consider when upgrading to 7.x:

**Java 17**

mangoo I/O now requires and uses Java 17.

**Removed build-in rate limit handling**

The built-in rate limit handling has been remove, as rate limiting should be done in a HTTP-Proxy (e.g. nginx).

**Authenticity handling has been removed**

Authenticity handling has been removed, as now all major browser support SameSite Cookies which is default in mangoo I/O.

**Removed slim-jar deployment feature**

The slim-jar deployment feature has been removed as at was to error prune.

**Removed JCasbin and authroization handling**

The JCasbin library and he authorization feature has been remove as the integration and maintenance was to cumbersome.

**Switched (password) hashing fromJBcrypt to Argon2**

The built-in password hashing has changed to Argon2. If you have used the build-in authentication mechanism you need to update your stored passwords.

## From 6.14.0 to 6.15.0
This is a drop-in replacement.

## From 6.13.0 to 6.14.0
This is a drop-in replacement.

## From 6.12.0 to 6.13.0
This is a drop-in replacement.

## From 6.11.0 to 6.12.0
This is a drop-in replacement.

## From 6.10.0 to 6.11.0
This is a drop-in replacement.

## From 6.9.0 to 6.10.0
This is a drop-in replacement.

## From 6.8.0 to 6.9.0
This is a drop-in replacement.

## From 6.7.0 to 6.8.0
This is a drop-in replacement.

## From 6.6.0 to 6.7.0
This is a drop-in replacement.

## From 6.5.0 to 6.6.0
This is a drop-in replacement.

## From 6.4.0 to 6.5.0
This is a drop-in replacement.

## From 6.3.0 to 6.4.0
This is a drop-in replacement.

## From 6.2.0 to 6.3.0
This is a drop-in replacement.

## From 6.1.0 to 6.2.0
This is a drop-in replacement.

## From 6.0.0 to 6.1.0
This is a drop-in replacement.

## From 5.x.x to 6.0.0
mangoo I/O 6.0.0 is a major release and contains code that will break API compatibility. These are the changes you need to consider when upgrading to 6.x:

**Removed 2FA from Basic authentication**

In previous versions it was possible to add and additional 2FA to basic authentication. This functionality has been removed.

**Removed SASS and LESS precompiler**

The precompiler functionality for SASS and LESS has been removed, mainly due to the fact that both Libraries were blowing up the FatJAR by more than 10MB.

**Renamed Filter interfaces**

The naming of the global and per request filter has changed.
"MangooFilter" is now "PerRequestFilter" and "MangooRequestFilter"is nor "OncePerRequestFilter".

**Switched Authentication of Admin UI**

The Admin UI was accessible through Basic authentication. This has been changed to a Form/Cookie based authentication and now offers the option to add 2FA to the Admin UI. Configuration of credentials for the Admin UI remains unchanged.

**Added persistence to core**
One of the biggest changes in this version is the additional persistence based on MongoDB/Morphia to the Core. There is no need to add the additional mangooio-mongodb-extension anymore.

## From 5.15.0 to 5.16.0
This is a drop-in replacement.

## From 5.14.0 to 5.15.0
This is a drop-in replacement.

## From 5.13.2 to 5.14.0
This is a drop-in replacement.

## From 5.13.1 to 5.13.2
This is a drop-in replacement.

## From 5.13.0 to 5.13.1
This is a drop-in replacement.

## From 5.12.0 to 5.13.0
This is a drop-in replacement.

## From 5.11.0 to 5.12.0
This is a drop-in replacement.

## From 5.10.0 to 5.11.0
This is a drop-in replacement.

## From 5.10.0 to 5.10.1
This is a drop-in replacement.

## From 5.9.0 to 5.10.0
This is a drop-in replacement.

## From 5.8.1 to 5.9.0
This is a drop-in replacement.

## From 5.8.0 to 5.8.1
This is a drop-in replacement.

## From 5.7.0 to 5.8.0
This is a drop-in replacement.

## From 5.6.0 to 5.7.0
This is a drop-in replacement.

## From 5.5.0 to 5.6.0
This is a drop-in replacement.

## From 5.4.0 to 5.5.0
This is a drop-in replacement.

## From 5.3.0 to 5.4.0
This is a drop-in replacement.

## From 5.2.0 to 5.3.0
This is a drop-in replacement.

## From 5.1.0 to 5.2.0
This is a drop-in replacement.

## From 5.0.1 to 5.1.0
This is a drop-in replacement.

## From 5.0.0 to 5.0.1
This is a drop-in replacement.

## From 4.x.x to 5.0.0
mangoo I/O 5.0.0 is a major release and contains code that will break API compatibility. These are the changes you need to consider when upgrading to version 5.0.0:

**Java version**

Java 11 is now required and supported. It is recommended to set a maven property

	<properties>
		<java.version>11</java.version>
	</properties>

and set the source and target variable accordingly in the compiler plugin

	<plugin>
		<groupId>org.apache.maven.plugins</groupId>
		<artifactId>maven-compiler-plugin</artifactId>
		<version>3.8.0</version>
		<configuration>
			<source>${java.version}</source>
			<target>${java.version}</target>
			<compilerArgument>-parameters</compilerArgument>
			<optimize>true</optimize>
			<debug>false</debug>
		</configuration>
	</plugin>

**Plugin versions**

It is recommended to check that your plugins are at the latest available versions.

**Maven Surefire Plugin**

In previous versions the Surefire Plugin was used to execute the Unit test via a TestSuite. This has been removed and the according configuration can be removed in the Maven Plugin.

	<plugin>
		<groupId>org.apache.maven.plugins</groupId>
		<artifactId>maven-surefire-plugin</artifactId>
		<version>3.0.0-M1</version>
	</plugin>	

**Maven Jar Plugin**

As integration of Log4j2 has become cleaner (more on that see below), the Log4j2-test file needs to be filtered when the JAR is created. Therefore, the following configuration needs to be added to the Jar Plugin
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-jar-plugin</artifactId>
<version>3.1.0</version>
<configuration>
<excludes>
<exclude>**/log4j2-test*</exclude>
</excludes>
</configuration>
</plugin>

**Renamed conf package**

The previously named "conf" package containing the Google Guice Module configration and the Lifecycle class has been renamed to "app" and must be renamed accordingly.

**Renamed Lifecycle class**

The previously named "Lifecycle" class is now called "Bootstrap" and must be renamed accordingly. It must also implement the new "MangooBootstrap" interface instead of "MangooLifecycle".

**Updated Module configuration**

In additional of the renaming the Lifecylce class, the binding in the module configuration needs to be updated accordingly

	bind(MangooBootstrap.class).to(Bootstrap.class);

**Removed AuthenticationFilter**

The AuthenticationFilter has been removed as this is now handled via the new programmatically routes configuration (more on that see below).

**Removed ETag for dynamic content**

The ETag function for dynmaic content has been removed. Please update your inovcations of the Response class accordingly.

**Cleanup of Log4j2 integration**

The integration of Log4j2 is now much cleaner and closer to the standard. You are not bound anymore to use the configuration via *.yaml files and can now use all available file extensiosn to configure your logging. That being sad, the Jackson yaml library has been removed from mangoo-core. If you want to continue using the exsiting yaml file configuraiton you need to added the following dependency

	<dependency>
		<groupId>com.fasterxml.jackson.dataformat</groupId>
		<artifactId>jackson-dataformat-yaml</artifactId>
		<version>2.9.7</version>
	</dependency>

Furthermore, the integration now follows the [automatic configuration](https://logging.apache.org/log4j/2.x/manual/configuration.html) of Log4j2, looking for specific files during the startup process. This means, that the previous environment specific configuration with log4j2.prod.yaml, log4j2.dev.yaml and log4j2.test.yaml has been dropped. In order to configure Log4j2 to specific environment, we are now following the Log4j2 standard by using

	log4j2-test.*

for dev and test. And

	log4j2.*

for production environments.

This makes it important to filter the test Log4j2 test configuration during the JAR build (see above). If the Log4j2 test configuration is not filtered during JAR build, it will become active in production.

**New config.props**

The previously used yaml configration (application.yaml) has been dropped in favour of [Jodd props](https://jodd.org/props) which give much more flexibility when it comes to cofiguring the application. Please refer to the [updated documentation](https://github.com/svenkubiak/mangooio/wiki/Configuration) on how to setup the new Jodd props based configuration file.

**Migrated to JUnit5**

mangoo I/O has been updated from JUnit4 to JUnit5. Please refer to the [updated documentaion](https://github.com/svenkubiak/mangooio/wiki/Testing) on how to configure and run Unit tests with the updated version.

**New programmatically routing**
Configuration of routes through the routes.yaml file has been dropeed in favour of a programmatically route configuration. Please refer to the [updated documentation](https://github.com/svenkubiak/mangooio/wiki/Routing) on how to setup routes programmatically.

## From 4.17.0 to 4.18.0
This is a drop-in replacement.

## From 4.16.0 to 4.17.0
This is a drop-in replacement.

## From 4.15.0 to 4.16.0
This is a drop-in replacement.

## From 4.14.0 to 4.15.0
This is a drop-in replacement.

## From 4.13.0 to 4.14.0
This is a drop-in replacement.

## From 4.12.0 to 4.13.0

Due to security concerns in regards to JWT, refactorings where required that break API compatability.

The application won't start if either a 512 Bit application secret is set or the following configuration values have a 512 Bit secret:

```
 session:
    cookie:
       signkey: ...
       encryptionkey: ...

 flash:
    cookie:
       signkey: ...        
       encryptionkey: ...

 authentication:
     cookie:
         signkey : ...      
         encryptionkey: ...
```

Please note, that starting with this version all values for the above cookies are encrypted by default.

## From 4.11.0 to 4.12.0

This is a drop-in replacement.

## From 4.10.0 to 4.11.0

This is a drop-in replacement.

## From 4.9.0 to 4.10.0

Required refactoring in the configuration lead to renaming of some configuration values.

Previously the session cookie was configured

```
cookie:
  name: ...
  encrypt: ...
```

This has been refactored to

```
session:
  cookie:
    name:
    encrypt: ..
```

Previously the authentication cookie was configured

```
auth:
  cookie:
    name: ...
    encrypt: ...
```

This has been refactored to

```
authentication:
  cookie:
    name: ...
      encrypt: ...
```

## From 4.8.0 to 4.9.0

This is a drop-in replacement.

## From 4.7.0 to 4.8.0

This is a drop-in replacement.

## From 4.6.0 to 4.7.0

This is a drop-in replacement.

## From 4.5.0 to 4.6.0

There was a required refactoring of the authentication class. Previously, the validLogin method did also execute the actual login. This has been removed and now the newly created login method has to be performed to actually do the login.

Additionally some dependencies where challenged and removed. So you might see some red flags, when using dependencies that where shipped with mangoo I/O.

## From 4.4.0 to 4.5.0

This is a drop-in replacement.

## From 4.3.0 to 4.4.0

This is basically a drop-in replacement. However, there have been some required refctorings on the Utility class. The RequestUtils and the TwoFactorUtils have been refactored and now sit in the helper package. To avoid concurrency issues these classes can not be accessed in a static way anymore and need to be injected.

## From 4.2.0 to 4.3.0

This is a drop-in replacement.

## From 4.1.0 to 4.2.0

This is a drop-in replacement.

## From 4.0.0 to 4.1.0

This is a drop-in replacement.

## From 3.x.x to 4.0.0

mangoo I/O 4.0.0 is a major release and contains code that will break API compatibility. These are the changes you need to consider when upgrading to version 4.0.0:

* All @depracted classes and methods have been removed
* A new method in the MangooLifecycle interface has been added

**Changes in routes.yaml**  
The syntax of the routes.yaml file has been updated to add more flexibility to the creating of routes. The new syntax requires the routes file to start with the following line:

```
routes:
```

Also each route has a new syntax. Here is an example

```
- method: GET
  url: /
  mapping: ApplicationController.index
```

Click [here](https://docs.mangoo.io/routing.html) for the complete documentation on the new routes syntax.

**Changes to application.conf \(1\)**  
As a new AJP connector has been added, the configuration for the listening host and port has been changed. The new syntax is as follows:

```
default:
    connector:
        http:
            host      : localhost
            port      : 9898
        ajp:
            host      : localhost
            port      : 9899
```

Click [here](https://docs.mangoo.io/configuration.html) for the complete documentation on the new application properties. Also see the updated overview of the [default configuration values](https://docs.mangoo.io/default-values.html).

**Changes to application.conf \(2\)**  
Encryption with AES256 is now enforced, which requires you to set an application secret with at least 32 characters.

**Changes to application.conf \(3\)**  
The basic authentication for the Admin UI has been updated. The password now has to be configured as an JBcrypt hased value. Hint: You can create a hashed value using the [Admin UI](https://docs.mangoo.io/administration.html).

## From 3.8.0 to 3.9.0

This is a drop-in replacement.

## From 3.7.0 to 3.8.0

This is a drop-in replacement.

## From 3.6.0 to 3.7.0

This is a drop-in replacement.

## From 3.5.0 to 3.6.0

This is a drop-in replacement.

## From 3.4.0 to 3.5.0

This is a drop-in replacement.

## From 3.3.0 to 3.4.0

This is a drop-in replacement.

## From 3.2.0 to 3.3.0

This is basically a drop-in replacement. However, this release fixes a bug with the WebRequest, WebResponse and WebBrowser classes which were integrated from the test project in the 3.0.0 release. If you have used this classes \(which probably failed due to the bug\) I have bad news. As the bug was so major, these utility classes have been removed from the core and are now back in the test project.

## From 3.1.0 to 3.2.0

This is a drop-in replacement.

## From 3.0.0 to 3.1.0

There have been last minute changes on the Scheduler. If you are using the scheduler in manual mode \(e.g. starting and stopping the scheduler manualy\), then your need to catch the newly thrown exceptions from the scheudler class on some methods. Otherwise version 3.1.0 is a drop in replacement. If you have previously used the mailer extension to send emails from mangoo I/O, I have good news for you. Sending e-mails has been re-integrated into mangoo I/O but in a more fluent way than before. Check out the documentation on [how to send e-mails](https://docs.mangoo.io/sending-emails.html).

## From 2.x.x to 3.0.0

mangoo I/O 3.0.0 is a major release and contains code that will break API compatibility. These are the changes you need to consider when upgrading to version 3.0.0:

* The mangooio-test-utilities artifact has been renamed to mangooio-test
* All @depracted classes and methods have been removed
* The authentication class has been moved from io.mangoo.authentication to io.mangoo.routing.bindings.authentication
* The on-the-fly GZIP compression for CSS and JS files has been removed, as this belongs in a “real” webserver
* The log4j2.xml configuration has been switched to log4j2.yaml configuration, as all configuration files are now YAML based
* The Admin UI was completely refactored and is now available under the /@admin URL. The /@health URL has been removed completely
* The Test-Utitlites for HTTP Requests \(Request, Response and Browser\) have been moved to the core and have been renamed to WebRequest, WebResponse and WebBrowser
