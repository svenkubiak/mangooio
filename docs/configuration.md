# Configuration ⚙️

Almost all runtime behavior is controlled through one YAML file: `src/main/resources/config.yaml`. mangoo I/O loads it with [SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml/src/master/), flattens the nested keys into dot notation, and exposes them through the `Config` class and Guice `@Named` bindings. There is deliberately no second configuration mechanism (no properties files, no XML, no `@ConfigurationProperties` classes to keep in sync); if a setting exists, it lives in this one file, which makes it realistic to actually read the whole configuration of an application in one sitting.

Typical uses include connector host and port, cookie names, MongoDB connection details, SMTP settings, feature toggles (admin UI, metrics, scheduler), and security options (CORS, authentication redirects). Sensitive values should reference environment variables, JVM properties or [secret files](secrets.md) rather than plain text in the repository, since `config.yaml` normally ends up committed to source control.

The file is split into a **`default`** block (shared by all modes) and an **`environments`** block with overrides for `dev`, `test`, and `prod`. When the application starts, it merges `default` with the section for the active mode, key by key, so a key you only set in `default` still applies everywhere, and a key you also set under `environments.prod` overrides it just for production. Missing an environment section for the active mode causes startup to fail on purpose. That may feel strict the first time it happens, but it exists to catch an environment block that was renamed or forgotten, where the app would otherwise silently fall back to defaults in a place where nobody is watching. Only `dev`, `test` and `prod` exist; any other value of `application.mode` selects `prod`.

Values are addressed with dot notation that mirrors the YAML hierarchy:

```yaml
application:
  api:
    key: foo
```

```java
config.getString("application.api.key");
```

You can point the application at a different file entirely by setting the JVM property `application.config` to an absolute path, which is handy for a config file that lives outside the JAR in production (see [Operating](operating.md)).

## Accessing configuration

Inject `io.mangoo.core.Config`. Constructor injection is preferred, mostly because it makes `Config` show up in your test setup explicitly instead of being pulled in behind the scenes:

```java
import io.mangoo.core.Config;
import jakarta.inject.Inject;

public class MyService {
    private final Config config;

    @Inject
    public MyService(Config config) {
        this.config = config;
    }
}
```

Look up values by key string, or by the constants in `io.mangoo.constants.Key` if you would rather avoid typos in raw strings scattered across the codebase:

```java
config.getString("application.api.key");
config.getString(Key.APPLICATION_API_KEY);
```

Typed helpers also exist (`getInt`, `getLong`, `getBoolean`) and dedicated getters such as `getApplicationName()` or `getSmtpHost()` for the values the framework itself relies on.

Every entry in `config.yaml` is also bound as a Guice `@Named` string, so a class that only needs one particular value can ask for exactly that, rather than pulling in the whole `Config` object:

```java
@Inject
public MyService(@Named("application.named") String named) {
    // ...
}
```

## Modes

mangoo I/O has three modes: **dev**, **test**, and **prod**. The mode decides which `environments.<mode>` block gets merged into `default`, so switching modes is really just switching which overrides apply, not a different code path.

- **dev** is activated by `mvn mangooio:run`.
- **test** is activated when tests start the application through `TestRunner`.
- **prod** is the default when you start the packaged JAR, so you do not need to set anything special for a normal deployment.

Set the mode explicitly with a system property, useful when you want `prod`-like behavior locally to reproduce something:

```shell
java -Dapplication.mode=dev -jar myapp.jar
```

or, less common outside of tooling that starts the JVM itself:

```java
System.setProperty("application.mode", "dev");
```

## Environment-specific values

Put shared settings under `default` and overrides under `environments.<mode>`. The active mode overwrites matching keys from `default`, leaving everything else untouched:

```yaml
default:
  application:
    name: myapp
    url: http://localhost

environments:
  test:
    application:
      url: https://test.example.com
  prod:
    application:
      url: https://example.com
```

Here `application.name` stays `myapp` in every mode, since none of the environment blocks touch it, while `application.url` changes depending on where the app is actually running.

The active environment block is required, even if it only overrides a single key. If `environments.dev` (or `test` / `prod`) is missing entirely, startup fails rather than quietly falling back to `default` alone.

## Secrets, environment variables, and JVM arguments

Configuration values can also come from environment variables, JVM arguments or secret files instead of being written out in plain text. See [Secrets](secrets.md) for how `env{}`, `arg{}` and `file{}` placeholders are resolved. A placeholder without a value and without a fallback fails the startup, unless it is optional (`env{?}`), and in prod mode the framework secrets must not be literal.

As a rule of thumb: cookie secrets and keys, database and SMTP credentials should never sit in source control as clear text, since `config.yaml` is versioned right alongside your application code and gets the same read access as everything else in the repository.

## Connectors

At least one HTTP or HTTPS connector must be configured, otherwise the application refuses to start. There is no implicit port. A connector starts only when both `host` and `port` are set. HTTPS accepts TLS 1.3 only.

```yaml
default:
  connector:
    http:
      host: localhost
      port: 8080
    https:
      host: localhost
      port: 8443
      keystore: /etc/myapp/https.p12
      password: file{}
```

HTTPS reads key and certificate chain from the PKCS12 file in `connector.https.keystore`, see [Secrets](secrets.md#https-certificates). The connector speaks TLS 1.3 and HTTP/2. `connector.https.certificate.alias` is only needed if the file holds more than one key entry.

## Default values

Keys that you omit fall back to the defaults below. Cells marked *(none)* have no built-in default, meaning the feature stays off or the value stays empty until you set it yourself. MongoDB keys are nested under `persistence` in YAML (`persistence.mongo.host` or `persistence.mongo.url`, and `persistence.<prefix>.mongo.host` for additional named datastores, see [Persistence](persistence.md)).

| Key | Description | Default |
|---|---|---|
| `application.admin.enable` | Enables the admin dashboard | `false` |
| `application.admin.locale` | Locale for the admin dashboard | `en_EN` |
| `application.admin.password` | Argon2id PHC hash of the admin dashboard password | *(none)* |
| `application.admin.secret` | Admin TOTP secret; if set, MFA is required | *(none)* |
| `application.admin.username` | Admin dashboard username | *(none)* |
| `application.allowedOrigins` | Comma-separated origins for `OriginFilter` | *(none)* |
| `application.api.key` | Shared secret for `ApiKeyFilter` (at least 64 bytes) | *(none)* |
| `application.language` | Default application language | `en` |
| `application.name` | Application name (JWT issuer, logs) | `mangooio-application` |
| `application.named` | Example named Guice binding | *(none)* |
| `application.parameter.strict` | Reject requests whose query parameter collides with a route parameter with a `400` | `false` |
| `application.secret` | Application secret, **required**; fallback for cookie secrets and keys (exactly 64 bytes) | *(none)* |
| `application.shutdown.timeout` | Seconds from the start of shutdown, including the heartbeat stop and closing SSE and WebSocket connections, see [Graceful shutdown](operating.md#graceful-shutdown) | `20` |
| `application.timezone` | Application timezone | `UTC` |
| `application.validation.passthrough` | Return Bean Validation errors as JSON instead of a rendered form | `false` |
| `authentication.blacklist` | Enable revocation of authentication cookies, see [Blacklist](authentication.md#blacklist) | `false` |
| `authentication.cookie.name` | Authentication cookie name | `mangooio-auth` |
| `authentication.cookie.key` | JWT signing key (min. 64 bytes); falls back to `application.secret` | *(none)* |
| `authentication.cookie.secret` | JWT encryption secret (exactly 64 bytes); falls back to `application.secret` | *(none)* |
| `authentication.cookie.remember.expires` | Remember-me lifetime in **seconds** | `2592000` |
| `authentication.cookie.samesitemode` | SameSite attribute | `Strict` |
| `authentication.cookie.secure` | Secure cookie flag | `false` |
| `authentication.cookie.token.expires` | Token and cookie lifetime in **seconds** | `3600` |
| `authentication.hashing.concurrency` | Maximum number of Argon2 hashes computed at the same time; `0` derives it from the configured memory cost and the heap, clamped to 2..8 | `0` |
| `authentication.hashing.iterations` | Argon2id iterations (time cost); minimum `2` | `3` |
| `authentication.hashing.memory` | Argon2id memory cost in **KiB**; minimum `8192` | `32768` |
| `authentication.hashing.parallelism` | Argon2id lanes; minimum `1` | `1` |
| `authentication.hashing.timeout` | Time in **milliseconds** a request waits for a free hashing slot | `5000` |
| `authentication.lock` | Failed attempts before lockout, per identifier and per step (password, second factor) | `10` |
| `authentication.lock.duration` | Lockout duration in **minutes**, absolute and not extended by further failed attempts | `60` |
| `authentication.origin` | Append `?origin=` on auth redirects | `false` |
| `authentication.redirect.login` | Redirect when authentication is missing | *(none)* |
| `authentication.redirect.mfa` | Redirect when MFA is required | *(none)* |
| `connector.http.host` | HTTP bind address | *(none)* |
| `connector.http.port` | HTTP port | *(none)* |
| `connector.https.host` | HTTPS bind address | *(none)* |
| `connector.https.port` | HTTPS port | *(none)* |
| `connector.https.certificate.alias` | Alias of the key entry in the keystore; only needed if it holds more than one | *(none)* |
| `connector.https.keystore` | PKCS12 file with key and certificate chain of the HTTPS connector | *(none)* |
| `connector.https.password` | Password of the keystore and its key entry | *(none)* |
| `connector.proxy` | Take client address and scheme from the `X-Forwarded-*` headers; only enable behind a trusted proxy that overwrites them | `false` |
| `cors.alloworigin` | Regex for `Access-Control-Allow-Origin` | `^http(s)?://(www\.)?example\.(com\|org)$` |
| `cors.enable` | Send CORS headers | `false` |
| `cors.headers.allowcredentials` | `Access-Control-Allow-Credentials` | `true` |
| `cors.headers.allowheaders` | `Access-Control-Allow-Headers` | `Authorization,Content-Type,Link,X-Total-Count,Range` |
| `cors.headers.allowmethods` | `Access-Control-Allow-Methods` | `DELETE,GET,HEAD,OPTIONS,PATCH,POST,PUT` |
| `cors.headers.exposeheaders` | `Access-Control-Expose-Headers` | `Accept-Ranges,Content-Length,Content-Range,ETag,Link,Server,X-Total-Count` |
| `cors.headers.maxage` | `Access-Control-Max-Age` | `864000` |
| `cors.urlpattern` | Regex of request URLs that receive CORS headers | `^http(s)?://([^/]+)(:([^/]+))?(/([^/])+)?/api(/.*)?$` |
| `flash.cookie.name` | Flash cookie name | `mangooio-flash` |
| `flash.cookie.key` | Flash JWT signing key (min. 64 bytes) | *(none)* |
| `flash.cookie.secret` | Flash JWT encryption secret (exactly 64 bytes) | *(none)* |
| `form.maxfilesize` | Maximum size of a single uploaded file in bytes | `5242880` |
| `i18n.cookie.name` | Locale cookie name | `mangooio-i18n` |
| `metrics.enable` | Collect request metrics for the admin dashboard | `false` |
| `otlp.enable` | Enable OpenTelemetry export | `false` |
| `otlp.endpoint` | OTLP gRPC endpoint | *(none)* |
| `persistence.enable` | Enable MongoDB persistence | `true` |
| `persistence.mongo.auth` | Use MongoDB authentication | `false` |
| `persistence.mongo.authdb` | MongoDB authentication database | `admin` |
| `persistence.mongo.dbname` | MongoDB database name | `mangoo-io-mongodb` |
| `persistence.mongo.embedded` | Start embedded MongoDB | `false` |
| `persistence.mongo.host` | MongoDB host | `localhost` |
| `persistence.mongo.password` | MongoDB password, passed to the driver as is, so special characters need no encoding | *(none)* |
| `persistence.mongo.port` | MongoDB port | `27017` |
| `persistence.mongo.schema` | `create` creates missing collections and indexes at startup, `validate` only checks them, see [Schema](persistence.md#schema) | `create` |
| `persistence.mongo.username` | MongoDB username | *(none)* |
| `persistence.mongo.url` | `mongodb://` or `mongodb+srv://` connection string that replaces host and port. Credentials in the URL are rejected | *(none)* |
| `scheduler.enable` | Enable `@Run` scheduling | `true` |
| `session.cookie.expires` | Persist the session cookie beyond the browser session | `false` |
| `session.cookie.name` | Session cookie name | `mangooio-session` |
| `session.cookie.key` | Session JWT signing key (min. 64 bytes) | *(none)* |
| `session.cookie.secret` | Session JWT encryption secret (exactly 64 bytes) | *(none)* |
| `session.cookie.samesitemode` | SameSite attribute | `Strict` |
| `session.cookie.secure` | Secure cookie flag (also used for flash) | `false` |
| `session.cookie.token.expires` | Session token lifetime in **seconds** | `3600` |
| `smtp.authentication` | Enable SMTP authentication | `false` |
| `smtp.connection.timeout` | Time in **milliseconds** to wait for the SMTP connection. `0` waits forever and is rejected | `10000` |
| `smtp.debug` | Enable SMTP debug output | `false` |
| `smtp.from` | Default From address | `mangoo <noreply@mangoo.local>` |
| `smtp.host` | SMTP host | `localhost` |
| `smtp.password` | SMTP password | *(none)* |
| `smtp.port` | SMTP port | `25` |
| `smtp.protocol` | `smtps` opens TLS immediately, `smtptls` requires STARTTLS, `smtp` is cleartext and only allowed in dev and test | `smtps` |
| `smtp.read.timeout` | Time in **milliseconds** to wait for an SMTP read. `0` waits forever and is rejected | `30000` |
| `smtp.username` | SMTP username | *(none)* |
| `undertow.maxentitysize` | Maximum HTTP entity size in bytes | `4194304` |

`smtps` and `smtptls` are the two encrypted ways to reach a mail server, and they are not interchangeable: `smtps` starts the TLS handshake before any SMTP command, which is what port 465 expects, while `smtptls` connects in cleartext and then requires STARTTLS, which is what port 587 expects. If the server does not upgrade the connection, the delivery is aborted before authentication or the message is sent. `smtp` leaves the connection in cleartext and the application refuses to start with it in prod mode; dev and test accept it because the local test server speaks plain SMTP. Every delivery gives up after `smtp.connection.timeout` milliseconds without a connection and after `smtp.read.timeout` milliseconds without a read. The defaults are 10 seconds and 30 seconds. Both configured values are written to the log when the mail session is created, and a delivery that hits either limit is logged again with the same values. A value of `0` or below is rejected at startup.

`authentication.cookie.samesitemode` and `session.cookie.samesitemode` default to `Strict`. `Lax` is the only other accepted value. Anything else, including `None`, fails startup. Flash and the locale cookie are always `Strict`. `Strict` is not sent on a cross-site navigation, such as a link from another domain. Use `Lax` when a login flow depends on that.

`undertow.maxentitysize` caps the body at 4 MiB. `form.maxfilesize` caps each uploaded file at 5 MiB, and the body limit is checked first, so a 5 MiB file needs a larger `maxentitysize` and a matching limit on the reverse proxy (`client_max_body_size` in nginx). A form that trips `form.maxfilesize` is rejected before the controller runs, logged at WARN, and answered with HTTP 400. Undertow's parameter limit (1000) and header size (8192 bytes) are not configurable. Worker and I/O thread counts are Undertow's defaults. The framework does not set them.

`application.mode` and `application.config` are JVM system properties, not YAML keys. `application.secrets.path` defaults to `/run/secrets` and is a system property or the environment variable `APPLICATION_SECRETS_PATH`. It cannot be set inside `config.yaml`. See [Secrets](secrets.md).

`authentication.cookie.token.expires` has to stay at or below `authentication.cookie.remember.expires`. Inbound cookies are accepted only up to the remember-me lifetime. A normal login cookie whose TTL is longer than that fails to parse. The defaults, 3600 and 2592000, do not hit this.

### Argon2 hashing

`CommonUtils.hashArgon2` and `CommonUtils.matchArgon2` compute Argon2id, and a single computation holds `authentication.hashing.memory` KiB of heap for its entire duration, 32 MiB with the default of 32768 KiB. Concurrent logins multiply that. The framework does not set Undertow's worker count, so the cap below is what keeps a burst of logins from allocating that memory without a bound.

The framework therefore caps how many hashes run at the same time. A call that finds every slot taken waits up to `authentication.hashing.timeout` milliseconds and is then rejected with a `MangooHashingException`, which is unchecked and does not change any signature. `Authentication#isValidLogin` catches it, logs it and returns `false` without counting a failed attempt, so an overload situation cannot lock a user out. Every other caller sees the exception and ends up in the `ExceptionHandler`, which answers it with a `503` rather than a silent "wrong password".

The default `authentication.hashing.concurrency: 0` derives the slot count as `floor((0.5 * maxMemory) / (memoryKiB * 1024 * 1.2))`, clamped to 2..8. A configured value greater than 0 is used as is. The effective value is logged at INFO during startup. A high explicit value is close to having no gate.

The defaults `m=32768`, `t=3`, `p=1` sit above the OWASP minimum of 19 MiB and two iterations and below RFC 9106's low-memory profile. `p=1` is deliberate: BouncyCastle's `Argon2BytesGenerator` computes the lanes sequentially, so a higher parallelism does not shorten a single hash, it only spreads the same memory over more lanes. A server gets its parallelism from concurrent requests, not from lanes inside one hash.

Values below `authentication.hashing.memory: 8192`, `authentication.hashing.iterations: 2` or `authentication.hashing.parallelism: 1` are rejected with an `IllegalArgumentException` at startup. The application refuses to start rather than hashing weaker than intended. `mvn -q mangooio:hash` creates the admin password hash with these three values, see [Administration](administration.md).

Changing the parameters is safe: a hash carries the parameters it was created with, see [Password hashing](authentication.md#password-hashing), and is always verified with those. `CommonUtils.needsRehash` reports hashes that were created with anything else than the current configuration.
