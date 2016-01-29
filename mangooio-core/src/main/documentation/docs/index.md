# Welcome to mangoo I/O

An Intuitive, Lightweight, High Performance Full Stack Java Web Framework.

## In a Nutshell.

Development of mangoo I/O was started in mid 2015 out of the interest on how difficult it would be to create an intuitiv, developer friendly, full stack java web framework from scratch. After the fresh new breeze of java development for the Web with the Play Framwork - Version 1, contributions to the Ninja Framework and having seen a lot of cumbersome "Enterprise" Applications, I thought it was time for yet another full stack java framework.

The foundation of mangoo I/O is the very performant Undertow web server from JBoss. On top of that, standard, production ready java libraries are used - no reinventing of the wheel, no bytecode manipulation, no magic whatsoever. The main reason for using Undertow was, that it is based on non-blocking I/O in the form of XNIO. And although Undertow does support the servlet API, one is not bound to use it in any way. Giving a java developer the opportunity to work fully stateless.

mangoo I/O is highly inspired by the Ninja Web Framework. Although the mangoo I/O core is a complete custom implementation, many of the ideas and methodologies were re-used.

One main focus of mangoo I/O was to have a good and well documented code base. Therefore, mangoo I/O is constantly checked against SonarQube with a rule set of more than 600 checks. Additionally each build is checked against Loader.io to ensure, that code changes don't decrease the framework performance. 

## Key features.

* Intuitiv convention-over-configuration
* Highly scalable using a share-nothing stateless architecture
* Hot-Compiling development mode for high productivity
* Easy to use template engine
* Support for Web Socket
* Support for Server-Sent Events
* Simple and self-explaining form handling and validation
* Flexible authentication features out-of-the-box
* Support for OAuth (with Twitter, Google and Facebook)
* Plain scheduling for recurring tasks
* Easy handling of JSON in- and output
* Build-in asset minification in development mode
* Flexible testing tools
* Simple Deployment and CI-Integration
* i18N Internationalization

## Read.

Checkout Introducing mangoo I/O in 2 minutes to get an overview of the features and methodologies.

The documentation of mangoo I/O is available as HTML.

Also have a look at the current changelog.

## Develop.

Latest stable version is 2.2.0

mangoo I/O is hosted on GitHub and releases are pushed to Maven Central.

## Contribute.

If you want to contribute or check the development progress, head over to the GitHub repository.

If you have questions or a topic you want to discuss, head over to the discussion board or follow the mangoo I/O Twitter account.
