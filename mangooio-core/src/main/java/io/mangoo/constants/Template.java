package io.mangoo.constants;

import io.mangoo.utils.MangooUtils;

public final class Template {
    private static final String ADMIN_TEMPLATE_PATH = "@admin/index.ftl";
    private static final String BAD_REQUEST_TEMPLATE_PATH = "defaults/400.html";
    private static final String CACHE_TEMPLATE_PATH = "@admin/cache.ftl";
    private static final String FORBIDDEN_TEMPLATE_PATH = "defaults/403.html";
    private static final String INTERNAL_SERVER_ERROR_TEMPLATE_PATH = "defaults/500.html";
    private static final String SCHEDULER_TEMPLATE_PATH = "@admin/scheduler.ftl";
    private static final String LOGIN_TEMPLATE_PATH = "@admin/login.ftl";
    private static final String NOT_FOUND_TEMPLATE_PATH = "defaults/404.html";
    private static final String TOO_MANY_REQUESTS_TEMPLATE_PATH = "defaults/429.html";
    private static final String TWO_FACTOR_TEMPLATE_PATH = "@admin/twofactor.ftl";
    private static final String TEMPLATES_FOLDER = "templates/";
    private static final String TOOLS_TEMPLATE_PATH = "@admin/tools.ftl";
    private static final String UNAUTHORIZED_TEMPLATE_PATH = "defaults/401.html";
    private static final String badRequestContent;
    private static final String forbiddenContent;
    private static final String notFoundContent;
    private static final String serverErrorContent;
    private static final String unauthorizedContent;
    private static final String tooManyRequests;
    static {
        notFoundContent = MangooUtils.readResourceToString(TEMPLATES_FOLDER + NOT_FOUND_TEMPLATE_PATH);
        badRequestContent = MangooUtils.readResourceToString(TEMPLATES_FOLDER + BAD_REQUEST_TEMPLATE_PATH);
        unauthorizedContent = MangooUtils.readResourceToString(TEMPLATES_FOLDER + UNAUTHORIZED_TEMPLATE_PATH);
        forbiddenContent = MangooUtils.readResourceToString(TEMPLATES_FOLDER + FORBIDDEN_TEMPLATE_PATH);
        serverErrorContent = MangooUtils.readResourceToString(TEMPLATES_FOLDER + INTERNAL_SERVER_ERROR_TEMPLATE_PATH);
        tooManyRequests = MangooUtils.readResourceToString(TEMPLATES_FOLDER + TOO_MANY_REQUESTS_TEMPLATE_PATH);
    }

    private Template () {
    }

    /**
     * @return The relative path of the admin template
     */
    public static String adminPath() {
        return ADMIN_TEMPLATE_PATH;
    }

    /**
     * @return The content of the default bad request template
     */
    public static String badRequest() {
        return badRequestContent;
    }

    /**
     * @return The relative path of the bad request template
     */
    public static String badRequestPath() {
        return BAD_REQUEST_TEMPLATE_PATH;
    }

    /**
     * @return The relative path of the cache template
     */
    public static String cachePath() {
        return CACHE_TEMPLATE_PATH;
    }

    /**
     * @return The content of the default forbidden template
     */
    public static String forbidden() {
        return forbiddenContent;
    }

    /**
     * @return The relative path of the forbidden template
     */
    public static String forbiddenPath() {
        return FORBIDDEN_TEMPLATE_PATH;
    }

    /**
     * @return The relative path of the scheduler template
     */
    public static String schedulerPath() {
        return SCHEDULER_TEMPLATE_PATH;
    }

    /**
     * @return The relative path of the cache template
     */
    public static String twofactorPath() {
        return TWO_FACTOR_TEMPLATE_PATH;
    }

    /**
     * @return The relative path of the login template
     */
    public static String loginPath() {
        return LOGIN_TEMPLATE_PATH;
    }

    /**
     * @return The content of the default not found template
     */
    public static String notFound() {
        return notFoundContent;
    }

    /**
     * @return The relative path of the not found template
     */
    public static String notFoundPath() {
        return NOT_FOUND_TEMPLATE_PATH;
    }

    /**
     * @return The content of the default internal server error template
     */
    public static String serverError() {
        return serverErrorContent;
    }

    /**
     * @return The content of the default too many requests error template
     */
    public static String tooManyRequests() {
        return tooManyRequests;
    }

    /**
     * @return The relative path of the internal server error template
     */
    public static String serverErrorPath() {
        return INTERNAL_SERVER_ERROR_TEMPLATE_PATH;
    }

    /**
     * @return The relative path of the tools template
     */
    public static String toolsPath() {
        return TOOLS_TEMPLATE_PATH;
    }

    /**
     * @return The content of the default unauthorized template
     */
    public static String unauthorized() {
        return unauthorizedContent;
    }

    /**
     * @return The relative path of the unauthorized template
     */
    public static String unauthorizedPath() {
        return UNAUTHORIZED_TEMPLATE_PATH;
    }
}
