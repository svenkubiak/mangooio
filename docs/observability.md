# Observability

Production systems need visibility beyond log lines. mangoo I/O exposes **request metrics** (counts and latency) for the admin dashboard and optional **OpenTelemetry** export for traces and metrics. Both are off by default so local development stays lightweight; enable them explicitly when you deploy.

The dashboard metrics answer "how much traffic and how slow?" for the instance that serves the page. OTLP sends spans and metrics out to a collector (Jaeger, Grafana Tempo, Prometheus via the OpenTelemetry Collector, vendor backends), where the data of all instances comes together. See [Operating](operating.md) for JVM and secrets setup alongside these features.

## Metrics

Set `metrics.enable` to `true` to start counting requests and recording processing time. Values show up on the [admin dashboard](administration.md). Metrics stay off by default so they never add overhead you did not ask for.

```yaml
metrics:
  enable: true
```

Admin UI traffic is excluded from the counters.

## OpenTelemetry

mangoo I/O can export traces and metrics over OTLP/gRPC:

```yaml
otlp:
  enable: true
  endpoint: http://127.0.0.1:4317
```

When enabled, spans and metrics go out with these resource attributes:

| Attribute | Value |
|---|---|
| `service.name` | `application.name` |
| `service.instance.id` | the instance id, hostname with a random suffix |
| `framework.name`, `framework.version` | `io.mangoo` and its version |
| `framework.environment` | the application mode |

Every request is recorded in the histogram `http.server.request.duration` (seconds, attributes `http.request.method` and `http.response.status_code`) as defined by the OpenTelemetry semantic conventions; health probes and server sent event streams are not. Metrics are exported every 60 seconds and once more on shutdown. Leave `otlp.enable` false unless you actually run a collector to receive them.

For load balancer and orchestrator probes use `/@health/live` and `/@health/ready`, see [Operating](operating.md#health-checks). If you need a MongoDB ping for your own checks, call `Datastore.isHealthy()` directly.
