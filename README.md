[![Maven Central](https://img.shields.io/maven-central/v/io.mangoo/mangooio)](https://mvnrepository.com/artifact/io.mangoo/mangooio)
![SemVer](https://img.shields.io/badge/SemVer-2.0.0-green)
[![Buy Me a Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-%F0%9F%8D%BA-yellow)](https://buymeacoffee.com/svenkubiak)

# mangoo I/O

mangoo I/O is a Java web framework. It runs on [Undertow](http://undertow.io/), keeps shared state in MongoDB, and stores sessions and authentication in encrypted cookies. Lockouts, a second factor, form limits, and Argon2 hashing are part of the framework. Token revocation across instances is there too, and it stays off until `authentication.blacklist` is set.

Applications are MVC: a route in `app.Bootstrap` points at a controller method, the method returns a `Response`, and Freemarker or JSON does the rest. Configuration is one YAML file. Injection is Google Guice. The application module has to be named `app.Module`.

Each build is checked with [SonarQube](http://www.sonarqube.org/), about 1,300 rules.

## Features

- Routes, controllers, and filters are ordinary Java. `mvn mangooio:run` restarts the process when compiled classes change. It does not compile them.
- MongoDB through the official driver, with a small `Datastore` on top. Freemarker for HTML. Jackson for JSON.
- One instance or many. Login lockouts, used TOTP codes, and (when enabled) revoked tokens are shared through MongoDB. The application cache and the `EventBus` stay on the instance that owns them.
- Cookie authentication, optional TOTP, CSRF, CORS, and Argon2id. Sessions and flash are encrypted JWT cookies, so there is no sticky session.
- Server-Sent Events, including delivery to other instances through MongoDB. WebSocket routes are Undertow handlers on the local instance. There is no fan-out.
- `@Run` for fixed-delay and cron jobs, on every instance. Mail is sent on a virtual thread.
- An admin UI, health probes, optional OpenTelemetry, and a fat JAR. See the [operating guide](https://svenkubiak.github.io/mangooio/operating/).
- Message bundles under `translations/`, cookie `i18n.cookie.name`.

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