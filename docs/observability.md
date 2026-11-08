# Observability

Production systems need visibility beyond log lines. mangoo I/O exposes **request metrics** (counts and latency) for the admin dashboard and optional **OpenTelemetry** export for traces and metrics. Both are off by default so local development stays lightweight; enable them explicitly when you deploy.

The dashboard metrics answer "how much traffic and how slow?" for the instance that serves the page. OTLP sends spans and metrics out to a collector (Jaeger, Grafana Tempo, Prometheus via the OpenTelemetry Collector, vendor backends), where the data of all instances comes together. See [Operating](operating.md) for JVM and secrets setup alongside these features.

## Metrics

Set `metrics.enable` to `true` to start counting requests and recording processing time. Values show up on the [admin dashboard](administration.md). Metrics stay off by default so they never add overhead you did not ask for.

```yaml
metrics:
  enable: true
```

Requests whose URI contains `@admin` are excluded. Health probes never reach the counter, because they are handled before it. User SSE and WebSocket routes have no route template and are stored under their request path for the whole connection lifetime. At most 1000 route templates are kept. The rest are `Other routes`. Errors are status codes of 500 and above. Times on the dashboard are milliseconds.

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

Every request is recorded in the histogram `http.server.request.duration` (seconds, attributes `http.request.method` and `http.response.status_code`). Buckets run from 0.005 to 10 seconds. Health probes are outside the telemetry handler. A response whose `Content-Type` starts with `text/event-stream` is skipped. WebSocket exchanges are not. The span name is the raw request path, plus a child span `Invoke controller`. When `otlp.enable` is true, MongoDB commands are traced as well.

`service.name` is `application.name` (default `mangooio-application`). `framework.environment` is `PROD`, `DEV`, or `TEST`. Export uses the SDK default interval of the periodic reader, 60 seconds in the OpenTelemetry Java version this build uses, and once more on shutdown. Leave `otlp.enable` false unless a collector is listening. The endpoint is gRPC, for example `http://127.0.0.1:4317`.

For load balancer and orchestrator probes use `/@health/live` and `/@health/ready`, see [Operating](operating.md#health-checks). If you need a MongoDB ping for your own checks, call `Datastore.isHealthy()` directly.
