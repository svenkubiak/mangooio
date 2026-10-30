package io.mangoo.admin;

import io.mangoo.async.EventBus;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.interfaces.InstanceRegistry;
import io.mangoo.models.InstanceStatus;
import io.mangoo.models.Metrics;
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.FileUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.LongAdder;

/** The figures of the dashboard that change while it is open; rendered by the page and pushed by the AdminStream. */
@Singleton
public class Dashboard {
    private static final double HUNDRED_PERCENT = 100.0;
    private static final long STALE_SECONDS = 30;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final Config config;
    private final InstanceHeartbeat instanceHeartbeat;
    private final InstanceRegistry instanceRegistry;
    private final Metrics metrics;

    @Inject
    public Dashboard(Config config, InstanceHeartbeat instanceHeartbeat, InstanceRegistry instanceRegistry, Metrics metrics) {
        this.config = Objects.requireNonNull(config, Required.CONFIG);
        this.instanceHeartbeat = Objects.requireNonNull(instanceHeartbeat, "instanceHeartbeat can not be null");
        this.instanceRegistry = Objects.requireNonNull(instanceRegistry, "instanceRegistry can not be null");
        this.metrics = Objects.requireNonNull(metrics, "metrics can not be null");
    }

    public Map<String, Object> getContent() {
        var eventBus = Application.getInstance(EventBus.class);
        InstanceStatus current = instanceHeartbeat.current();
        List<Map<String, Object>> instances = getInstances(current);

        Map<String, Object> content = new HashMap<>();
        content.put("enabled", config.isMetricsEnable());
        content.put("uptime", DateUtils.getPrettyTime(Application.getStart()));
        content.put("events", eventBus.getHandledEvents());
        content.put("failedEvents", eventBus.getFailedEvents());
        content.put("connections", current.connections());
        content.put("instances", instances);
        content.put("instanceTotals", getTotals(instances));
        content.put("shared", config.isPersistenceEnabled());
        content.put("system", getSystem(current));
        if (config.isMetricsEnable()) {
            content.putAll(getMetrics());
        }

        return content;
    }

    private Map<String, Object> getMetrics() {
        Map<String, Long> statusCodes = new TreeMap<>();
        Map<String, Long> statusClasses = new TreeMap<>(Map.of("2xx", 0L, "3xx", 0L, "4xx", 0L, "5xx", 0L));
        for (Entry<Integer, LongAdder> entry : metrics.getResponseMetrics().entrySet()) {
            long count = entry.getValue().longValue();
            statusCodes.put(String.valueOf(entry.getKey()), count);
            statusClasses.merge(entry.getKey() / 100 + "xx", count, Long::sum);
        }

        long totalRequests = statusCodes.values().stream().mapToLong(Long::longValue).sum();
        long errorRequests = statusClasses.get("5xx");
        Metrics.Window window = metrics.getWindow();

        Map<String, Object> content = new HashMap<>();
        content.put("metrics", statusCodes);
        content.put("statusClasses", statusClasses);
        content.put("totalRequests", totalRequests);
        content.put("avgRequestTime", metrics.getAvgRequestTime());
        content.put("maxRequestTime", metrics.getMaxRequestTime());
        content.put("errorRate", errorRequests > 0 ? (HUNDRED_PERCENT / totalRequests) * errorRequests : 0);
        content.put("recentRequests", window.requests());
        content.put("recentErrorRate", window.requests() == 0 ? 0 : (HUNDRED_PERCENT / window.requests()) * window.errors());
        content.put("recentAvgRequestTime", window.requests() == 0 ? -1 : window.avgRequestTime());
        content.put("recentMaxRequestTime", window.maxRequestTime());

        return content;
    }

    private static Map<String, Object> getSystem(InstanceStatus current) {
        Map<String, Object> system = new HashMap<>();
        system.put("started", Application.getStart().format(DATE_TIME_FORMATTER));
        system.put("java", Runtime.version().toString());
        system.put("heapUsed", current.heapUsed());
        system.put("heapMax", current.heapMax());
        system.put("heapUsedReadable", FileUtils.readableFileSize(current.heapUsed()));
        system.put("heapMaxReadable", FileUtils.readableFileSize(current.heapMax()));

        return system;
    }

    // The status of this instance is always the current one, the others are those of their last heartbeat
    private List<Map<String, Object>> getInstances(InstanceStatus current) {
        Map<String, InstanceStatus> statuses = new TreeMap<>();
        instanceRegistry.getInstances().forEach(status -> statuses.put(status.id(), status));
        statuses.put(current.id(), current);

        var now = Instant.now();
        List<Map<String, Object>> instances = new ArrayList<>();
        for (InstanceStatus status : statuses.values()) {
            long seen = Math.max(0, Duration.between(status.seen(), now).toSeconds());
            Map<String, Object> instance = new HashMap<>();
            instance.put("id", status.id());
            instance.put("current", status.id().equals(current.id()));
            instance.put("version", status.version());
            instance.put("started", DateUtils.getPrettyTime(LocalDateTime.ofInstant(status.started(), ZoneId.systemDefault())));
            instance.put("seen", seen);
            instance.put("stale", seen > STALE_SECONDS);
            instance.put("heap", FileUtils.readableFileSize(status.heapUsed()) + " / " + FileUtils.readableFileSize(status.heapMax()));
            instance.put("connections", status.connections());
            instance.put("requests", status.requests());
            instance.put("errors", status.errors());
            instance.put("avgRequestTime", status.requests() == 0 ? -1 : status.avgRequestTime());
            instance.put("failedEvents", status.failedEvents());
            instances.add(instance);
        }

        return instances;
    }

    private static Map<String, Long> getTotals(List<Map<String, Object>> instances) {
        Map<String, Long> totals = new HashMap<>();
        for (String key : List.of("connections", "requests", "errors", "failedEvents")) {
            totals.put(key, instances.stream().mapToLong(instance -> ((Number) instance.get(key)).longValue()).sum());
        }

        return totals;
    }
}
