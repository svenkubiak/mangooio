mangoo I/O comes with a build in administrative interface, which enables you acces to certain data of the Framework like metrics, cache, scheduler, logger configuration, etc.

The administrative interface is disabled by default and needs to be enabled through the following configuration value in the config.props file.

```properties
[application]
	admin.enable = true
```

The administrative interface is protected by a HTTP Basic authentication. The credentials for this authentication have to be set in the config.props fileas follows

```properties
[application]
	admin.username: admin
	amidn.password: admin
```

Once enabled, the administrative interface is available under the following URL

```properties
<host>:<port>/@admin
```

The admin interfaces also display the metrics of you application, by counting the requests and calculating the process time. Metrics are not enabled by default. In order to enable metrics, set the following config value.

```properties
[metrics]
	enable = true
```

The admin interfaces also contain a JSON based health check which gives you information on the application health. When the admin interfaces are enabled, you can fetch the health information from the following URL:

```properties
<host>:<port>/@admin/health
```

This URL requires also authentication via the previously defined Basic HTTP authentication.
