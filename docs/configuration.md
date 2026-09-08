# Configuration ⚙️

Almost all runtime behavior is controlled through one YAML file: `src/main/resources/config.yaml`. mangoo I/O loads it with [SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml/src/master/), flattens the nested keys into dot notation, and exposes them through the `Config` class and Guice `@Named` bindings. There is deliberately no second configuration mechanism (no properties files, no XML, no `@ConfigurationProperties` classes to keep in sync); if a setting exists, it lives in this one file, which makes it realistic to actually read the whole configuration of an application in one sitting.

Typical uses include connector host and port, cookie names, MongoDB connection details, SMTP settings, feature toggles (admin UI, metrics, scheduler), and security options (CORS, authentication redirects). Sensitive values should reference the [vault](secrets.md), environment variables, or JVM properties rather than plain text in the repository, since `config.yaml` normally ends up committed to source control.

The file is split into a **`default`** block (shared by all modes) and an **`environments`** block with overrides for `dev`, `test`, and `prod`. When the application starts, it merges `default` with the section for the active mode, key by key, so a key you only set in `default` still applies everywhere, and a key you also set under `environments.prod` overrides it just for production. Missing an environment section for the active mode causes startup to fail on purpose. That may feel strict the first time it happens, but it exists to catch the case where someone adds a `staging` environment, forgets to add matching config, and the app would otherwise silently fall back to defaults in a place where nobody is watching.

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

Configuration values can also come from the application vault, environment variables, or JVM arguments instead of being written out in plain text. See [Secrets](secrets.md) for how `vault{}`, `env{}`, and `arg{}` placeholders are resolved.

As a rule of thumb: cookie signing keys, the vault password, and SMTP credentials should never sit in source control as clear text, since `config.yaml` is versioned right alongside your application code and gets the same read access as everything else in the repository.

## Connectors

At least one HTTP or HTTPS connector must be configured, otherwise the application refuses to start. There is no implicit default port, on purpose: a web framework that silently picks a port for you is more surprising than one that asks you to say so once.

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

HTTPS uses an SSL context built from the [vault](secrets.md). The certificate alias defaults to `certificate`, which only matters if you store more than one certificate in the same keystore.

## Default values

Keys that you omit fall back to the defaults below. Cells marked *(none)* have no built-in default, meaning the feature stays off or the value stays empty until you set it yourself. MongoDB keys are nested under `persistence` in YAML (`persistence.mongo.host`, and `persistence.<prefix>.mongo.host` for additional named datastores, see [Persistence](persistence.md)).

| Key | Description | Default |
|---|---|---|
| `application.admin.enable` | Enables the admin dashboard | `false` |
| `application.admin.locale` | Locale for the admin dashboard | `en_EN` |
| `application.admin.password` | Admin dashboard password | *(none)* |
| `application.admin.secret` | Admin TOTP secret; if set, MFA is required | *(none)* |
| `application.admin.username` | Admin dashboard username | *(none)* |
| `application.allowedOrigins` | Comma-separated origins for `OriginFilter` | *(none)* |
| `application.api.key` | Shared secret for `ApiKeyFilter` | *(none)* |
| `application.controller` | Controller package prefix | `controllers.` |
| `application.language` | Default application language | `en` |
| `application.name` | Application name (JWT issuer, logs) | `mangooio-application` |
| `application.named` | Example named Guice binding | *(none)* |
| `application.secret` | Application secret; fallback for cookie keys | *(none)* |
| `application.timezone` | Application timezone | `UTC` |
| `application.validation.passthrough` | Return Bean Validation errors as JSON instead of a rendered form | `false` |
| `application.vault.enable` | Enable the PKCS12 vault | *(none)* |
| `application.vault.path` | Directory of `vault.p12` in prod | *(none)* |
| `application.vault.secret` | Vault password (min. 64 characters) | *(none)* |
| `authentication.blacklist` | Enable authentication blacklist cache | `false` |
| `authentication.cookie.name` | Authentication cookie name | `mangooio-auth` |
| `authentication.cookie.key` | JWT signing key; falls back to `application.secret` | *(none)* |
| `authentication.cookie.secret` | JWT encryption secret; falls back to `application.secret` | *(none)* |
| `authentication.cookie.remember.expires` | Remember-me lifetime in **seconds** | `2592000` |
| `authentication.cookie.samesitemode` | SameSite attribute | `Strict` |
| `authentication.cookie.secure` | Secure cookie flag | `false` |
| `authentication.cookie.token.expires` | Token and cookie lifetime in **seconds** | `3600` |
| `authentication.lock` | Failed logins before lockout | `10` |
| `authentication.origin` | Append `?origin=` on auth redirects | `false` |
| `authentication.redirect.login` | Redirect when authentication is missing | *(none)* |
| `authentication.redirect.mfa` | Redirect when MFA is required | *(none)* |
| `connector.http.host` | HTTP bind address | *(none)* |
| `connector.http.port` | HTTP port | *(none)* |
| `connector.https.host` | HTTPS bind address | *(none)* |
| `connector.https.port` | HTTPS port | *(none)* |
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
| `flash.cookie.key` | Flash JWT signing key | *(none)* |
| `flash.cookie.secret` | Flash JWT encryption secret | *(none)* |
| `i18n.cookie.name` | Locale cookie name | `mangooio-i18n` |
| `metrics.enable` | Collect request metrics for the admin dashboard | `false` |
| `otlp.enable` | Enable OpenTelemetry export | `false` |
| `otlp.endpoint` | OTLP gRPC endpoint | *(none)* |
| `persistence.enable` | Enable MongoDB persistence | `true` |
| `persistence.mongo.auth` | Use MongoDB authentication | `false` |
| `persistence.mongo.authdb` | MongoDB authentication database | *(none)* |
| `persistence.mongo.dbname` | MongoDB database name | `mangoo-io-mongodb` |
| `persistence.mongo.embedded` | Start embedded MongoDB | `false` |
| `persistence.mongo.host` | MongoDB host | `localhost` |
| `persistence.mongo.password` | MongoDB password | *(none)* |
| `persistence.mongo.port` | MongoDB port | `27017` |
| `persistence.mongo.username` | MongoDB username | *(none)* |
| `scheduler.enable` | Enable `@Run` scheduling | `true` |
| `session.cookie.expires` | Persist the session cookie beyond the browser session | `false` |
| `session.cookie.name` | Session cookie name | `mangooio-session` |
| `session.cookie.key` | Session JWT signing key | *(none)* |
| `session.cookie.secret` | Session JWT encryption secret | *(none)* |
| `session.cookie.samesitemode` | SameSite attribute | `Strict` |
| `session.cookie.secure` | Secure cookie flag (also used for flash) | `false` |
| `session.cookie.token.expires` | Session token lifetime in **seconds** | `3600` |
| `smtp.authentication` | Enable SMTP authentication | `false` |
| `smtp.debug` | Enable SMTP debug output | `false` |
| `smtp.from` | Default From address | `mangoo <noreply@mangoo.local>` |
| `smtp.host` | SMTP host | `localhost` |
| `smtp.password` | SMTP password | *(none)* |
| `smtp.port` | SMTP port | `25` |
| `smtp.protocol` | SMTP protocol | `smtps` |
| `smtp.username` | SMTP username | *(none)* |
| `undertow.maxentitysize` | Maximum HTTP entity size in bytes | `4194304` |

A couple of these are worth calling out by name. `authentication.cookie.samesitemode` and `session.cookie.samesitemode` both default to `Strict`, which is the safest choice against CSRF but also means the cookie will not be sent on cross-site navigations at all (a link from another domain, for instance); loosen it to `Lax` if your login flow depends on that. `undertow.maxentitysize` caps request body size at 4 MiB by default, mostly to stop an accidental (or malicious) huge upload from eating memory before your controller even gets a chance to reject it; raise it deliberately if your application genuinely needs larger uploads.
