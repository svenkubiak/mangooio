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
    private String internalServerErrorContent;
    private String notFoundTemplate = "defaults/404.html";
    private String badRequestTemplate = "defaults/400.html";
    private String unauthorizedTemplate = "defaults/401.html";
    private String forbiddenTemplate = "defaults/403.html";
    private String internalServerErrorTemplate = "defaults/500.html";
    private String routesTemplate = "defaults/routes.ftl";
    private String cacheTemplate = "defaults/cache.ftl";
    private String configTemplate = "defaults/config.ftl";
    private String metricsTemplate = "defaults/metrics.ftl";
    private String schedulerTemplate = "defaults/scheduler.ftl";

    Template () {
        try {
            this.notFoundContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + this.notFoundTemplate), Charsets.UTF_8);
            this.badRequestContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + this.badRequestTemplate), Charsets.UTF_8);
            this.unauthorizedContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + this.unauthorizedTemplate), Charsets.UTF_8);
            this.forbiddenContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + this.forbiddenTemplate), Charsets.UTF_8);
            this.internalServerErrorContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + this.internalServerErrorTemplate), Charsets.UTF_8);
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
    public String internalServerErrorContent() {
        return this.internalServerErrorContent;
    }
    
    /**
     * @return The relative path of the forbidden template
     */
    public String forbiddenTemplate() {
        return this.forbiddenTemplate;
    }

    /**
     * @return The relative path of the not found template
     */
    public String notFoundTemplate() {
        return this.notFoundTemplate;
    }

    /**
     * @return The relative path of the bad request template
     */
    public String badRequestTemplate() {
        return this.badRequestTemplate;
    }

    /**
     * @return The relative path of the unauthorized template
     */
    public String unauthorizedTemplate() {
        return this.unauthorizedTemplate;
    }

    /**
     * @return The relative path of the internal server error template
     */
    public String internalServerErrorTemplate() {
        return this.internalServerErrorTemplate;
    }
    
    /**
     * @return The relative path of the routes template
     */
    public String routesTemplate() {
        return this.routesTemplate;
    }

    /**
     * @return The relative path of the cache template
     */
    public String cacheTemplate() {
        return this.cacheTemplate;
    }

    /**
     * @return The relative path of the config template
     */
    public String configTemplate() {
        return this.configTemplate;
    }

    /**
     * @return The relative path of the metrics template
     */
    public String metricsTemplate() {
        return this.metricsTemplate;
    }

    /**
     * @return The relative path of the scheduler template
     */
    public String schedulerTemplate() {
        return this.schedulerTemplate;
    }
}