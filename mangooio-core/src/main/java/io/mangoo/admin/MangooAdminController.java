package io.mangoo.admin;

import java.util.HashMap;
import java.util.Map;

import com.google.common.cache.CacheStats;
import com.google.inject.Inject;

import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Key;
import io.mangoo.enums.Template;
import io.mangoo.models.Metrics;
import io.mangoo.routing.Response;
import io.mangoo.routing.Router;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooAdminController {
    private Config config;
    private Cache cache;

    @Inject
    public MangooAdminController(Config config, Cache cache) {
        this.config = config;
        this.cache = cache;
    }

    public Response health() {
        if (!Application.inDevMode() && !config.isAdminHealthEnabled()) {
            return notFound();
        }

        return Response.withOk()
                .andTextBody("alive");
    }

    public Response routes() {
        if (!Application.inDevMode() && !config.isAdminRoutesEnabled()) {
            return notFound();
        }

        return Response.withOk()
                .andContent("routes", Router.getRoutes())
                .andTemplate("defaults/routes.ftl");
    }

    public Response cache() {
        if (!Application.inDevMode() && !config.isAdminCacheEnabled()) {
            return notFound();
        }

        CacheStats cacheStats = cache.getStats();

        Map<String, Object> stats = new HashMap<String, Object>();
        stats.put("Average load penalty", cacheStats.averageLoadPenalty());
        stats.put("Eviction count", cacheStats.evictionCount());
        stats.put("Hit count", cacheStats.hitCount());
        stats.put("Hit rate", cacheStats.hitRate());
        stats.put("Load count", cacheStats.loadCount());
        stats.put("Load exception count", cacheStats.loadExceptionCount());
        stats.put("Load exception rate", cacheStats.loadExceptionRate());
        stats.put("Load success rate", cacheStats.loadSuccessCount());
        stats.put("Miss count", cacheStats.missCount());
        stats.put("Request count", cacheStats.requestCount());
        stats.put("Total load time in ns", cacheStats.totalLoadTime());

        return Response.withOk()
                .andContent("stats", stats)
                .andTemplate("defaults/cache.ftl");
    }

    public Response config() {
        if (!Application.inDevMode() && !config.isAdminConfigEnabled()) {
            return notFound();
        }

        Map<String, String> configurations = config.getAllConfigurations();
        configurations.remove(Key.APPLICATION_SECRET.toString());

        return Response.withOk()
                .andContent("configuration", configurations)
                .andTemplate("defaults/config.ftl");
    }

    public Response metrics() {
        if (!Application.inDevMode() && !config.isAdminConfigEnabled()) {
            return notFound();
        }

        Metrics metrics = Application.getInjector().getInstance(Metrics.class);
        return Response.withOk()
                .andContent("metrics", metrics.getMetrics())
                .andTemplate("defaults/metrics.ftl");
    }

    private Response notFound() {
        return Response.withNotFound().andBody(Template.DEFAULT.notFound());
    }
}