# Observability

Production systems need visibility beyond log lines. mangoo I/O exposes **request metrics** (counts and latency) for the admin dashboard and optional **OpenTelemetry** export for traces. Both are off by default so local development stays lightweight; enable them explicitly when you deploy.

Metrics answer "how much traffic and how slow?" from inside the app itself. OTLP instead sends spans out to a collector (Jaeger, Grafana Tempo, vendor backends) for distributed tracing across services. See [Operating](operating.md) for JVM and vault setup alongside these features.

## Metrics

Set `metrics.enable` to `true` to start counting requests and recording processing time. Values show up on the [admin dashboard](administration.md). Metrics stay off by default so they never add overhead you did not ask for.

```yaml
metrics:
  enable: true
```

Admin UI traffic is excluded from the counters.

## OpenTelemetry

mangoo I/O can export traces over OTLP/gRPC:

```yaml
otlp:
  enable: true
  endpoint: http://127.0.0.1:4317
```

When enabled, spans go out under the service name `io.mangoo` along with the current application mode. Leave `otlp.enable` false unless you actually run a collector to receive them.

There is no public `/health` HTTP endpoint. If you need a MongoDB ping for your own health checks, call `Datastore.isHealthy()` directly.
