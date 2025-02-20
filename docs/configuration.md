mangoo I/O relies on one configuration file for your application which is based on [Jodd Props](https://jodd.org/props/). The properties based config.props file is located in the src/main/resources folder, along with all other files that are not Java classes. You can add and customize settings by simply adding a value in the config.props file, for example

```properties
[application]
	name = myValue
```

There is a number of default property values which configure a mangoo I/O application. See [default values](https://docs.mangoo.io/default-values.html), for all configuration options and there default values.

Config values are accessed in your application with a dot-notation. If you have a config value like

```properties
[application]
	minify.js = true
        minify.css = true
```

this would be accessible by the following keys in your application

```properties
application.minify.js
application.minify.css
```

To access configuration values you have two options for retrieving the Config class. You can either inject the Config class via constructor or member variable.

Injection via member variable

```java
@Inject
private Config config;
```

Injection via constructor variable (recommended)

```java
@Inject
private MyClass(Config config) {
    //do something
}
```

You can access a configuration value, either by a given key or predefined default keys from mangoo I/O.

```java
config.getString("application.minify.js");
config.getString(Key.APPLICATION_MINIFY_JS);
```

## Modes

By convention, mangoo I/O offers three configuration modes: dev, test and prod. The dev mode is automatically activated when you start your mangoo I/O application with the Maven plugin for local development.

```bash
mvn mangooio:run
```

The test mode is automatically activated when executing unit test and the prod mode is activated by default when no other mode is given. You can overwrite this programatically, by setting a system property

```bash
System.setProperty("application.mode", "dev");
```

or by passing a system property to the executable JAR

```bash
... -Dapplication.mode=dev
```

**Mode specific configuration**

You can create mode specific configuration by prefixing a configuration value.

```properties
[application]
	connector.host = localhost
    connector.port = 8080

[application<test>]
	connector.host = localhost
    connector.port = 10808

[application<dev>]
	connector.host = localhost
    connector.port = 10909
```

By default mangoo I/O uses sections for mode specific configuration. If no mode specific value is found, mangoo I/O will lookup the default configuration.

## Encrypted values

All configuration values in the config.props file can be encrypted using public/private key encryption. The values will be decrypted once the application starts an will be kept decrypted in-memory as long as the application runs.

In order to use encryption of config values you need generate a public/private key pair. This can be done via the mangoo I/O administrative backend which offers configuration tools. 

Having generated a keypair you can encrypt your config values via the administrative interface and set them in your configuration file accordingly.

The private key for decryption needs to be either set via a JVM argument referencing a absolute file path.

```bash
... -Dapplication.privatekey=/path/to/privatekey
```

To place an encrypted value in the config.props file you have to set the encryption value into a specific pattern, as follows:

```properties
[application]
	db.username = cryptex{...}
    db.password = cryptex{...}
```

Please note, that there is not "t" and the end as this is related to [Cryptex](https://en.wikipedia.org/wiki/Cryptex).

It is recommended to use the build-in encryption functions to create the encrypted values, see [admin interface tools page](https://docs.mangoo.io/administration.html).

## Default values

This is an overview of the out-of-the box configuration options for the application.yaml and their default values, if the properties are not configured in the application.yaml file.

| Key | Value | Default Value |
| ------------- | ------------ | ------------- |
| api.key |  |  |
| application.admin.enable |  |  |
| application.admin.password |  |  |
| application.admin.secret |  |  |
| application.admin.username |  |  |
| application.admin.locale |  |  |
| application.config |  |  |
| application.controller |  |  |
| application.language |  |  |
| application.mode |  |  |
| application.name |  |  |
| application.named |  |  |
| application.privatekey |  |  |
| application.publickey |  |  |
| application.secret |  |  |
| authentication.cookie.expires |  |  |
| authentication.cookie.name |  |  |
| authentication.cookie.remember.expires |  |  |
| authentication.cookie.secret |  |  |
| authentication.cookie.secure |  |  |
| authentication.cookie.token.expires |  |  |
| authentication.lock |  |  |
| authentication.redirect.login |  |  |
| authentication.redirect.mfa |  |  |
| authentication.origin |  |  |
| connector.ajp.host |  |  |
| connector.ajp.port |  |  |
| connector.http.host |  |  |
| connector.http.port |  |  |
| cors.alloworigin |  |  |
| cors.enable |  |  |
| cors.headers.allowcredentials |  |  |
| cors.headers.allowheaders |  |  |
| cors.headers.allowmethods |  |  |
| cors.headers.exposeheaders |  |  |
| cors.headers.maxage |  |  |
| cors.urlpattern |  |  |
| flash.cookie.name |  |  |
| flash.cookie.secret |  |  |
| i18n.cookie.name |  |  |
| MANGOOIO-WARNINGS |  |  |
| metrics.enable |  |  |
| paseto.secret |  |  |
| persistence.enable |  |  |
| mongo.auth |  |  |
| mongo.authdb |  |  |
| mongo.dbname |  |  |
| mongo.embedded |  |  |
| mongo.host |  |  |
| mongo.password |  |  |
| mongo.port |  |  |
| mongo.username |  |  |
| scheduler.enable |  |  |
| session.cookie.expires |  |  |
| session.cookie.name |  |  |
| session.cookie.secret |  |  |
| session.cookie.secure |  |  |
| session.cookie.token.expires |  |  |
| smtp.authentication |  |  |
| smtp.debug |  |  |
| smtp.from |  |  |
| smtp.host |  |  |
| smtp.password |  |  |
| smtp.port |  |  |
| smtp.protocol |  |  |
| smtp.username |  |  |
| undertow.maxentitysize |  |  |

## JVM arguments

mangoo I/O offers the option to pass any configuration value as JVM arugment. In order to use this functionality, you need to set a specific value at the property in the config.props configuration file. See the following example:

```properties
[application]
	foo.bar = ${arg} 
```

This will tell mangoo I/O to look for a JVM argument in the form of 

```bash
-Dapplication.foo.bar=foobar
```

Retrieving a config value from a JVM argument will overwrite any other configuration value, as the JVM arguments have the highest priority.

## Custom configuration

If you require a custom configuration for quartz inside mangoo I/O you can use the application.yaml to pass any option to quartz. Simply add the configuration option with the appropriate prefix org.quartz.

```properties
org.quartz.jobStore.class = com.novemberain.quartz.mongodb.MongoDBJobStore
org.quartz.jobStore.uri = mognodb://localhost:27017
org.quartz.jobStore.dbName = quartz
```

Check out the [Quartz Scheudler configuration documentation](http://quartz-scheduler.org/generated/2.2.2/html/qs-all/#page/Quartz_Scheduler_Documentation_Set%2F_qs_all.1.041.html%23) for more information.