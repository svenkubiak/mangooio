package io.mangoo.admin;

import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheProvider;
import io.mangoo.constants.CacheName;
import io.mangoo.constants.Key;
import io.mangoo.constants.Required;
import io.mangoo.interfaces.MessageBroker;
import io.mangoo.models.Metrics;
import io.mangoo.utils.JsonUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Runs the actions of the admin dashboard on every instance. An action is published through the MessageBroker, so with
 * persistence it reaches all instances, without persistence only this one.
 */
@Singleton
public class AdminActions {
    private static final Logger LOG = LogManager.getLogger(AdminActions.class);
    private static final String TOPIC = "mangooio-admin";
    private static final String CLEAR_CACHE = "clear-cache";
    private static final String RESET_METRICS = "reset-metrics";
    private final MessageBroker messageBroker;
    private final CacheProvider cacheProvider;
    private final Metrics metrics;

    record Action(String type, String cache) {
    }

    @Inject
    public AdminActions(MessageBroker messageBroker, CacheProvider cacheProvider, Metrics metrics) {
        this.messageBroker = Objects.requireNonNull(messageBroker, "messageBroker can not be null");
        this.cacheProvider = Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER);
        this.metrics = Objects.requireNonNull(metrics, "metrics can not be null");
        messageBroker.subscribe(TOPIC, json -> execute(JsonUtils.toObject(json, Action.class)));
    }

    public void clearCache(String name) {
        Objects.requireNonNull(name, Required.NAME);
        messageBroker.publish(TOPIC, JsonUtils.toJson(new Action(CLEAR_CACHE, name)));
    }

    public void resetMetrics() {
        messageBroker.publish(TOPIC, JsonUtils.toJson(new Action(RESET_METRICS, null)));
    }

    void execute(Action action) {
        if (action == null || action.type() == null) {
            return;
        }

        switch (action.type()) {
            case CLEAR_CACHE -> clear(action.cache());
            case RESET_METRICS -> metrics.reset();
            default -> LOG.warn("Ignoring unknown admin action '{}'", action.type());
        }
    }

    private void clear(String name) {
        Cache cache = name == null ? null : cacheProvider.getCaches().get(name);
        if (cache == null) {
            return;
        }

        // The configuration warnings of the dashboard live in the application cache and are only collected at startup
        Object warnings = CacheName.APPLICATION.equals(name) ? cache.get(Key.MANGOOIO_WARNINGS) : null;
        cache.clear();
        if (warnings != null) {
            cache.put(Key.MANGOOIO_WARNINGS, warnings);
        }
    }
}
