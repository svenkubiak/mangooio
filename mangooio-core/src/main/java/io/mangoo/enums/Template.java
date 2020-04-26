package io.mangoo.enums;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;

import com.google.common.io.Resources;

/**
 * Default HTML templates for standard HTTP responses
 *
 * @author svenkbiak
 *
 */
public enum Template {
    DEFAULT;
    private static final String ADMIN_TEMPLATE_PATH = "@admin/index.ftl";
    private static final String BAD_REQUEST_TEMPLATE_PATH = "defaults/400.html";
    private static final String CACHE_TEMPLATE_PATH = "@admin/cache.ftl";
    private static final String FORBIDDEN_TAMPLTE_PATH = "defaults/403.html";
    private static final String INTERNAL_SERVER_ERROR_TEMPLATE_PATH = "defaults/500.html";
    private static final String LOGGER_TEMPLATE_PATH = "@admin/logger.ftl";
    private static final String LOGIN_TEMPLARE_PATH = "@admin/login.ftl";
    private static final String NOT_FOUND_TEMPLATE_PATH = "defaults/404.html";
    private static final String TOO_MANY_REQUESTS_TEMPLATE_PATH = "defaults/429.html";
    private static final String ROUTES_TEMPLATE_PATH = "@admin/routes.ftl";
    private static final String TWOFACTOR_TEMPLATE_PATH = "@admin/twofactor.ftl";
    private static final String SCHEDULER_TEMPLATE_PATH = "@admin/scheduler.ftl";
    private static final String TEMPLATES_FOLDER = "templates/";
    private static final String TOOLS_TEMPLATE_PATH = "@admin/tools.ftl";
    private static final String UNAUTHORIZED_TEMPLATE_PATH = "defaults/401.html";
    private String badRequestContent;
    private String forbiddenContent;
    private String notFoundContent;
    private String serverErrorContent;
    private String unauthorizedContent;
    private String tooManyRequests;;
    
    Template () {
        try {
            this.notFoundContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + NOT_FOUND_TEMPLATE_PATH), StandardCharsets.UTF_8);
            this.badRequestContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + BAD_REQUEST_TEMPLATE_PATH), StandardCharsets.UTF_8);
            this.unauthorizedContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + UNAUTHORIZED_TEMPLATE_PATH), StandardCharsets.UTF_8);
            this.forbiddenContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + FORBIDDEN_TAMPLTE_PATH), StandardCharsets.UTF_8);
            this.serverErrorContent = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + INTERNAL_SERVER_ERROR_TEMPLATE_PATH), StandardCharsets.UTF_8);
            this.tooManyRequests = Resources.toString(Resources.getResource(TEMPLATES_FOLDER + TOO_MANY_REQUESTS_TEMPLATE_PATH), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LogManager.getLogger(Template.class).error("Failed to load default templates", e);
        }
    }

    /**
     * @return The relative path of the admin template
     */
    public String adminPath() {
        return ADMIN_TEMPLATE_PATH;
    }

    /**
     * @return The content of the default bad request template
     */
    public String badRequest() {
        return this.badRequestContent;
    }

    /**
     * @return The relative path of the bad request template
     */
    public String badRequestPath() {
        return BAD_REQUEST_TEMPLATE_PATH;
    }

    /**
     * @return The relative path of the cache template
     */
    public String cachePath() {
        return CACHE_TEMPLATE_PATH;
    }

    /**
     * @return The content of the default forbidden template
     */
    public String forbidden() {
        return this.forbiddenContent;
    }

    /**
     * @return The relative path of the forbidden template
     */
    public String forbiddenPath() {
        return FORBIDDEN_TAMPLTE_PATH;
    }

    /**
     * @return The relative path of the cache template
     */
    public String loggerPath() {
        return LOGGER_TEMPLATE_PATH;
    }
    
    /**
     * @return The relative path of the cache template
     */
    public String twofactorPath() {
        return TWOFACTOR_TEMPLATE_PATH;
    }

    /**
     * @return The relative path of the login template
     */
    public String loginPath() {
        return LOGIN_TEMPLARE_PATH;
    }

    /**
     * @return The content of the default not found template
     */
    public String notFound() {
        return this.notFoundContent;
    }

    /**
     * @return The relative path of the not found template
     */
    public String notFoundPath() {
        return NOT_FOUND_TEMPLATE_PATH;
    }

    /**
     * @return The relative path of the routes template
     */
    public String routesPath() {
        return ROUTES_TEMPLATE_PATH;
    }

    /**
     * @return The relative path of the scheduler template
     */
    public String schedulerPath() {
        return SCHEDULER_TEMPLATE_PATH;
    }
    
    /**
     * @return The content of the default internal server error template
     */
    public String serverError() {
        return this.serverErrorContent;
    }
    
    /**
     * @return The content of the default too many requests error template
     */
    public String tooManyRequests() {
        return this.tooManyRequests;
    }    
    
    /**
     * @return The relative path of the internal server error template
     */
    public String serverErrorPath() {
        return INTERNAL_SERVER_ERROR_TEMPLATE_PATH;
    }

    /**
     * @return The relative path of the tools template
     */
    public String toolsPath() {
        return TOOLS_TEMPLATE_PATH;
    }

    /**
     * @return The content of the default unauthorized template
     */
    public String unauthorized() {
        return this.unauthorizedContent;
    }

    /**
     * @return The relative path of the unauthorized template
     */
    public String unauthorizedPath() {
        return UNAUTHORIZED_TEMPLATE_PATH;
    }
}