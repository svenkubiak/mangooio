## Version 11.1.0

* The HTTPS connector offers HTTP/2. The plain HTTP connector stays HTTP/1.1 (svenkubiak)
* A JSON POST, PUT or PATCH of at most 16 KiB stays on the I/O thread. Forms, larger bodies, bodies without Content-Length and withNonBlocking() still run on a worker. The form parser is created once and only for form requests (svenkubiak)
* Reused the AES cipher and HMAC per request thread when creating and reading cookies (svenkubiak)
* Opened the authentication cookie only for routes that require authentication, take Authentication or Request, or run a request filter (svenkubiak)
* Created the CSRF token when it is first read instead of on every request without a session cookie (svenkubiak)
* Rendered HTML into a small buffer and sent those UTF-8 bytes without a second string copy (svenkubiak)
* Read a JSON request body once as bytes and sent the JSON response from those bytes (svenkubiak)
* Version bumps (svenkubiak)
  * commons-lang3 3.21.0 -&gt; 3.22.0
  * bouncy-castle 1.86 -&gt; 1.87
  
## Version 11.0.0

Released at 09.11.2026

* **Behaviour change** smtp.protocol smtptls requires STARTTLS, smtp is cleartext and refused in prod, and an SMTP connection or read that exceeds smtp.connection.timeout or smtp.read.timeout (10 seconds and 30 seconds by default) is aborted and logged (svenkubiak)
* **Behaviour change** Graceful shutdown: readiness goes down, SSE and WebSocket connections are closed, open requests and running @Run tasks get application.shutdown.timeout, then applicationStopped() is called (svenkubiak)
* **Behaviour change** A binding in app.Module replaces the core binding of the same type instead of failing (svenkubiak)
* **Behaviour change** Removed the vault and vault{}, secrets come from env{}, arg{} or file{} (svenkubiak)
* **Behaviour change** An env{}, arg{} or file{} without value and fallback fails the startup (svenkubiak)
* **Behaviour change** Framework secrets must not be literal in the config.yaml of the classpath in prod mode (svenkubiak)
* **Behaviour change** The HTTPS connector reads its certificate from connector.https.keystore, no self-signed certificate is generated (svenkubiak)
* **Behaviour change** The HTTPS connector accepts TLS 1.3 only, TLS 1.2 is refused (svenkubiak)
* **Behaviour change** Failed login budgets, admin pre-authentication and token revocations are kept in MongoDB and shared by all instances when persistence is enabled; MongoDB 4.2 is required (svenkubiak)
* **Behaviour change** isValidSecondFactor and the admin login accept each TOTP code once (svenkubiak)
* **Behaviour change** Removed AuthenticationLock and CacheName.AUTH (svenkubiak)
* **Behaviour change** Cache counters are atomic and return long: increment, decrement, getCounter, resetCounter replace getAndIncrementCounter and getAndDecrementCounter (svenkubiak)
* **Behaviour change** OpenTelemetry uses application.name as service.name instead of io.mangoo (svenkubiak)
* **Behaviour change** The session cookie ends with the browser session unless session.cookie.expires is true (svenkubiak)
* **Behaviour change** Removed the unused application.controller (svenkubiak)
* Removed all deprecated methods (svenkubiak)
* Removed asset minification (svenkubiak)
* Migrated from Jackson 2 to Jackson 3 (svenkubiak)
* Grouped the features of the admin dashboard into security, persistence, connectivity and operations with their values, risky settings are marked in prod mode (svenkubiak)
* Changed Admin-Password handling (svenkubiak)
* Redesigned the admin dashboard with Blades CSS instead of Bulma, with dark mode and mobile layout (svenkubiak)
* Added support for MongoDB connection strings - replica sets, sharded clusters, SRV (svenkubiak)
* Added responses by status code, Java version and heap, cache clearing and metrics reset to the admin dashboard; with persistence cache clearing and metrics reset run on all instances (svenkubiak)
* Added a routes page, open connections, event queues, failed events and active features to the admin dashboard (svenkubiak)
* Added application.name to the brand and page title of the admin dashboard (svenkubiak)
* Added mvn mangooio:hash to print an Argon2id hash for application.admin.password from the project's config.yaml without starting the application (svenkubiak)
* Added CommonUtils.hashArgon2(cleartext, salt, settings) to hash with explicit Argon2 settings without a started application (svenkubiak)
* Added EventBus.getFailedEvents, EventBus.getSubscribers, Application.getServerSentEventConnections and Application.getWebSocketConnections (svenkubiak)
* Added a list of all instances with heartbeat, version and the requests and errors of the last five minutes to the admin dashboard, shared through mangooio_instances with persistence (svenkubiak)
* Added live updates of the admin dashboard every five seconds through the server sent event stream /@admin/stream, which requires the admin cookie (svenkubiak)
* Added the failed requests per route and the last 50 server errors to the admin dashboard (svenkubiak)
* Added the request metrics of the last five minutes with Metrics.getWindow (svenkubiak)
* Added health endpoints /@health/live and /@health/ready (svenkubiak)
* Added connector.proxy to take client address and scheme from X-Forwarded-* headers (svenkubiak)
* Added an instance id to the startup log, the admin dashboard and the OpenTelemetry resource (svenkubiak)
* Added a startup warning for embedded MongoDB in prod mode (svenkubiak)
* Added file{} to read secrets from mounted files in /run/secrets (svenkubiak)
* Added generated dev and test secrets to the archetype (svenkubiak)
* Added AttemptStore, OneTimeStore and TotpUtils.verifyTotp(identifier, secret, totp) (svenkubiak)
* Added MessageBroker, server sent events reach the clients of every instance when persistence is enabled (svenkubiak)
* Added the OpenTelemetry metric http.server.request.duration when otlp.enable is true (svenkubiak)
* Added the instance id as system property mangooio.instance.id for log patterns (svenkubiak)
* Added env{?}, arg{?} and file{?} for optional values, which leave the key unset if the source is empty (svenkubiak)
* Added persistence.mongo.schema: validate to check collections and indexes at startup instead of creating them (svenkubiak)
* Fixed a revoked authentication cookie being accepted again within the JWT clock skew after its expiry (svenkubiak)
* Fixed MongoDB clients not being closed and embedded MongoDB being stopped before Undertow on shutdown (svenkubiak)
* Fixed the admin login with a second factor failing when the two steps reached different instances (svenkubiak)
* Fixed a hashing overload failing the admin login with an error instead of refusing it without counting the attempt (svenkubiak)
* Fixed the shutdown losing its log output, as Log4j stopped in its own shutdown hook (svenkubiak)
* Fixed concurrent first calls of DatastoreProvider.getDatastore for a prefix creating a second MongoClient that was never closed (svenkubiak)
* Fixed an empty secret such as smtp.password: "" failing the startup in prod as a literal secret (svenkubiak)
* Fixed server sent event connections not registered in the ServerSentEventManager holding the shutdown until application.shutdown.timeout (svenkubiak)
* Fixed Crypto.encrypt and Crypto.decrypt with a public or private key not rejecting a null key (svenkubiak)
* Fixed Router.reset not clearing the registered URLs (svenkubiak)
* Fixed TotpUtils.getOtpAuthURL not encoding name and issuer, which broke the URL for values with spaces or & (svenkubiak)
* Fixed JwtUtils.parseJwt failing with a NullPointerException instead of a MangooJwtException when exp, iat or nbf is missing (svenkubiak)
* Fixed MangooUtils.flattenMapHelper not rejecting a null target map (svenkubiak)
* Fixed Mail.from(name, address) reporting a missing name as missing address and vice versa (svenkubiak)
* Fixed Mail.textMessage and Mail.htmlMessage with a template not rejecting null content with a clear message (svenkubiak)
* Fixed TemplateContext writing into the content map of the caller, which failed for immutable maps such as Map.of (svenkubiak)
* Fixed Authentication.login not writing a cookie while the browser still sent a stale or revoked one (svenkubiak)
* Fixed the admin security page proposing a TOTP secret that is not Base32 (svenkubiak)
* Fixed expectTrue, expectFalse, expectNull and expectNotNull having no default error message (svenkubiak)
* Fixed a missing application.secret failing the startup with a NullPointerException instead of an error message (svenkubiak)
* Fixed the instance id system property being application.instance.id instead of mangooio.instance.id (svenkubiak)
* Fixed TestRequest.withBasicAuthentication not sending the credentials (svenkubiak)
* Fixed the archetype importing Datastore from its old package and setting authentication.redirect instead of authentication.redirect.login (svenkubiak)
* Fixed the MessageBroker preparing its collection again after a stop and logging a failed instance removal on shutdown as error (svenkubiak)
* Fixed an unreadable or expired session cookie never being replaced, which failed every CSRF check until the cookie was deleted (svenkubiak)

## Version 10.16.1

Released at 06.11.2026

* Version bumps (svenkubiak)
  * guava 33.7.2-jre -&gt; 33.8.0-jre
  * opentelemetry-semconv 1.44.0 -&gt; 1.45.0
  * classgraph 6.0.1 -&gt; 6.0.2

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.16.0...10.16.1)

## Version 10.16.0

Released at 02.11.2026

* MongoDB commands are traced again when otlp.enable is true (svenkubiak)
* **Behaviour change** Removed freemarker-java8. .format() without a pattern prints toString(), format styles, ISO_* names, a zone argument, toZonedDateTime, zoneId.format and clock.format are no longer accepted, and ${zoneId} prints the zone id (svenkubiak)
* Cleanup of unused dependencies (svenkubiak)
* Version bumps (svenkubiak)
  * greenmail 2.1.14 -&gt; 2.1.15
  * mongo-driver-sync 5.13.0 -&gt; 5.14.0
  * classgraph 6.0.0 -&gt; 6.0.1
  * tika-core 5.0.1 -&gt; 5.0.2
  * hibernate-validator 9.1.5.Final -&gt; 9.1.6.Final

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.15.1...10.16.0)

## Version 10.15.1

Released at 24.10.2026

* Fixed /@admin/cache failing with a ClassCastException when a cache other than the built-in implementation was registered via CacheProvider#addCache (svenkubiak)
* Version bumps (svenkubiak)
  * classgraph 4.8.196 -&gt; 6.0.0
  * open-telemetry 1.66.0 -&gt; 1.67.0
  * commons-codec 1.22.1 -&gt; 1.23.0
  * fory-core 1.7.6 -&gt; 1.7.7
  * caffeine 3.3.0 -&gt; 3.3.1
  * hibernate-validator 9.1.4.Final -&gt; 9.1.5.Final
  * jackson-core 2.22.3 -&gt; 2.22.4

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.15.0...10.15.1)

## Version 10.15.0

Released at 03.10.2026

* **Behaviour change** Session#put and Flash#put reject reserved JWT claim names and internal keys with an IllegalArgumentException instead of losing the whole cookie, the ineffective check for "|", ":", "&" and spaces was removed (svenkubiak)
* **Behaviour change** Mail#header rejects a value with line breaks and an invalid header name with an IllegalArgumentException to prevent header injection (svenkubiak)
* **Behaviour change** The application refuses to start if a @Run method is not public or has parameters, such a job was scheduled but never ran (svenkubiak)
* **Behaviour change** The application refuses to start in prod mode when running as root, the check never applied before as it ran before the mode was set (svenkubiak)
* **Behaviour change** Validator#expectNumeric accepts signed decimal numbers with ASCII digits instead of digits only, use expectRegex to require digits only (svenkubiak)
* **Behaviour change** The classpath scan on startup skips known library packages, which reduces the startup time, application classes must not be placed in these packages (svenkubiak)
* Added proxy support for SSE routes: X-Accel-Buffering and Cache-Control headers and a comment heartbeat every 30 seconds (svenkubiak)
* Fixed Form#getInteger, #getLong, #getDouble and #getFloat throwing a NumberFormatException instead of returning an empty Optional for values the target type cannot parse (svenkubiak)
* Fixed Validator#expectMinValue, #expectMaxValue and #expectRangeValue throwing on Unicode digits and rejecting negative and decimal numbers (svenkubiak)
* Fixed Validator#expectExactMatch and #expectMatch accepting an empty value when the other field is filled (svenkubiak)
* Fixed the HTTP parameter pollution check being bypassable with a percent-encoded parameter name (svenkubiak)
* Fixed every SSE client receiving a bogus ": ok" data event on connect (svenkubiak)
* Fixed concurrent login and second factor attempts exceeding the failed attempt budget (svenkubiak)
* Fixed the startup failing when a class with a @Run method has further annotated methods or the @Run method has further annotations (svenkubiak)
* Fixed errors of scheduled tasks being swallowed by a NullPointerException instead of being logged (svenkubiak)
* Fixed runs of an "Every X" job overlapping when a run takes longer than the interval, the next run is now scheduled after the previous one finished (svenkubiak)
* Fixed the server accepting requests before collections, indexes, jobs and subscribers were registered, which silently dropped saves during startup (svenkubiak)
* Fixed a mail without from() not being sent, smtp.from is now used as default sender and every send failure is logged (svenkubiak)
* Fixed an HTML mail with attachments being sent as plain text (svenkubiak)
* Fixed a NullPointerException when a cache fallback returns null or null is put into the cache, null is now returned and not cached, putting null removes the value (svenkubiak)
* Fixed Datastore#save not storing an object with an id set by the application but reporting success, save now inserts or replaces (svenkubiak)
* Fixed MongoDB usernames and passwords with special characters breaking the connection and a missing authdb resulting in authSource=null, credentials are now passed separately and authdb defaults to admin (svenkubiak)
* Fixed a missing translation key in ${i18n("key")} failing the whole page, it now renders an empty string and is logged once as warning (svenkubiak)
* Fixed the dev mode failing on a clean build that deletes target/classes, the directory is now watched again once it is recreated (svenkubiak)
* Fixed the maven archetype failing with -DoutputDirectory or in IDE wizards and leaving the application secret placeholder behind (svenkubiak)
* Fixed Authentication#isValidLogin not checking the salt, a missing salt consumed a failed login attempt (svenkubiak)
* Version bumps (svenkubiak)
  * undertow-core 2.4.3.Final -&gt; 2.4.4.Final

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.14.1...10.15.0)

## Version 10.14.1

Released at 01.10.2026

* Fixed failed maven release (svenkubiak)

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.14.0...10.14.1)

## Version 10.14.0

Released at 01.10.2026

* **Behaviour change** Secrets (application.secret, *.cookie.secret) must be exactly 64 bytes and keys (*.cookie.key) at least 64 bytes, otherwise the startup fails (svenkubiak)
* **Behaviour change** vault{fallback} reads the vault first and uses the fallback only without a vault entry (svenkubiak)
* **Behaviour change** Vault#put throws on a blank value or a failed write instead of only logging (svenkubiak)
* **Behaviour change** Missing @Indexed indexes are created on startup, a unique index fails if duplicates exist (svenkubiak)
* **Deprecation** CommonUtils#blacklist and CommonUtils#isBlacklisted, use TokenBlacklist instead (svenkubiak)
* Added TokenBlacklist as extension point for revoking authentication cookies, incl. revokeSubject for all cookies of a subject (svenkubiak)
* Fixed cron tasks stopping after ~24,000 runs and running up to one second early (svenkubiak)
* Fixed the ExceptionHandler not being called for POST, PUT, PATCH and non-blocking routes (svenkubiak)
* Fixed On.anyOf registering only the last HTTP method (svenkubiak)
* Fixed revoked authentication cookies becoming valid again after 60 minutes (svenkubiak)
* Fixed a password login resetting the failed attempts of the admin second factor (svenkubiak)
* Fixed Vault#put destroying vault.p12 on failure, the vault being rewritten on every start and not starting on Windows (svenkubiak)
* Fixed Vault#put rewriting vault.p12 when the value is unchanged (svenkubiak)
* Fixed subscribers not being registered, not receiving nested payload types and being blocked by a failing subscriber (svenkubiak)
* Fixed @Collection and @Indexed reading other annotations of a class or field (svenkubiak)
* Fixed mail attachments being read from the working directory instead of the given path (svenkubiak)
* Fixed arg{default} and env{default} ignoring a set system property or environment variable (svenkubiak)
* Fixed the maven archetype generating projects with mangoo I/O 10.11.0 (svenkubiak)

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.13.1...10.14.0)

## Version 10.13.1

Released at 30.09.2026

* Added an optional ServerSentEventRoute#withHandler(Class) which replaces the default ServerSentEventHandler of an SSE route with a connection callback of the application, so that a connection can be inspected, rejected or keyed per client instead of per request URI, a route without it keeps the previous broadcast behaviour and a custom handler is responsible for registering the connection and for attaching a close task itself (svenkubiak)
* Added ServerSentEventManager#removeConnection(key, connection) which removes a connection from a given key, as the existing removeConnection(connection) can only find a connection that is held under its request URI (svenkubiak)
* WebSocketRoute#to now registers itself as type "websocket" instead of "sse", which only affected the startup log and let a URL bound as both a WebSocket and a request route slip through the duplicate detection (svenkubiak)
* Version bumps (svenkubiak)
  * guava 33.7.1-jre -&gt; 33.7.2-jre
  * tika 4.0.0 -&gt; 4.1.0
  * commons-lang3 3.20.0 -&gt; 3.21.0
  * cactoos 0.62.0 -&gt; 0.63.0
  * fory 1.7.5 -&gt; 1.7.5

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.13.0...10.13.1)

## Version 10.13.0

Released at 29.09.2026

* **Behaviour change** Argon2 hashing is now gated as an OOM protection since a single computation holds the configured memory cost of heap for its entire duration, the new io.mangoo.crypto.PasswordHasher holds the limit, CommonUtils#hashArgon2 and CommonUtils#matchArgon2 throw an unchecked MangooHashingException when no slot becomes available in time and the ExceptionHandler answers it with 503 instead of 500 (svenkubiak)
* Added authentication.hashing.concurrency and authentication.hashing.timeout to configure the gate and authentication.hashing.memory, authentication.hashing.iterations and authentication.hashing.parallelism to configure the Argon2 parameters, values below 8192 KiB, 2 iterations or 1 lane are rejected at startup (svenkubiak)
* **Behaviour change** The Argon2 defaults are lowered from m=80000, t=6, p=2 to m=32768, t=3, p=1, which is still above the OWASP minimum at around 100 ms and 32 MB instead of 230 ms and 91 MB per hash, p=1 because BouncyCastle computes the lanes sequentially (svenkubiak)
* **Behaviour change** CommonUtils#hashArgon2 now returns the standard PHC string $argon2id$v=19$m=...,t=...,p=...$salt$hash instead of a bare Base64 hash, CommonUtils#matchArgon2 verifies with the parameters embedded in the stored hash rather than the current configuration, hashes from earlier versions keep verifying with m=80000, t=6, p=2 and the given salt, and the given salt must still match the one embedded in a PHC hash (svenkubiak)
* Added CommonUtils#needsRehash(hash) which reports a legacy hash or a hash created with other than the configured parameters, to be evaluated only after a successful verification, so applications can rehash transparently on login (svenkubiak)
* CommonUtils#matchArgon2 now compares the raw hash bytes in constant time instead of comparing the Base64 representation with a short-circuiting Arrays#areEqual (svenkubiak)
* **Behaviour change** Authentication#isValid now means "fully authenticated" and returns false while a second factor is outstanding, the previous behaviour is available as Authentication#hasSubject and Authentication#isValidLogin now fails closed without counting a failed attempt (svenkubiak)
* Added Authentication#isValidSecondFactor(identifier, secret, totp) which applies the authentication.lock failed attempt budget to the second factor, deprecating the unthrottled Authentication#isValidSecondFactor(secret, totp), and Authentication#userHasSecondFactorLock (svenkubiak)
* **Behaviour change** The failed attempt budgets of the password step and the second factor step are now counted separately, a lockout is an absolute point in time, authentication.lock locks on the configured attempt instead of one attempt later and the new authentication.lock.duration configures its duration in minutes (svenkubiak)
* Added form.maxfilesize to configure the maximum size of a single uploaded file, the default stays at 5 MiB and note that undertow.maxentitysize still caps the whole request body one layer earlier (svenkubiak)
* The FormHandler now logs on WARN why it rejected a form, including the offending size and the limit, instead of failing the request without a single log line (svenkubiak)
* Added Response#bodyFile(Path) which streams a file to the client with constant memory usage (svenkubiak)
* The mangoo I/O test runner no longer starts the application twice when JUnit invokes beforeAll of several test classes at the same time (svenkubiak)
* Fixed an issue in OTLP handling and in form-cookie, added additional unit tests for OTLP (svenkubiak)
* Documented the complete login flow with and without two-factor authentication (svenkubiak)
* Version bumps (svenkubiak)
  * fory-core 1.7.2 -&gt; 1.7.6
  * cactoos 0.61.1 -&gt; 0.62
  * nimbus-jwt 10.9.1 -&gt; 10.10
  * jackson 2.22.2 -&gt; 2.22.3
  * hibernate-validator 9.1.3.Final -&gt; 9.1.4.Final
  * greenmail 2.1.13 -&gt; 2.1.14
  * caffeine 3.2.4 -&gt; 3.3.0
  * classgraph 4.8.195 -&gt; 4.8.196
  * mockito-core 5.23.0 -&gt; 5.24.0
  * mongo-driver-sync 5.12.0 -&gt; 5.13.0
  * archunit-junit5 1.5.0 -&gt; 1.5.1

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.12.2...10.13.0)

## Version 10.12.2

Released at 12.09.2026

* Fixed a potential race-condition when handing of AttachmentKey (svenkubiak)

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.12.1...10.12.2)

## Version 10.12.1

Released at 12.09.2026

* Fixed incorrect handling of SSE connections (svenkubiak)
* Query parameters can no longer override a route parameter of the same name (svenkubiak)
* Added Request#getPathParameter, Request#getQueryParameter and Request#hasPathParameter to tell route parameters and client input apart (svenkubiak)
* Added application.parameter.strict to reject requests whose query parameter collides with a route parameter (svenkubiak)
* Messages are now resolved per request and no longer leak the locale of one request into concurrent requests (svenkubiak)
* Resolving a message bundle no longer modifies the JVM default locale (svenkubiak)
* Fixed incorrect cookie handling in /@admin dashboard (svenkubiak)

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.12.0...10.12.1)

## Version 10.12.0

Released at 12.09.2026

* CORS responses now send Vary: Origin to prevent cache poisoning by shared caches (svenkubiak)
* The origin parameter on authentication redirects is now URL encoded, keeps the query string and can no longer be protocol-relative (svenkubiak)
* Multiple query and request parameter with same values are now rejected (svenkubiak)
* Cookie settings for prod config are now secure=true by default (svenkubiak)
* Removed SSE Auth (svenkubiak)
* Successful login now resets lock counter (svenkubiak)
* Version bumps (svenkubiak)
  * fory-core 1.7.1 -&gt; 1.7.2

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.11.6...10.12.0)

## Version 10.11.6

Released at 11.09.2026

* Various security hardenings (svenkubia)
* Fixed an issue in the BinaryHandler that overwrote headers from a controller (svenkubiak)
* Version bumps (svenkubiak)
  * oopentelemetry-semconv 1.43.0 -&gt; 1.44.0
  * opentelemetry 1.65.0 -&gt; 1.66.0
  * mongodb-driver-sync 5.11.0 -&gt; 5.11.1
  * bouncy-castle 1.85 -&gt; 1.86

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.11.5...10.11.6)

## Version 10.11.5

Released at 08.09.2026

* Fixed an issue that prevented mvn mangooio:run from starting (svenkubiak)
* Changed behaviour of error propagation in JsonUtils (svenkubiak)
* Doucmentation update (svenkubiak)
* Version bumps (svenkubiak)
  * classgraph 4.8.194 -&gt; 4.8.195

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.11.4...10.11.5)

## Version 10.11.4

Released at 06.09.2026

* Added additional documentation updates (svenkubiak)

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.11.3...10.11.4)

## Version 10.11.3

Released at 06.09.2026

* Update @admin dashboad to bulma 1.0.4 (svenkubiak)
* Fixed an issue in vault creation and value parsing (svenkubiak)
* Added additional method to datastore to retrieve the MongoClient (svenkubiak)
* Version bumps (svenkubiak)
  * fory-core 1.7.0 -&gt; 1.7.1

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.11.2...10.11.3)

## Version 10.11.2

Released at 01.09.2026

* Added additional documentation updates (svenkubiak)

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.11.1...10.11.2)

## Version 10.11.1

Released at 01.09.2026

* Added additional query options for MongoDB (svenkubiak)
* Added method for generating UUIDv7 in CommonUtils (svenkubiak)
* Fixed incorrect handling of getAuthenticationCookieKey (svenkubiak)
* Fixed incorrect handling of getApplicationVaultPath (svenkubiak)
* Switched to new Apache Fory library and refactoring usage of Fory (svenkubiak)
* Updated documentation (svenkubiak)
* Version bumps (svenkubiak)
  * greenmail-standalone 2.1.11 -&gt; 2.1.13
  * classgraph 4.8.186 -&gt; 4.8.194
  * junit 6.1.2 -&gt; 6.1.3
  * mongo-driver-sync 5.9.1 -&gt; 5.9.2
  * bouncy-castle 1.85 -&gt; 1.85.2
  * jackson 2.22.1 -&gt; 2.22.2
  * guava 33.6.0-jre -&gt; 33.7.1-jre
  * undertow 2.4.2.Final -&gt; 2.4.3.Final
  * tika-core 3.3.2 -&gt; 4.0.0
  * freemarker 2.3.34 -&gt; 2.3.35
  * mongo-driver-sync 5.9.2 -&gt; 5.11.0 
  * snakeyaml 2.6 -&gt; 2.7

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.10.0...10.11.1)

## Version 10.10.0

Released at 03.08.2026

* Set MongoDB version to v7.0 in Embedded MongoDB (svenkubiak)
* Version bumps (svenkubiak)
  * greenmail 2.1.9 -&gt; 2.1.11
  * open-telemetry 1.63.0 -&gt; 1.64.0
  * undertow-core 2.4.1.Final -&gt; 2.4.2.Final
  * junit 6.1.1 -&gt; 6.1.2
  * bouncy-castle 1.84 -&gt; 1.85
  * zt-exec 1.12 -&gt; 1.13.0
  * tika-core 3.3.1 -&gt; 3.3.2
  * classgraph 4.8.184 -&gt; 4.8.186
  * mongodb-driver-sync 5.9.0 -&gt; 5.9.1
  * hibernate-validator 9.1.2.Final -&gt; 9.1.3.Final
  * cactoos 0.61.0 -&gt; 0.61.1
  * commons-codec 1.22.0 -&gt; 1.22.1
  * commons-validator 1.10.1 -&gt; 1.11.0

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.9.0...10.10.0)

## Version 10.9.0

Released at 14.07.2026

* Version bumps (svenkubiak)
  * commons-logging 1.3.6 -&gt; 1.4.0
  * hibernate-validator 9.1.0.Final -&gt; 9.1.2.Final
  * log4j 2.26.0 -&gt; 2.26.1
  * otp-java 2.1.0 -&gt; 2.2.0
  * mongodb-driver-sync 5.8.0 -&gt; 5.9.0
  * jackson 2.22.0 -&gt; 2.22.1
  * undertow 2.4.0.RC4 -&gt; 2.4.1.Final

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.8.6...10.9.0)

## Version 10.8.6

Released at 12.06.2026

* Updated release script to avoid release issues (svenkubiak)

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.8.5...10.8.6)

## Version 10.8.5

Released at 12.06.2026

* Fixed build issue (svenkubiak)

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.8.4...10.8.5)

## Version 10.8.4

Released at 12.06.2026

* Fixed build issue (svenkubiak)

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.8.3...10.8.4)

## Version 10.8.3

Released at 12.06.2026

* Fixed an issue when setting session cookie (svenkubiak)
* Version bumps (svenkubiak)
  * mongo-driver-sync 5.7.0 -&gt; 5.8.0
  * jackson 2.21.3 -&gt; 2.22.0
  * nimbus-jose-jwt 10.9 -&gt; 10.9.1
  * open-telemetry 1.62.0 -&gt; 1.63.0

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.8.2...10.8.3)

## Version 10.8.2

Released at 17.05.2026

* Fixed dependency issue (svenkubiak)

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.8.1...10.8.2)

## Version 10.8.1

Released at 17.05.2026

* Fixed dependency issue (svenkubiak)

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.8.0...10.8.1)

## Version 10.8.0

Released at 17.05.2026

* Flushing SSE connection on first connect (svenkubiak)
* Version bumps (svenkubiak)
  * jackson 2.21.1 -&gt; 2.21.3
  * opentelemetry-semconv 1.40.0 -&gt; 1.41.1
  * opentelemetry 1.61.0 -&gt; 1.62.0
  * mongo-driver-sync 5.6.5 -&gt; 5.7.0
  * caffeine 3.2.3 -&gt; 3.2.4
  * log4j 2.25.4 -&gt; 2.26.0

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.7.0...10.8.0)

## Version 10.7.0

Released at 27.04.2026

* Added Log4j configuration check on application start (svenkubiak)
* Sonar refactorings (svenkubiak)
* Version bumps (svenkubiak)
  * opentelemetry 1.60.1 -&gt; 1.61.0
  * guava-jre 33.5.0-jre -&gt; 33.6.0-jre
  * bouncy-castle 1.83 -&gt; 1.84
  * commons-codec 1.21.0 -&gt; 1.22.0
  * commons-io 2.21.0 -&gt; 2.22.0
  * cactoos 0.59.0 -&gt; 0.60.0
  
[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.6.1...10.7.0)

## Version 10.6.1

Released at 10.04.2026

* Version bumps (svenkubiak)
  * mongo-driver-sync 5.6.4 -&gt; 5.6.5 
  * undertow-core 2.3.24.Final -&gt; 2.4.0.RC4

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.6.0...10.6.1)

## Version 10.6.0

Released at 09.04.2026

* Added healthcheck method to datastore (svenkubiak)
* Hardened Form parsing (svenkubiak)
* Sonar refactorings (svenkubiak)
* Refactorings and improvements (svenkubiak)
* Version bumps (svenkubiak)
  * snake-yaml 2.5 -&gt; 2.6
  * mockito-core 5.21.0 -&gt; 5.22.0
  * opentelemetry 1.59.0 -&gt; 1.60.1
  * undertow-core 2.3.23.Final -&gt; 2.3.24.Final
  * jackson 2.21.1 -&gt; 2.21.2
  * nimbus-jose-jwt 10.8 -&gt; 10.9
  * log4j 2.25.3 -&gt; 2.25.4

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.5.0...10.6.0)

## Version 10.5.0

Released at 27.02.2026

* Added timezone support to application configuration (svenkubiak)
* Added timezone handling of POJO-mapped MongoDB entities (svenkubiak)
* Version bumps (svenkubiak)
  * mongo-driver-sync 5.6.2 -&gt; 5.6.3
  * opentelemetry 1.58.0 -&gt; 1.59.0
  * undertow-core 2.3.22.Final -&gt; 2.3.23.Final
  * nimbus-jose-jwt 10.7 -&gt; 10.8
  * jackson-databind 2.21.0 -&gt; 2.21.1

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.4.2...10.5.0)

## Version 10.4.2

Released at 07.02.2026

* Fixed an issue when creating indices

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.4.1...10.4.2)

## Version 10.4.1

Released at 03.02.2026

* Fixed a dependency conflict

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.4.0...10.4.1)

## Version 10.4.0

Released at 09.02.2026

* Added option to passthrough JSR 380 validation errors (svenkubiak)
* Added better handling of unparsable JSON and entities (svenkubiak)
* Version bumps (svenkubiak)
  * opentelemetry-semconv 1.37 -&gt; 1.39
  * commons-codec 1.20.0 -&gt; 1.21.0

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.3.0...10.4.0)

## Version 10.3.1

Released at 19.01.2026

* Version bumps (svenkubiak)
  * undertow-core 2.3.21.Final -&gt; 2.3.22.Final
  * jackson-databind 2.20.1 -&gt; 2.21.0

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.3.0...10.3.1)

## Version 10.3.0

Released at 15.01.2026

* Argument Utils now have an additional null-check (svenkubiak)
* Authentication-, Session- and Flash-Cookie don't invalidate if parsing fails (svenkubiak)
* CSRF-Filter now returns default Forbidden-Body (svenkubiak)
* Sonar refactorings (svenkubiak)
* Version bumps (svenkubiak)
  * undertow-core 2.3.20.Final -&gt; 2.3.21.Final
  * otp 1.57 -&gt; 1.58
  * nimbus-jose-jwt 10.6 -&gt; 10.7

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.2.0...10.3.0)

## Version 10.2.0

Released at 17.12.2025

* Version bumps (svenkubiak)
    * mongo-driver-sync 5.6.1 -&gt; 5.6.2
    * otp 1.56 -&gt; 1.57
    * java-uuid-generator 5.1.1 -&gt; 5.2.0
    * greenmail 2.1.7 -&gt; 2.1.8
    * log4j 2.25.2 -&gt; 2.25.31

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.1.0...10.2.0)
  
## Version 10.1.0

Released at 01.12.2025

* Version bumps (svenkubiak)
    * jackson 2.20.0 -&gt; 2.20.1 
    * commons-codec 1.19.0 -&gt; 1.20.0
    * nimbus-jose-jwt 10.5 -&gt; 10.6
    * commons-io 2.20.0 -&gt; 2.21.0
    * commons-lang3 3.19.0 -&gt; 3.20.0
    * hibernate-validator 9.0.1.Final -&gt; 9.1.0.Final
    * commons-validator 1.10.0 -&gt; 1.10.1
    * bouncy-castle 1.82 -&gt; 1.83

[Full Changelog](https://github.com/svenkubiak/mangooio/compare/10.0.0...10.1.0)

## Version 10.0.0

Released at 03.11.2025

* Added Support for Open Telemetry (svenkubiak)
* Added Support for Jakarta Validation API for methods parameters (svenkubiak)
* Added new vault functionality for storing secrets based on Java Keystore (svenkubiak)
* Added HTTPS connector (svenkubiak)
* Removed cryptex{} from configuration in favor of new vault (svenkubiak)
* Removed all deprecated methods and classed (svenkubiak)
* Removed AJP connector (svenkubiak)
* Replaced internal TOTP implementation with external library (svenkubiak)
* Switched from Paseto to JWT (svenkubiak)
* MongoDB index are now case-insensitive when they are unique (svenkubiak)
* Upgraded to Java 25 (svenkubiak)
* Sonar refactorings (svenkubiak)
* Version bumps (svenkubiak)