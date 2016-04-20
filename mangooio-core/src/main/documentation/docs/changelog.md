## Version 2.7.0

**Released at xx.xx.xxxx**

* Version bumps (svenkubiak)
	* scribejava-apis 2.5.2 -> 2.5.3
	* scribejava-core 2.5.2 -> 2.5.3
	* mockito 2.0.46-Beta -> 2.0.49-Beta
	* quartz 2.2.2 -> 2.2.3
	
## Version 2.6.1

**Released at 17.04.2016**

* [#163](https://github.com/svenkubiak/mangooio/issues/163) Fixed dependency cycle bug when using mangoo-test-utilities (svenkubiak)

## Version 2.6.0

**Released at 15.04.2016**

* Added option to encrypt configuration values (svenkubiak)
* Switched to default Java 8 Base64 Encoder/Decoder (svenkubiak)
* Sonar refactorings  (svenkubiak)
* Version bumps (svenkubiak)
	* undertow-core 1.3.19.Final -> 1.3.21.Final
	* freemarker 2.3.23 -> 2.3.24-incubating
	* pebble 2.2.0 -> 2.2.1
	* hazelcast-client 3.6.1 -> 3.6.2
	* boon-json 0.5.6.RELEASE -> 0.5.7
	* fluentlenium-core 0.10.9 -> 0.13.0
	* scripe-apis 2.4.0 -> 2.5.2
	* scripe-core 2.4.0 -> 2.5.2
	* zt-exec 1.8 -> 1.9
	* dsituils 2.3.0 -> 2.3.2
	* mockito 2.0.44-Beta -> 2.0.46-Beta

## Version 2.5.1

**Released at 20.03.2016**

* Documentation fix

## Version 2.5.0

**Released at 20.03.2016**

* [#160](https://github.com/svenkubiak/mangooio/issues/159) Fixed ClassCastException in OAuthLoginFilter (svenkubiak)
* [#159](https://github.com/svenkubiak/mangooio/issues/159) Added authenticity token and form for Pebble (svenkubiak)
* [#154](https://github.com/svenkubiak/mangooio/issues/154) Added new exception handling for public API (svenkubiak)
* Sonar refactorings (svenkubiak)
* Version bumps (svenkubiak)
	* undertow-core 1.3.15.Final -> 1.3.19.Final
	* mockito 2.0.42-Beta -> 2.0.44-Beta
	* pebble 2.1.0 -> 2.2.0
	* scripe-apis 2.2.2 -> 2.4.0
	* scripe-core 2.2.2 -> 2.4.0
	* hazlecast-client 3.6 -> 3.6.1
    * json-path 2.1.0 -> 2.2.0
    * fleunt-hc 4.5.1 -> 4.5.2

## Version 2.4.1

**Released at 23.02.2016**

* [#157](https://github.com/svenkubiak/mangooio/issues/157) Fixed bug that prevented authentication wrong showing an error (svenkubiak)
* Added additional methods for mapping collections in JSONUtils (svenkubiak)

## Version 2.4.0

**Released at 20.02.2016**

* [#154](https://github.com/svenkubiak/mangooio/issues/154) Added limit for max post size (svenkubiak)
* [#146](https://github.com/svenkubiak/mangooio/issues/146) Updated to 2.x of Scribe OAuth library (svenkubiak)
* [#145](https://github.com/svenkubiak/mangooio/issues/145) Added provider for TemplateEngine and second Template Engine Pebble (svenkubiak)
* [#139](https://github.com/svenkubiak/mangooio/issues/139) Added utility methods for Two-Factor-Authentication (svenkubiak, MrDunne)
* [#134](https://github.com/svenkubiak/mangooio/issues/134) Change documentation from ascidoc to MkDocs (svenkubiak)
* [#138](https://github.com/svenkubiak/mangooio/issues/138) Added provider for cache (svenkubiak)
* [#133](https://github.com/svenkubiak/mangooio/issues/137) SSE and WSS informations are now stored in cache (svenkubiak)
* Sonar refactorings (svenkubiak)
* Updated documentation (svenkubiak)
* Version bumps (svenkubiak)
	* undertow-core 1.3.15.Final -> 1.3.18.Final
	* mockito 2.0.40-Beta -> 2.0.42-Beta
	* dsiutils 2.2.6 -> 2.3.0
	* jersey-media-sse 2.22.1 -> 2.22.2
	* snakeyaml 1.16 -> 1.17
	* jetty-websocket 8.1.18.v20150929 -> 8.1.19.v20160209

## Version 2.3.1

**Released at 27.01.2016**

* Fxed bug when config injector was initialized in application start (svenkubiak)

## Version 2.3.0

**Released at 26.01.2016**

* [#126](https://github.com/svenkubiak/mangooio/issues/126) Added i18n cookie to LocaleHandler (MrDunne)
* [#118](https://github.com/svenkubiak/mangooio/issues/118) Refactored request chaining for custom handlers (svenkubiak, MarkVink)
* [#122](https://github.com/svenkubiak/mangooio/issues/122) Fixed inconsistency configuration controller package (MrDunne)
* [#109](https://github.com/svenkubiak/mangooio/issues/109) Refactored Config (svenkubiak)
* [#120](https://github.com/svenkubiak/mangooio/issues/120) Added getAcceptLangauge to Request (MrDunne)
* [#121](https://github.com/svenkubiak/mangooio/issues/121) Updated readme (MrDunne)
* Added an option to add a custom cookie in test utilities request method (svenkubiak)
* Removed Mangoo.TEST enum in test utilities (svenkubiak)
* Removed cache provider (svenkubiak)
* Refactored LocaleHandler (svenkubiak)
* Sonar refactorings (svenkubiak)
* Updated documentation (svenkubiak)
* Version bumps (svenkubiak)
	* undertow-core 1.3.12.Final -> 1.3.15.Final
	* mockito 2.0.36-Beta -> 2.0.40-Beta
	* dsiutils 2.2.5 -> 2.2.6

## Version 2.2.0

**Released at 10.01.2016**

* [#110](https://github.com/svenkubiak/mangooio/issues/110) Added X-Response-Header (svenkubiak)
* [#108](https://github.com/svenkubiak/mangooio/issues/108) Refactored RequestHandler (svenkubiak)
* [#98](https://github.com/svenkubiak/mangooio/issues/98) Improved RequestHandler (svenkubiak)
* Added LocalDateTime timestamp of application start (svenkubiak)
* Added new Benchmark project (svenkubiak)
* Improved application startup time (svenkubiak)
* Updated documentation (svenkubiak)
* Version bumps (svenkubiak)
	* undertow-core 1.3.10.Final -> 1.3.12.Final
	* bcprov-jdk15on 1.53 -> 1.54
	* mockito 2.0.33-Beta -> 2.0.36-Beta

## Version 2.1.0

**Released at 21.12.2015**

* [#103](https://github.com/svenkubiak/mangooio/issues/103) Fixed bug when cookie encryption was enabled (svenkubiak)
* [#102](https://github.com/svenkubiak/mangooio/issues/102) Made Cache eviction configurable (svenkubiak)
* [#101](https://github.com/svenkubiak/mangooio/issues/101) Fixed keys in archetype (MarkVink)
* [#97](https://github.com/svenkubiak/mangooio/issues/97) Fixed incorrect Form documentation (svenkubiak, MarkVink)
* [#95](https://github.com/svenkubiak/mangooio/issues/95) Added new @memory administrative URL (svenkubiak)
* [#94](https://github.com/svenkubiak/mangooio/issues/94) Added new @system administrative URL (svenkubiak)
* [#93](https://github.com/svenkubiak/mangooio/issues/93) Improved DispatcherHandler/RequestHandler (svenkubiak)
* Updated documentation (svenkubiak, MarkVink)
* Version bumps (svenkubiak)
	* log4j 2.4.1 -> 2.5
	* mockito 2.0.31-Beta -> 2.0.33-Beta

## Version 2.0.2

**Released at 17.12.2015**

* [#91](https://github.com/svenkubiak/mangooio/issues/91) Added numeric validation (svenkubiak)
* [#90](https://github.com/svenkubiak/mangooio/issues/90) Added JSONUtils for custom serialization with annotations (svenkubiak)
* [#89](https://github.com/svenkubiak/mangooio/issues/89) Added Internal Server Error to Response class (svenkubiak)
* Sonar refactorings (svenkubiak)
* Updated documentation (svenkubiak)

## Version 2.0.1

**Released at 13.12.2015**

* Fixed bug in documentation asciidoc file (svenkubiak)

## Version 2.0.0

**Released at 13.12.2015**

* [#79](https://github.com/svenkubiak/mangooio/issues/79) Add Server-Sent Events handler (svenkubiak)
* [#78](https://github.com/svenkubiak/mangooio/issues/78) Switched to plugin and dependency management in root pom.xml (svenkubiak)
* [#69](https://github.com/svenkubiak/mangooio/issues/69) Restructured integration tests (svenkubiak)
* [#76](https://github.com/svenkubiak/mangooio/issues/76) Improved handling of values in forms (svenkubiak)
* [#77](https://github.com/svenkubiak/mangooio/issues/77) Added option for adding custom cookies (svenkubiak)
* [#75](https://github.com/svenkubiak/mangooio/issues/75) Fixed bug that did not handle remember in authentication correctly (svenkubiak)
* [#74](https://github.com/svenkubiak/mangooio/issues/74) Refactored test utilities (svenkubiak)
* [#64](https://github.com/svenkubiak/mangooio/issues/64) Move to log4j2 for logging (svenkubiak)
* [#53](https://github.com/svenkubiak/mangooio/issues/53) Extracted Mailer to own extension (svenkubiak)
* [#66](https://github.com/svenkubiak/mangooio/issues/66) Removed all redundant legacy code  (svenkubiak)
* [#45](https://github.com/svenkubiak/mangooio/issues/45) Removed all @deprecated methods and classes (svenkubiak)
* Refactored routing mechanism to routes.yaml file (svenkubiak)
* Code cleanup, refactorings and more JavaDoc (svenkubiak)
* Updated Documentation (svenkubiak)
* Version bumps (svenkubiak)
	* undertow-core 1.3.3.Final -> 1.3.10.Final
	* guava 18.0 -> 19.0
	* fluent-hc 4.4.1 -> 4.5.1
	* fluentlenium-core 0.10.3 -> 0.10.8
	* json-path 2.0.0 -> 2.1.0

## Version 1.3.2

**Released at 19.11.2015**

* Fixed NPE in maven plugin (svenkubiak)

## Version 1.3.1

**Released at 25.10.2015**

* Fixed bug that displayed the wrong previous firing time on @scheduler page (svenkubiak)
* Fixed bug in table that displayed schedule job on @scheduler page (svenkubiak)

## Version 1.3.0

**Released at 22.10.2015**

* [#73](https://github.com/svenkubiak/mangooio/issues/73) Added Basic HTTP authentication for administrative URLs (svenkubiak)
* [#72](https://github.com/svenkubiak/mangooio/issues/72) Added @scheduler administrative URL (svenkubiak)
* [#71](https://github.com/svenkubiak/mangooio/issues/71) Added autostart option to scheduler (svenkubiak)
* Code cleanup, refactorings and more javadoc (svenkubiak)
* Updated Documentation (svenkubiak)
* Version bumps (svenkubiak)
	* undertow-core 1.3.0.Final -> 1.3.3.Final
	* bcprov-jdk15on 1.52 -> 1.53

## Version 1.2.0

**Released at 17.10.2015**

* [#33](https://github.com/svenkubiak/mangooio/issues/33) Added Cookie versioning (svenkubiak)
* [#57](https://github.com/svenkubiak/mangooio/issues/57) Added OAuth to authentication (svenkubiak)
* [#61](https://github.com/svenkubiak/mangooio/issues/61) Move Body to Request (svenkubiak)
* [#58](https://github.com/svenkubiak/mangooio/issues/58) Added @metrics administrative URL (svenkubiak)
* Added PUT and DELETE to MangooRequest in test utilities (svenkuibiak)
* Fixed typo in MangooRequestFilter interface (svenkubiak)
* Updated Documentation (svenkubiak)
* Version bumps (svenkubiak)
	* undertow-core 1.2.12.Final -> 1.3.0.Final
	* quartz 2.2.1 -> 2.2.2
	* jetty-websocket 8.1.17.v20150415 -> 8.1.18.v20150929

## Version 1.1.4

**Released at 04.10.2015**

* Fixed bug when sending binary content (svenkubiak)
* ExceptionHandler now preserves root cause when exception occurs (svenkubiak)

## Version 1.1.3

**Released at 21.09.2015**

* [#62](https://github.com/svenkubiak/mangooio/issues/62) Set default encoding (UTF-8) to form parsing (svenkubiak)
* [#59](https://github.com/svenkubiak/mangooio/issues/59) Fixed a NPE when template exception was caught (svenkubiak)

## Version 1.1.2

**Released at 17.09.2015**

* Fixed a NPE when a Request object and JSON was required in a controller method (svenkubiak)
* Refactored dev mode exception template (svenkubiak)
* Fixed bug that did not show exception in frontend in dev mode (svenkubiak)
* Fixed typo in ContentType enum (svenkubiak)

## Version 1.1.1

**Released at 15.09.2015**

* Updated Documentation (svenkubiak)

## Version 1.1.0

**Released at 14.09.2015**

* Refactored RequestHandler (svenkubiak)
* Refactored EhCache to Guava Cache (svenkubiak)
* Added ETag support for dynamic content (svenkubiak)
* Added a method for adding a complete content map to a template (svenkubiak)
* Added administrative URLs @health, @routes, @cache and @config (svenkubiak)
* Updated Documentation (svenkubiak)
* [#52](https://github.com/svenkubiak/mangooio/issues/52) Refactored filters (svenkubiak)
* [#40](https://github.com/svenkubiak/mangooio/issues/40) Added handling of multiple parameters in controller method (svenkubiak)
* [#39](https://github.com/svenkubiak/mangooio/issues/39) Added methods for parameter validation (svenkubiak)
* [#37](https://github.com/svenkubiak/mangooio/issues/37) Added option to set the secure flag for session and auth cookie (svenkubiak)
* [#35](https://github.com/svenkubiak/mangooio/issues/35) Added method for regular expression to validation (svenkubiak)
* [#34](https://github.com/svenkubiak/mangooio/issues/34) Added LocalDate and LocalDateTime as request parameter (svenkubiak)
* [#36](https://github.com/svenkubiak/mangooio/issues/36) Validation now works for numeric values (svenkubiak)
* Version bumps (svenkubiak)
	* doctester-core 1.1.6 -> 1.1.8
	* snakeyaml 1.15 -> 1.16
	* junit-toolbox 2.1 -> 2.2
	* undertow-core 1.2.9.Final -> 1.2.12.Final

## Version 1.0.1

**Released at 05.08.2015**

* Fixed typo in archetype that prevented archetype from building (svenkubiak)

## Version 1.0.0

**Released at 31.07.2015**

* Updated documentation (svenkubiak)

## Version 1.0.0-RC5

**Released at 23.07.2015**

* Fixed bug, that prevented dev mode from starting (svenkubiak)
* Updated documentation (svenkubiak)

## Version 1.0.0-RC4

**Released at 23.07.2015**

* Refactored packaging from mangoo.io to io.mangoo (svenkubiak)
* Updated documentation (svenkubiak)
* Sonar refactorings (svenkubiak)
* Added more JavaDoc (svenkubiak)

## Version 1.0.0-RC3

**Released at 10.07.2015**

* Added dispatcher handler and refactored invoking of requesthandler (svenkubiak)
* Added X-XSS-Protection, X-Content-Type-Options and X-Frame-Options headers (svenkubiak)

## Version 1.0.0-RC2

**Released at 07.07.2015**#

* Added some more javadoc (svenkubiak)
* Template engine does not throw generic exception anymore, throws specific ones instead (svenkubiak)
* Version bumps (svenkubiak)
	* undertow-core 1.2.7.Final -> 1.2.8.Final
	* freemarker 2.3.22 -> 2.3.23

## Version 1.0.0-RC1

**Released at 03.07.2015**

* Sonar Refactorings (svenkubiak)
* [#32](https://github.com/svenkubiak/mangooio/issues/32) Switched to Java8 DateTime API (svenkubiak)

## Version 1.0.0-Beta5

**Released at 01.07.2015**

* [#29](https://github.com/svenkubiak/mangooio/issues/29) Fixed bug in authentication and session cookie generation (svenkubiak)
* [#28](https://github.com/svenkubiak/mangooio/issues/28) Changed default expire of authentication to one hour (svenkubiak)
* [#26](https://github.com/svenkubiak/mangooio/issues/26) Added option to pass an external configuration path (svenkubiak)
* [#23](https://github.com/svenkubiak/mangooio/issues/23) Added form unit tests and more bindings tests (svenkubiak)
* [#20](https://github.com/svenkubiak/mangooio/issues/20) Added convinent methods for retrieving default config values (svenkubiak)
* [#24](https://github.com/svenkubiak/mangooio/issues/24) Switched from properties to yaml configuration (svenkubiak)
* [#17](https://github.com/svenkubiak/mangooio/issues/17) Added preparsing of routes (svenkubiak)

## Version 1.0.0-Beta4

**Released at 29.07.2015**

* [#19](https://github.com/svenkubiak/mangooio/issues/19) Fixed MangooFluent and refactored testing utilities (svenkubiak)
* [#18](https://github.com/svenkubiak/mangooio/issues/18) Added default validation messages for form handling (svenkubiak)
* Better exception handling when in dev mode (svenkubiak)
* Fixed bug that prevented flash from bein passed to next request (svenkubiak)
* Optimized the shade plugin to create smaller JAR files (svenkubiak)

## Version 1.0.0-Beta3

**Released at 26.07.2015**

* Fixed bug that did not set the correct cookie for authentication (svenkubiak)
* Fixed bug in authentication which caused an error when authentication was injected (svenkubiak)
* Fixed bug in cache that throw an NPE when getType was called and value was not in cache (svenkubiak)
* Added method to add additional content to template with a filter (svenkubiak)
* Added missing interfaces methods to maven archetype (svenkubiak)

## Version 1.0.0-Beta2

**Released at 23.07.2015**

* [#9](https://github.com/svenkubiak/mangooio/issues/9) Refactored dev mode exception page (svenkubiak)
* [#15](https://github.com/svenkubiak/mangooio/issues/15) Added version tag to documentation (PDF and HTML) (svenkubiak)
* [#16](https://github.com/svenkubiak/mangooio/issues/16) Fixed bug that result in wrong compilation when in dev mode (svenkubiak)
* Cache is not autostarted anymore (svenkubiak)
* Fixed bug that throw NumberFormatException when passing an empty request parameter (svenkubiak)

## Version 1.0.0-Beta1

**Released at 17.07.2015**

* Added server "Undertow" token to response (svenkubiak)
* Added new lifecycle hook "applicationInitialized" (svenkubiak)
* Fixed bug that checked mode for testing incorrectly (svenkubiak)
* [#10](https://github.com/svenkubiak/mangooio/issues/10) Added option to add additional headers to response (svenkubiak)
* [#12](https://github.com/svenkubiak/mangooio/issues/12) Added option to autocast cache values (svenkubiak)
* [#11](https://github.com/svenkubiak/mangooio/issues/11) Application name and secret is now generated from user input (svenkubiak)
* [#13](https://github.com/svenkubiak/mangooio/issues/13) Archetype now takes root project version on generation (svenkubiak)
* Updated documentation (svenkubiak)
* Sonar refactorings (svenkubiak)

## Version 1.0.0-Alpha3

**Released at 15.06.2015**

* [#2](https://github.com/svenkubiak/mangooio/issues/2) Fixed flash passing between requests (svenkubiak)
* [#1](https://github.com/svenkubiak/mangooio/issues/1) Fixed failing parameter tests (svenkubiak)
* [#6](https://github.com/svenkubiak/mangooio/issues/6) Version bump (svenkubiak)
* [#5](https://github.com/svenkubiak/mangooio/issues/5) Added option for sending binary content (svenkubiak)
* Added HtmlUnitDriver to MangooUnit (svenkubiak)
* Removed changelog from documentation and added to seperate file (svenkubiak)
* Fixed bug that did not pass request parameter when project was generated from archetype (svenkubiak)

## Version 1.0.0-Alpha2

**Released at 11.06.2015**

* Added asciidoc documentation to mangooio-core (svenkubiak)

## Version 1.0.0-Alpha1

**Released at 11.06.2015**

* Initial release (svenkubiak)