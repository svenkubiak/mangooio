# Configuration

Mangoo I/O relies on a single configuration file for your application, based on [SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml/src/master/). The `config.yaml` file is located in the `src/main/resources` folder, along with other non-Java files. You can customize settings by adding values to `config.yaml`. For example:

```yaml
application:
  foo: bar
```

Mangoo I/O provides a set of default property values that configure the application. See [default values](https://docs.mangoo.io/default-values.html) for a full list of configuration options and their defaults.

## Accessing Configuration Values

Configuration values are accessed using dot notation based on their hierarchy. For example:

```yaml
application:
  api:
    key: foo
```

This value can be accessed using:

```java
config.getString("application.api.key");
```

### Injecting the Config Class

You can retrieve configuration values by injecting the `Config` class in two ways:

#### Member Variable Injection
```java
@Inject
private Config config;
```

#### Constructor Injection (Recommended)
```java
@Inject
private MyClass(Config config) {
    // Use config
}
```

You can retrieve configuration values either by specifying a key or using predefined constants from Mangoo I/O:
```java
config.getString("application.minify.js");
config.getString(Key.APPLICATION_MINIFY_JS);
```

## Configuration Modes

Mangoo I/O offers three configuration modes: **dev**, **test**, and **prod**.

- **Dev mode** is activated automatically when starting the application using the Maven plugin:
  ```shell
  mvn mangooio:run
  ```
- **Test mode** is activated during unit tests.
- **Prod mode** is the default if no other mode is specified.

To manually set a mode, use:

```shell
System.setProperty("application.mode", "dev");
```

Or pass it as a JVM argument:

```shell
... -Dapplication.mode=dev
```

### Mode-Specific Configuration

You can define mode-specific settings by prefixing configuration values:

```yaml
default:
  application:
    name: foo
    url: http://localhost

environments:
  test:
    application:
      name: foo
      url:  https://test.mydomain.com

  prod:
    application:
      name: foo
      url:  https://mydomain.com
```

By default, Mangoo I/O uses values from the `default` section, which are overridden by environment-specific values when the corresponding mode is active.

## Encrypted Configuration Values

Configuration values in `config.yaml` can be encrypted using public/private key encryption. Encrypted values are decrypted at runtime and stored in-memory.

To use encryption, generate a key pair via the Mangoo I/O administrative backend. Once generated, you can encrypt configuration values and set them in `config.yaml` as follows:

```yaml
application:
  db:
    username: cryptex{...}
    password: cryptex{...}
```

The private key must be provided as a JVM argument:

```shell
... -Dapplication.privatekey=/path/to/privatekey
```

**Note:** The encryption prefix is `cryptex{}`, without a trailing "t", based on the [Cryptex](https://en.wikipedia.org/wiki/Cryptex) concept.

## Passing JVM Arguments

JVM arguments can be used in configuration values by using the `arg{}` syntax:

```yaml
application:
  db:
    username: arg{}
```

This configuration will use the corresponding JVM argument if provided:

```shell
... -Dapplication.db.username=myusername
```

You can also specify default values:

```yaml
application:
  db:
    username: arg{defaultuser}
```

## Default Values

This is an overview of the out-of-the box configuration options for the config.yaml and their default values, if the properties are not configured in the config.yaml file.

| Key                                    | Description                                                                                                  | Default Value                                                             |
|----------------------------------------|--------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| application.admin.enable               | Activates the admin dashboard                                                                                | false                                                                     |
| application.admin.password             | Password for the admin dashboard                                                                             | -                                                                         |
| application.admin.secret               | Secret for the admin dashboard. If configured, enabled MFA for the admin dashboard                           | -                                                                         |
| application.admin.username             | Username for the admin dashboard                                                                             | -                                                                         |
| application.admin.locale               | Locale for the admin dashoard                                                                                | en_EN                                                                     |
| application.api.key                    | API key for the build-in ApiKeyFilter                                                                        | -                                                                         |
| application.controller                 | Package name where the controller classes are located                                                        | controllers.                                                              |
| application.language                   | Language of the application                                                                                  | en                                                                        |
| application.name                       | Name of the application                                                                                      | mangooio-application                                                      |
| application.paseto.secret              | Secret for the build-in PasetoFilter                                                                         | -                                                                         |
| application.secret                     | Default application secret                                                                                   | -                                                                         |
| authentication.cookie.expires          | Activates that the cookie has a defined lifetime, otherwise the cookie is only valid for the browser session | false                                                                     |
| authentication.cookie.name             | Name of the authentication cookie                                                                            | mangooio-authentication                                                   |
| authentication.cookie.remember.expires | Lifetime of the cookie in hours if remember me is activated                                                  | 720                                                                       |
| authentication.cookie.secret           | Secret for the authentication cookie                                                                         | -> application.secret value                                                                         |
| authentication.cookie.secure           | Set the secure attribute of the authentication cookie                                                        | false                                                                     |
| authentication.cookie.token.expires    | Lifetime of the token and the cookie in minutes                                                              | 60                                                                        |
| authentication.lock                    | Number of attemps after an account gets locked; Only valid if build-in authentication is used                | 10                                                                        |
| authentication.redirect.login          | Redirect URL for the login page when build-in authentication is used                                         | -                                                                         |
| authentication.redirect.mfa            | Redirect URL for the MFA page when build-in authentication is used                                           | -                                                                         |
| authentication.origin                  | Activates that an "?origin=" parameter with the request URL is added during authentication                   | false                                                                     |
| connector.ajp.host                     | AJP host                                                                                                     | -                                                                         |
| connector.ajp.port                     | AJP port                                                                                                     | 0                                                                         |
| connector.http.host                    | HTTP host                                                                                                    | -                                                                         |
| connector.http.port                    | HTTP port                                                                                                    | -                                                                         |
| cors.alloworigin                       | Header value for Access-Control-Allow-Origin                                                                 | ^http(s)?://(www\.)?example\.(com                                         |org)$                                                                          |
| cors.enable                            | Activate sending of CORS headers                                                                             | false                                                                     |
| cors.headers.allowcredentials          | Header value for Access-Control-Allow-Credentials                                                            | true                                                                      |
| cors.headers.allowheaders              | Header value for Access-Control-Allow-Headers                                                                | Authorization,Content-Type,Link,X-Total-Count,Range                       |
| cors.headers.allowmethods              | Header value for Access-Control-Allow-Methods                                                                | DELETE,GET,HEAD,OPTIONS,PATCH,POST,PUT                                    |
| cors.headers.exposeheaders             | Header value for Access-Control-Expose-Headers                                                               | Accept-Ranges,Content-Length,Content-Range,ETag,Link,Server,X-Total-Count |
| cors.headers.maxage                    | Header value for Access-Control-Max-Age                                                                      | 864000                                                                    |
| cors.urlpattern                        | Regex pattern on which the CORS headers should be checked against                                            | ^http(s)?://([^/]+)(:([^/]+))?(/([^/])+)?/api(/.*)?$                      |
| flash.cookie.name                      | Name of the flash cookie                                                                                     | mangooio-flash                                                            |
| flash.cookie.secret                    | Secret for the flash cookie                                                                                  | -> application.secret value                                                                         |
| i18n.cookie.name                       | Name of the i18n cookie                                                                                      | mangooio-i18n                                                             |
| metrics.enable                         | Activates collecting metrics which are shown in the admin dashboard                                          | false                                                                     |
| persistence.enable                     | Activates default persistence with MongoDB                                                                   | true                                                                      |
| mongo.auth                             | Activates MongoDB authentication                                                                             | false                                                                     |
| mongo.authdb                           | Name of the MongoDB AuthDB                                                                                   | -                                                                         |
| mongo.dbname                           | Name of the MongoDB database                                                                                 | mangoo-io-mongodb                                                         |
| mongo.embedded                         | Activates the build-in in-memory MongoDB                                                                     | false                                                                     |
| mongo.host                             | MongoDB host                                                                                                 | localhost                                                                 |
| mongo.password                         | MongoDB password                                                                                             | -                                                                         |
| mongo.port                             | MongoDB port                                                                                                 | 27017                                                                     |
| mongo.username                         | MongoDB username                                                                                             | -                                                                         |
| scheduler.enable                       | Activates the build-in scheduker                                                                             | true                                                                      |
| session.cookie.expires                 | Activates that the cookie has a defined lifetime, otherwise the cookie is only valid for the browser session | false                                                                     |
| session.cookie.name                    | Name of the seesion cookie                                                                                   | mangooio-session                                                          |
| session.cookie.secret                  | Secret of the seesion cookie                                                                                 | -> application.secret value                                   |
| session.cookie.secure                  | Set the secure attribute of the session cookie                                                               | -                                                                         |
| session.cookie.token.expires           | Lifetime of the token and the cookie in minutes                                                              | 60                                                                        |
| smtp.authentication                    | Axtivates SMTP authentication                                                                                | false                                                                     |
| smtp.debug                             | Acticates SMTP debugging                                                                                     | false                                                                     |
| smtp.from                              | SMTP from address                                                                                            | mangoo <noreply@mangoo.local>                                             |
| smtp.host                              | SMTP host address                                                                                            | localhost                                                                 |
| smtp.password                          | SMTP password                                                                                                | -                                                                         |
| smtp.port                              | SMTP port                                                                                                    | 25                                                                        |
| smtp.protocol                          | SMTP protocol                                                                                                | smtps                                                                     |
| smtp.username                          | SMTP username                                                                                                | -                                                                         |
| undertow.maxentitysize                 | Maximum size of an HTTP request entity (body)                                                                | 4194304 byte                                                              |