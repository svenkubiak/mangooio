[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.mangoo/mangooio/badge.svg?style=flat)](http://search.maven.org/#search|ga|1|io.mangoo)
[![Travis Build Status](https://travis-ci.org/svenkubiak/mangooio.svg?branch=master)](http://travis-ci.org/svenkubiak/mangooio)


mangoo I/O
================

mangoo I/O is a Modern, Intuitive, Lightweight, High Performance Full Stack Java Web Framework.

Development of mangoo I/O started in mid 2015 out of the interest on
how difficult it would be to create an intuitive, developer friendly,
full stack Java web framework from scratch. After a fresh new breeze of
Java development for the web with the [Play
Framwork - Version 1](https://www.playframework.com), contributions to the
[Ninja Framework](http://www.ninjaframework.org), and having seen a lot of
cumbersome "Enterprise" applications, I thought it was time for yet
another full stack Java web framework.

Developing mangoo I/O will always be about having a developer friendly,
fluent and easy to understand web framework with a small learning curve for the Java ecosystem.
This will be the untouchable base line.

At its core, mangoo I/O is a classic MVC-Framework. The foundation of mangoo I/O is the high
performant [Undertow](http://undertow.io) web server from JBoss. On top of that, standard, production ready Java libraries are used - no reinventing of the wheel, no bytecode manipulation, no magic whatsoever.

The main reason for using Undertow was that it is based on non-blocking I/O in the form of XNIO. And although Undertow does support the servlet API, one is not bound to use it in any way, giving a Java developer the opportunity to work fully stateless and being independent to the servlet API.

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
* Strong cryptography and enforced security
* Build-in asset minification in development mode
* Flexible testing tools
* Build-in authentication tools
* Support for OAuth with Twitter, Google and Facebook
* Simple Deployment and CI-Integration
* Preprocessor for LESS and SASS
* i18N Internationalization
* And many more...

One main focus of mangoo I/O was to have a very good and well documented code
base. Therefore, mangoo I/O is constantly checked against
[SonarQube](http://www.sonarqube.org) with a rule set of more than 1000
checks. Additionally each build is checked against [Loader.io](https://loader.io/) to ensure, that
code changes don't decrease the framework performance.

Here are some used libraries and their purpose in mangoo I/O.

* [Maven](https://maven.apache.org/) - Dependency management, built-system, packaging
* [Undertow](http://undertow.io/) - Web Server
* [Google Guice](https://github.com/google/guice) - Dependency injection
* [Log4j 2](https://logging.apache.org/log4j/2.x/) - Logging
* [Ehcache](http://www.ehcache.org/) - Cacheing
* [Freemarker](http://freemarker.org/) - Template engine
* [Quartz Scheduler](https://quartz-scheduler.org/) - Scheduling
* [Jackson](https://github.com/FasterXML/jackson), [JSONPath](http://goessner.net/articles/JsonPath/) - JSON handling
* [JUnit](http://junit.org/), [FluentLenium](https://github.com/FluentLenium/FluentLenium) - Testing
* [JBcrypt](http://www.mindrot.org/projects/jBCrypt/) - Strong hashing
* [Bouncy Castle](https://www.bouncycastle.org/) - Strong cryptography
* And many more ...

**Homepage**   
[https://github.com/svenkubiak/mangooio](https://github.com/svenkubiak/mangooio)

**Documentation**   
[https://github.com/svenkubiak/mangooio/wiki](https://github.com/svenkubiak/mangooio/wiki)  

**Changelog**   
[https://github.com/svenkubiak/mangooio/wiki/Changelog](https://github.com/svenkubiak/mangooio/wiki/Changelog)  

**Apidocs**   
[http://svenkubiak.github.io/mangooio/](http://svenkubiak.github.io/mangooio/)

**Twitter**  
[https://twitter.com/mangoo_io](https://twitter.com/mangoo_io)
