package io.mangoo.enums;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * Default HTML templates for standard HTTP responses
 *
 * @author svenkbiak
 *
 */
public enum Template {
    DEFAULT;
    private transient Logger LOG = LogManager.getLogger(Template.class); //NOSONAR
    private static final String TEMPLATES_FOLDER = "templates/";
    private String notFoundContent;
    private String badRequestContent;
    private String unauthorizedContent;
    private String forbiddenContent;
    private String serverErrorContent;
    private final String notFoundTemplatePath = "defaults/404.html";
    private final String badRequestTemplatePath = "defaults/400.html";
    private final String unauthorizedTemplatePath = "defaults/401.html";
    private final String forbiddenTemplatePath = "defaults/403.html";
    private final String serverErrorTemplatePath = "defaults/500.html";
    private final String routesTemplatePath = "admin/routes.ftl";
    private final String cacheTemplatePath = "admin/cache.ftl";
    private final String configurationTemplatePath = "admin/configuration.ftl";
    private final String metricsTemplatePath = "admin/metrics.ftl";
    private final String schedulerTemplatePath = "admin/scheduler.ftl";
    private final String adminTemplatePath = "admin/index.ftl";

    Template () {
        try {
            this.notFoundContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + this.notFoundTemplatePath), Charsets.UTF_8);
            this.badRequestContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + this.badRequestTemplatePath), Charsets.UTF_8);
            this.unauthorizedContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + this.unauthorizedTemplatePath), Charsets.UTF_8);
            this.forbiddenContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + this.forbiddenTemplatePath), Charsets.UTF_8);
            this.serverErrorContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + this.serverErrorTemplatePath), Charsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Failed to load default templates", e);
        }
    }

    /**
     * @return The content of the default forbidden template
     */
    public String forbidden() {
        return this.forbiddenContent;
    }

    /**
     * @return The content of the default not found template
     */
    public String notFound() {
        return this.notFoundContent;
    }

    /**
     * @return The content of the default bad request template
     */
    public String badRequest() {
        return this.badRequestContent;
    }

    /**
     * @return The content of the default unauthorized template
     */
    public String unauthorized() {
        return this.unauthorizedContent;
    }

    /**
     * @return The content of the default internal server error template
     */
    public String serverError() {
        return this.serverErrorContent;
    }

    /**
     * @return The relative path of the forbidden template
     */
    public String forbiddenPath() {
        return this.forbiddenTemplatePath;
    }

    /**
     * @return The relative path of the not found template
     */
    public String notFoundPath() {
        return this.notFoundTemplatePath;
    }

    /**
     * @return The relative path of the bad request template
     */
    public String badRequestPath() {
        return this.badRequestTemplatePath;
    }

    /**
     * @return The relative path of the unauthorized template
     */
    public String unauthorizedPath() {
        return this.unauthorizedTemplatePath;
    }

    /**
     * @return The relative path of the internal server error template
     */
    public String serverErrorPath() {
        return this.serverErrorTemplatePath;
    }

    /**
     * @return The relative path of the routes template
     */
    public String routesPath() {
        return this.routesTemplatePath;
    }

    /**
     * @return The relative path of the cache template
     */
    public String cachePath() {
        return this.cacheTemplatePath;
    }

    /**
     * @return The relative path of the config template
     */
    public String configurationPath() {
        return this.configurationTemplatePath;
    }

    /**
     * @return The relative path of the metrics template
     */
    public String metricsPath() {
        return this.metricsTemplatePath;
    }

    /**
     * @return The relative path of the scheduler template
     */
    public String schedulerPath() {
        return this.schedulerTemplatePath;
    }

    /**
     * @return The relative path of the admin template
     */
    public String adminPath() {
        return this.adminTemplatePath;
    }
}