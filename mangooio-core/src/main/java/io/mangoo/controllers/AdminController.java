package io.mangoo.controllers;

import java.util.HashMap;
import java.util.Map;

import com.google.common.cache.CacheStats;
import com.google.inject.Inject;

import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Key;
import io.mangoo.routing.Response;
import io.mangoo.routing.Router;

/**
 *
 * @author svenkubiak
 *
 */
public class AdminController {

    @Inject
    private Config config;

    @Inject
    private Cache cache;

    public Response health() {
        if (!Application.inDevMode() && !config.isAdminHealthEnabled()) {
            return Response.withNotFound();
        }

        return Response.withOk()
                .andTextBody("alive")
                .andEtag();
    }

    public Response routes() {
        if (!Application.inDevMode() && !config.isAdminRoutesEnabled()) {
            return Response.withNotFound();
        }

        return Response.withOk()
                .andContent("routes", Router.getRoutes())
                .andTemplate("defaults/routes.ftl")
                .andEtag();
    }

    public Response cache() {
        if (!Application.inDevMode() && !config.isAdminCacheEnabled()) {
            return Response.withNotFound();
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
                .andTemplate("defaults/cache.ftl")
                .andEtag();
    }

    public Response config() {
        if (!Application.inDevMode() && !config.isAdminConfigEnabled()) {
            return Response.withNotFound();
        }

        Map<String, String> configurations = config.getAllConfigurations();
        configurations.remove(Key.APPLICATION_SECRET.toString());
        configurations.remove(Key.SMTP_USERNAME.toString());
        configurations.remove(Key.SMTP_PASSWORD.toString());

        return Response.withOk()
                .andContent("configuration", configurations)
                .andTemplate("defaults/config.ftl")
                .andEtag();
    }
}