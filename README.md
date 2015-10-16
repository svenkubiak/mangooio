[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.mangoo/mangooio/badge.svg?style=flat)](http://search.maven.org/#search|ga|1|io.mangoo)
[![Travis Build Status](https://travis-ci.org/svenkubiak/mangooio.svg?branch=master)](http://travis-ci.org/svenkubiak/mangooio)
[![codecov.io](http://codecov.io/github/svenkubiak/mangooio/coverage.svg?branch=master)](http://codecov.io/github/svenkubiak/mangooio?branch=master)

If this software is useful to you, you can support further development by using Flattr. Thank you!

[![Flattr this repository](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=svenkubiak&url=https://github.com/svenkubiak/mangooio&title=mangooio&language=en&tags=github&category=software)


mangoo I/O
================

mangoo I/O is an Intuitive, Lightweight, High Performance Full Stack Java Web Framework.

Development of mangoo I/O was started in mid 2015 out of the interest on
how difficult it would be to create an intuitiv, developer friendly,
full stack java web framework from scratch. After the fresh new breeze of
java development for the Web with the [Play
Framwork - Version 1](https://www.playframework.com), contributions to the
[Ninja Framework](http://www.ninjaframework.org) and having seen a lot of
cumbersome "Enterprise" Applications, I thought it was time for yet
another full stack java framework.

The foundation of mangoo I/O is the very performant
[Undertow](http://undertow.io) web server from JBoss. On top of that,
standard, production ready java libraries are used - no reinventing of the
wheel, no bytecode manipulation, no magic whatsoever. The main reason for using Undertow was, that
it is based on non-blocking I/O in the form of
[XNIO](http://xnio.jboss.org). And although Undertow does support the
servlet API, one is not bound to use it in any way. Giving a java developer
the opportunity to work fully stateless.

mangoo I/O is highly inspired by the [Ninja
Web Framework](http://www.ninjaframework.org). Although the mangoo I/O core is a complete custom
implementation, many of the ideas and methodologies were re-used.

Here are some key features of mangoo I/O in a nutshell:

* Intuitiv convention-over-configuration, making a java developer feel
at home from the start
* Highly scalable using a share-nothing stateless architecture
* Hot-Compiling development mode for high productivity
* Easy to use template engine
* Support for Web Sockets
* Simple and self-explaining form handling and validation
* Plain scheduling for recurring tasks
* Easy handling of JSON in- and output
* Built-in asset minification in development mode
* Flexible testing tools
* Simple Deployment and CI-Integration
* i18N Internationalization
* Easy eMail handling

One main focus of mangoo I/O was to have a good and well documented code
base. Therefore, mangoo I/O is constantly checked against
[SonarQube](http://www.sonarqube.org) with a rule set of more than 600
checks. Additionally each build is checked against [Loader.io](https://loader.io/) to ensure, that
code changes don't decrease the framework performance.

Here are some used libraries and their purpose in mangoo I/O.

* [Maven](https://maven.apache.org/) - Dependency management, built-system, packaging
* [Undertow](http://undertow.io/) - Web Server
* [Google Guice](https://github.com/google/guice) - Dependency injection
* [Logback](http://logback.qos.ch/) - Logging
* [Freemarker](http://freemarker.org/) - Template engine
* [Quartz Scheduler](https://quartz-scheduler.org/) - Scheduling
* [Google Guava](https://github.com/google/guava) - Caching
* [Boon JSON](https://github.com/boonproject/boon) - JSON parser
* And many more ...

**Homepage**   
[https://mangoo.io](https://mangoo.io) 

**Discussion board**   
[https://groups.google.com/forum/#!forum/mangooio](https://groups.google.com/forum/#!forum/mangooio)

**Documentation**   
[https://mangoo.io/documentation](https://mangoo.io/documentation)  

**Changelog**   
[https://mangoo.io/changelog](https://mangoo.io/changelog)  

**Apidocs**   
[http://svenkubiak.github.io/mangooio/](http://svenkubiak.github.io/mangooio/)  

**Twitter**  
[https://twitter.com/mangoo_io](https://twitter.com/mangoo_io)