package io.mangoo.controllers;

import com.google.inject.Inject;

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

    public Response health() {
        return Response.withOk()
                .andTextBody("alive")
                .andEtag();
    }

    public Response routes() {
        if (!Application.inDevMode() && !config.isAdministrationEnabled()) {
            return Response.withNotFound();
        }

        return Response.withOk()
                .andContent("routes", Router.getRoutes())
                .andTemplate("defaults/routes.ftl")
                .andEtag();
    }

    public Response config() {
        if (!Application.inDevMode() && !config.isAdministrationEnabled()) {
            return Response.withNotFound();
        }

        return Response.withOk()
                .andContent("configuration", config.getAllConfigurations())
                .andTemplate("defaults/config.ftl")
                .andEtag();
    }
}