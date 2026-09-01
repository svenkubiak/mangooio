# Configuration

Almost all runtime behavior is controlled through one YAML file: `src/main/resources/config.yaml`. mangoo I/O loads it with [SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml/src/master/), flattens nested keys into dot notation, and exposes them through the `Config` class and Guice `@Named` bindings.

Typical uses include connector host and port, cookie names, MongoDB connection details, SMTP settings, feature toggles (admin UI, metrics, scheduler), and security options (CORS, authentication redirects). Sensitive values should reference the [vault](secrets.md), environment variables, or JVM properties rather than plain text in the repository.

The file is split into a **`default`** block (shared by all modes) and an **`environments`** block with overrides for `dev`, `test`, and `prod`. When the application starts, it merges `default` with the section for the active mode. Missing an environment section for the current mode causes startup to fail, which catches incomplete deployment configs early.

Values are addressed with dot notation that matches the YAML hierarchy:

```yaml
application:
  api:
    key: foo
```

```java
config.getString("application.api.key");
```

You can load a different file by setting the JVM property `application.config` to an absolute path.

## Accessing configuration

Inject `io.mangoo.core.Config`. Constructor injection is preferred:

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

Look up values by key string or by constants in `io.mangoo.constants.Key`:

```java
config.getString("application.api.key");
config.getString(Key.APPLICATION_API_KEY);
```

Typed helpers also exist (`getInt`, `getLong`, `getBoolean`) and dedicated getters such as `getApplicationName()` or `getSmtpHost()`.

Every entry in `config.yaml` is also bound as a Guice `@Named` string, so you can inject a single value:

```java
@Inject
public MyService(@Named("application.named") String named) {
    // ...
}
```

## Modes

mangoo I/O has three modes: **dev**, **test**, and **prod**.

- **dev** is activated by `mvn mangooio:run`.
- **test** is activated when tests start the application through `TestRunner`.
- **prod** is the default when you start the packaged JAR.

Set the mode explicitly with:

```shell
java -Dapplication.mode=dev -jar myapp.jar
```

or:

```java
System.setProperty("application.mode", "dev");
```

## Environment-specific values

Put shared settings under `default` and overrides under `environments.<mode>`. The active mode overwrites matching keys from `default`:

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

The active environment block is required. If `environments.dev` (or `test` / `prod`) is missing, startup fails.

## Secrets, environment variables, and JVM arguments

Configuration values can come from the application vault, environment variables, or JVM arguments. See [Secrets](secrets.md) for `vault{}`, `env{}`, and `arg{}`.

Do not put cookie signing keys, the vault password, or SMTP credentials in source control as clear text.

## Connectors

At least one HTTP or HTTPS connector must be configured, otherwise the application does not start.

```yaml
default:
  connector:
    http:
      host: localhost
      port: 8080
    https:
      host: localhost
      port: 8443
      certificate:
        alias: certificate
```

HTTPS uses an SSL context from the [vault](secrets.md). The certificate alias defaults to `certificate`.

## Default values

Keys that you omit fall back to these defaults. MongoDB keys are nested under `persistence` in YAML (`persistence.mongo.host`, and `persistence.<prefix>.mongo.host` for extra datastores).

| Key | Description | Default |
|---|---|---|
| `application.admin.enable` | Enables the admin dashboard | `false` |
| `application.admin.locale` | Locale for the admin dashboard | `en_EN` |
| `application.admin.password` | Admin dashboard password | — |
| `application.admin.secret` | Admin TOTP secret; if set, MFA is required | — |
| `application.admin.username` | Admin dashboard username | — |
| `application.allowedOrigins` | Comma-separated origins for `OriginFilter` | — |
| `application.api.key` | Shared secret for `ApiKeyFilter` | — |
| `application.controller` | Controller package prefix | `controllers.` |
| `application.language` | Default application language | `en` |
| `application.name` | Application name (JWT issuer, logs) | `mangooio-application` |
| `application.named` | Example named Guice binding | — |
| `application.secret` | Application secret; fallback for cookie keys | — |
| `application.timezone` | Application timezone | `UTC` |
| `application.validation.passthrough` | Return Bean Validation errors as JSON | `false` |
| `application.vault.enable` | Enable the PKCS12 vault | — |
| `application.vault.path` | Directory of `vault.p12` in prod | — |
| `application.vault.secret` | Vault password (min. 64 characters) | — |
| `authentication.blacklist` | Enable authentication blacklist cache | `false` |
| `authentication.cookie.name` | Authentication cookie name | `mangooio-auth` |
| `authentication.cookie.key` | JWT signing key; falls back to `application.secret` | — |
| `authentication.cookie.secret` | JWT encryption secret; falls back to `application.secret` | — |
| `authentication.cookie.remember.expires` | Remember-me lifetime in **seconds** | `2592000` |
| `authentication.cookie.samesitemode` | SameSite attribute | `Strict` |
| `authentication.cookie.secure` | Secure cookie flag | `false` |
| `authentication.cookie.token.expires` | Token and cookie lifetime in **seconds** | `3600` |
| `authentication.lock` | Failed logins before lockout | `10` |
| `authentication.origin` | Append `?origin=` on auth redirects | `false` |
| `authentication.redirect.login` | Redirect when authentication is missing | — |
| `authentication.redirect.mfa` | Redirect when MFA is required | — |
| `connector.http.host` | HTTP bind address | — |
| `connector.http.port` | HTTP port | — |
| `connector.https.host` | HTTPS bind address | — |
| `connector.https.port` | HTTPS port | — |
| `connector.https.certificate.alias` | Vault certificate alias | `certificate` |
| `cors.alloworigin` | Regex for `Access-Control-Allow-Origin` | `^http(s)?://(www\.)?example\.(com\|org)$` |
| `cors.enable` | Send CORS headers | `false` |
| `cors.headers.allowcredentials` | `Access-Control-Allow-Credentials` | `true` |
| `cors.headers.allowheaders` | `Access-Control-Allow-Headers` | `Authorization,Content-Type,Link,X-Total-Count,Range` |
| `cors.headers.allowmethods` | `Access-Control-Allow-Methods` | `DELETE,GET,HEAD,OPTIONS,PATCH,POST,PUT` |
| `cors.headers.exposeheaders` | `Access-Control-Expose-Headers` | `Accept-Ranges,Content-Length,Content-Range,ETag,Link,Server,X-Total-Count` |
| `cors.headers.maxage` | `Access-Control-Max-Age` | `864000` |
| `cors.urlpattern` | Regex of request URLs that receive CORS headers | `^http(s)?://([^/]+)(:([^/]+))?(/([^/])+)?/api(/.*)?$` |
| `flash.cookie.name` | Flash cookie name | `mangooio-flash` |
| `flash.cookie.key` | Flash JWT signing key | — |
| `flash.cookie.secret` | Flash JWT encryption secret | — |
| `i18n.cookie.name` | Locale cookie name | `mangooio-i18n` |
| `metrics.enable` | Collect request metrics for the admin dashboard | `false` |
| `otlp.enable` | Enable OpenTelemetry export | `false` |
| `otlp.endpoint` | OTLP gRPC endpoint | — |
| `persistence.enable` | Enable MongoDB persistence | `true` |
| `persistence.mongo.auth` | Use MongoDB authentication | `false` |
| `persistence.mongo.authdb` | MongoDB authentication database | — |
| `persistence.mongo.dbname` | MongoDB database name | `mangoo-io-mongodb` |
| `persistence.mongo.embedded` | Start embedded MongoDB | `false` |
| `persistence.mongo.host` | MongoDB host | `localhost` |
| `persistence.mongo.password` | MongoDB password | — |
| `persistence.mongo.port` | MongoDB port | `27017` |
| `persistence.mongo.username` | MongoDB username | — |
| `scheduler.enable` | Enable `@Run` scheduling | `true` |
| `session.cookie.expires` | Persist the session cookie beyond the browser session | `false` |
| `session.cookie.name` | Session cookie name | `mangooio-session` |
| `session.cookie.key` | Session JWT signing key | — |
| `session.cookie.secret` | Session JWT encryption secret | — |
| `session.cookie.samesitemode` | SameSite attribute | `Strict` |
| `session.cookie.secure` | Secure cookie flag (also used for flash) | `false` |
| `session.cookie.token.expires` | Session token lifetime in **seconds** | `3600` |
| `smtp.authentication` | Enable SMTP authentication | `false` |
| `smtp.debug` | Enable SMTP debug output | `false` |
| `smtp.from` | Default From address | `mangoo <noreply@mangoo.local>` |
| `smtp.host` | SMTP host | `localhost` |
| `smtp.password` | SMTP password | — |
| `smtp.port` | SMTP port | `25` |
| `smtp.protocol` | SMTP protocol | `smtps` |
| `smtp.username` | SMTP username | — |
| `undertow.maxentitysize` | Maximum HTTP entity size in bytes | `4194304` |
