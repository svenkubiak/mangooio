## Version 9.4.0

Unreleased

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/9.3.0...9.4.0)

* Sonar refactorings (svenkubiak)
* Version bumps (svenkubiak)

## Version 9.3.0

Released at 06.03.2025

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/9.2.0...9.3.0)

* Added utility method to retrieve all configured translations / resource bundles (svenkubiak)
* All new and optimized documentation based on MkDocs Material (svenkubiak)
* Version bumps (svenkubiak)
    * snakeyaml 2.3 -&gt; 2.4
    * fury-core 0.9.0 -&gt; 0.10.0
    * jackson 2.18.2 -&gt; 2.18.3
    * mockito-core 5.15.2 -&gt; 5.16.0

## Version 9.2.0

Released at 11.02.2025

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/9.1.0...9.2.0)

* Added parsing of a default value from an arg{} config value (svenkubiak)
* Added cleaner handling of authentication when using mfa (svenkubiak)
* Added .notModified() to Response class (svenkubiak)
* Fixed an issue where the default messages.properties was not loaded (svenkubiak)
* Version bumps (svenkubiak)
    * bouncy-castle 1.79 -&gt; 1.80
    * caffeine 3.1.8 -&gt; 3.2.0
    * mongo-driver-sync 5.3.0 -&gt; 5.3.1
    * commons-codec 1.17.2 -&gt; 1.18.0

## Version 9.1.0

Released at 13.01.2025

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/9.0.0...9.1.0)

* Added handling of boolean request and URL parameter (svenkubiak)
* Fixed bug in CodecUtils that generated the same uuid (svenkubiak)
* Fixed bug that did not send the correct cookie when rememberMe was 'true' (svenkubiak)
* Fixed bug that send the authentication cookie on every request after valid authentication (svenkubiak)
* Fixed potential NPE in LocaleHandler (svenkubiak)
* Version bumps (svenkubiak)
    * log4j 2.24.2 -&gt; 2.24.3
    * freemarker-java8 3.0.0 -&gt; 3.0.1
    * freemarker 2.3.33 -&gt; 2.3.34
    * junit 5.11.3 -&gt; 5.11.4
    * guava 33.3.1-jre -&gt; 33.4.0-jre
    * commons-codec 1.17.1 -&gt; 1.17.2
    * re2j 1.7 -&gt; 1.8
    * mongo-driver-sync 5.2.1 -&gt; 5.3.0

## Version 9.0.0

Released at 10.12.2024

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/8.11.0...9.0.0)

* [#601](https://github.com/svenkubiak/mangooio/issues/601) Removed Basic HTTP authentication (svenkubiak)
* [#591](https://github.com/svenkubiak/mangooio/issues/591) Refactored Response class (svenkubiak)
* [#588](https://github.com/svenkubiak/mangooio/issues/588) Removed @admin/health endpoint (svenkubiak)
* [#576](https://github.com/svenkubiak/mangooio/issues/588) Removed all deprecated classes and methods (svenkubiak)
* Switched from props based configuration yaml based configuration (svenkubiak)
* Added copy-to-clipboard function to @admin dashboard (svenkubiak)
* Upgraded SHA hashing to SHA3-512 (svenkubiak)
* Increase iterations, memory, and parallelism of Argon2 hashing (svenkubiak)
* Switched to new Paseto library and upgraded to Paseto v4 (svenkubiak)
* Removed searchbar and table sorter from @admin dashboard (svenkubiak)
* Added new PasetoFilter and APIFilter to handle easy API authentication (svenkubiak)
* Version bumps (svenkubiak)
    * mongodb-driver-sync 5.2.0 -&gt; 5.2.1
    * fury-core 0.8.0 -&gt; 0.9.0
    * classgraph 4.8.177 -&gt; 4.8.179
    * greenmail 2.1.0 -&gt; 2.1.2
    * commons-io 2.17.0 -&gt; 2.18.0
    * log4j 2.24.1 -&gt; 2.24.2

## Version 8.11.0

Released at 04.11.2024

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/8.10.0...8.11.0)

* Added option to make an index unique via @Indexed annotation (svenkubiak)
* Version bumps (svenkubiak)
    * undertow-core 2.3.17.Final -&gt; 2.3.18.Final
    * junit 5.11.1 -&gt; 5.11.3
    * fury-core 0.7.1 -&gt; 0.8.0
    * jackson-databind 2.18.0 -&gt; 2.18.1
    * cactoos 0.56.0 -&gt; 0.56.1
    * bouncy-castle 1.78.1 -&gt; 1.79

## Version 8.10.0

Released at 04.10.2024

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/8.9.0...8.10.0)

* Fixed issue with multiple ServerSentEvent connections (svenkubiak)
* Removed external (web) dependency for generating 2FA QR code in @admin (svenkubiak)
* Version bumps (svenkubiak)
    * log4j 2.23.1 -&gt; 2.24.0
    * fury-core 0.7.0 -&gt; 0.7.1
    * classgraph 4.8.175 -&gt; 4.8.177
    * commons-io 2.16.1 -&gt; 2.17.0
    * guava 33.3.0-jre -&gt; 33.3.1-jre
    * mongodb-driver-sync 5.1.4 -&gt; 5.2.0
    * junit 5.11.0 -&gt; 5.11.1

## Version 8.9.0

Released at 02.09.2024

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/8.8.0...8.9.0)

* Added sanity checks on application startup for multiple mappings with the same URL (svenkubiak)
* Added new JSON error response method (svenkubiak)
* Added additional response methods for default templates without rendering (svenkubiak)
* Added an option to pass an auth origin parameter to the default /login redirect (svenkubiak)
* Added a method for dropping all indexes of all collections in the connected database (svenkubiak)
* Version bumps (svenkubiak)
    * fury-core 0.6.0 -&gt; 0.7.0
    * guava 33.2.1-jre -&gt; 33.3.0-jre
    * commons-compress 1.26.2 -&gt; 1.27.0
    * commons-lang 3.15.0 -&gt; 3.17.0
    * mongodb-driver-sync 5.1.2 -&gt; 5.1.3
    * awaitility 4.2.1 -&gt; 4.2.2
    * commons-compress 1.27.0 -&gt; 1.27.1
    * classgraph 4.8.174 -&gt; 4.8.175
    * undertow-core 2.3.15.Final -&gt; 2.3.17.Final
    * freemarker-java8 2.1.0 -&gt; 3.0.0

## Version 8.8.0

Released at 29.07.2024

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/8.7.0...8.8.0)

* Added utils method to generate a UUIDv5 in CodecUtils (svenkubiak)
* Version bumps (svenkubiak)
    * pretty-time 5.0.8.Final -&gt; 5.0.9.Final
    * jackson 2.17.1 -&gt; 2.17.2
    * commons-codec 1.17.0 -&gt; 1.17.1
    * undertow-core 2.3.14.Final -&gt; 2.3.15.Final
    * commons-lang3 3.14.0 -&gt; 3.15.0
    * fury-core 0.4.1 -&gt; 0.6.0

## Version 8.7.0

Released at 21.06.2024

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/8.6.0...8.7.0)

* Updated design of default error pages (svenkubiak)
* Version bumps (svenkubiak)
    * commons-validator 1.8.0 -&gt; 1.9.0
    * guava 33.2.0-jre -&gt; 33.2.1-jre
    * freemarker 2.3.32 -&gt; 2.3.33
    * mongodb-driver-sync 5.1.0 -&gt; 5.1.1
    * classgraph 4.8.173 -&gt; 4.8.174
    * undertow-core 2.3.13.Final -&gt; 2.3.14.Final

## Version 8.6.0

Released at 27.05.2024

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/8.5.0...8.6.0)

* Added scheduler overview page in @admin area (svenkubiak)
* Removed logger overview page in @admin area (svenkubiak)
* Refactored Response Entity for a more convenience usage (svenkubiak)
* Sonar refactorings (svenkubiak)
* Version bumps (svenkubiak)
    * mongodb-driver-sync 5.0.1 -&gt; 5.1.0
    * guava-jre 33.1.0-jre -&gt; 33.2.0-jre
    * jackson 2.17.0 -&gt; 2.17.1
    * pretty-time 5.0.7.Final -&gt; 5.0.8.Final
    * mockito-core 5.11.0 -&gt; 5.12.0
    * common-logging 1.3.1 -&gt; 1.3.2

## Version 8.5.0

Released at 28.04.2024

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/8.4.0...8.5.0)

* Reduced start-up time by around 50% (svenkubiak)
* Added additional methods for database quering (svenkubiak)
* Added additional failsafe when task is scheduled (svenkubiak)
* Added @Indexed annotation for field indexing (svenkubiak)
* Updated admin dashboard to latest version of bulma (svenkubiak)
* Fix bug that set the wrong cache value when using expiry (svenkubiak)
* Sonar refactorings (svenkubiak)
* Version bumps (svenkubiak)
    * mongodb-driver-sync 5.0.0 -&gt; 5.0.1
    * classgraph 4.8.168 -&gt; 4.8.172
    * bouncycastle 1.77 -&gt; 1.78.1
    * undertow-core 2.3.12.Final -&gt; 2.3.13.Final

## Version 8.4.0

Released at 02.04.2024

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/8.3.0...8.4.0)

* Sonar refactorings (svenkubiak)
* Added config option to disable persistence (svenkubiak)
* Version bumps (svenkubiak)
    * mongodb-driver-sync 4.11.1 -&gt; 5.0.0
    * commons-compress 1.26.0 -&gt; 1.26.1
    * log4j 2.23.0 -&gt; 2.23.1
    * guava 33.0.0-jre -&gt; 33.1.0-jre
    * commons-io 2.15.1 -&gt; 2.16.0

## Version 8.3.0

Released at 25.02.2024

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/8.2.0...8.3.0)

* Sonar refactorings (svenkubiak)
* Version bumps (svenkubiak)
    * mockito-core 5.9.0 -&gt; 5.10.0
    * junit 5.10.1 -&gt; 5.10.2
    * commons-codec 1.16.0 -&gt; 1.16.1
    * undertow-core 2.3.10.Final -&gt; 2.3.12.Final
    * log4j 2.22.1 -&gt; 2.23.0

## Version 8.2.0

Released at 22.01.2024

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/8.1.0...8.2.0)

* Sonar refactorings (svenkubiak)
* Replaced Guava event bus with new event bus based on reactive streams (svenkubiak)
* Version bumps (svenkubiak)
    * log4j 2.22.0 -&gt; 2.22.1
    * jackson 2.16.0 -&gt; 2.16.1
    * mockito-core 5.8.0 -&gt; 5.9.0

## Version 8.1.0

Released at 19.12.2023

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/8.0.0...8.1.0)

* Added additional cache statistics to /@admin (svenkubiak)
* Added additional cache retrieval method with fallback option (svenkubiak)
* Switched scheduler execution to virtual threads (svenkubiak)
* Switched cache handling from Guava to Caffeine (svenkubiak)
* Version bumps (svenkubiak)
    * commons-io 2.15.0 -&gt; 2.16.0
    * fury-core 0.3.1 -&gt; 0.4.1
    * mockito-core 5.7.0 -&gt; 5.8.0
    * commons-validator 1.7 -&gt; 1.8
    * guava 32.1.3-jre -&gt; 33.0.0-jre

## Version 8.0.0

Released at 28.11.2023

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/7.19.0...8.0.0)

* Removed Morphia in favour of a direct MongoDB integration (svenkubiak)
* Removed all deprecated methods and classes (svenkubiak)
* Updated to Java 21
* Sonar refactorings (svenkubiak)
* Version bumps (svenkubiak)
    * commons-lang3 3.13.0 -&gt; 3.14.0
    * fury-core 0.3.0 -&gt; 0.3.1