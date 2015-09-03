package io.mangoo.controllers;

import java.util.HashMap;
import java.util.Map;

import com.google.common.cache.CacheStats;
import com.google.inject.Inject;

import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
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
        stats.put("average load penalty", cacheStats.averageLoadPenalty());
        stats.put("eviction count", cacheStats.evictionCount());
        stats.put("hit count", cacheStats.hitCount());
        stats.put("hit rate", cacheStats.hitRate());
        stats.put("load count", cacheStats.loadCount());
        stats.put("load exception count", cacheStats.loadExceptionCount());
        stats.put("load exception rate", cacheStats.loadExceptionRate());
        stats.put("load success rate", cacheStats.loadSuccessCount());
        stats.put("miss count", cacheStats.missCount());
        stats.put("request count", cacheStats.requestCount());
        stats.put("total load time", cacheStats.totalLoadTime());

        return Response.withOk()
                .andContent("stats", stats)
                .andTemplate("defaults/cache.ftl")
                .andEtag();
    }

    public Response config() {
        if (!Application.inDevMode() && !config.isAdminConfigEnabled()) {
            return Response.withNotFound();
        }

        return Response.withOk()
                .andContent("configuration", config.getAllConfigurations())
                .andTemplate("defaults/config.ftl")
                .andEtag();
    }
}