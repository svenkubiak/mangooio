# Administration

mangoo I/O includes a built-in **administrative interface**, providing access to framework data such as **metrics, cache, scheduler, and logger configuration**.

## Enabling the Administrative Interface

By default, the administrative interface is **disabled**. To enable it, update the `config.yaml` file:

```yaml
application:
  admin:
    enable: true
```

## Authentication

Access to the interface is secured with **HTTP Basic Authentication**. The credentials must be specified in the `config.yaml` file:

```yaml
application:
  admin:
    username: admin
    password: admin
```

## Accessing the Interface

Once enabled, the administrative interface is accessible at:

```properties
<host>:<port>/@admin
```

## Enabling Metrics

The **admin interface** includes request counting and process time calculation metrics. By default, metrics are **disabled**. To enable them, modify `config.yaml`:

```properties
metrics:
  enable: true
```

This ensures that system performance and request handling statistics are available through the administrative dashboard.
