package io.mangoo.constants;

import io.mangoo.utils.MangooUtils;

public final class Template {
    private static final String ADMIN_TEMPLATE_PATH = "@admin/index.ftl";
    private static final String CACHE_TEMPLATE_PATH = "@admin/cache.ftl";
    private static final String INTERNAL_SERVER_ERROR_TEMPLATE_PATH = "defaults/500.html";
    private static final String SCHEDULER_TEMPLATE_PATH = "@admin/scheduler.ftl";
    private static final String LOGIN_TEMPLATE_PATH = "@admin/login.ftl";
    private static final String NOT_FOUND_TEMPLATE_PATH = "defaults/404.html";
    private static final String TWO_FACTOR_TEMPLATE_PATH = "@admin/twoFactor.ftl";
    private static final String TEMPLATES_FOLDER = "templates/";
    private static final String TOOLS_TEMPLATE_PATH = "@admin/tools.ftl";
    private static final String UNAUTHORIZED_TEMPLATE_PATH = "defaults/401.html";
    private static final String NOT_FOUND_CONTENT;
    private static final String SERVER_ERROR_CONTENT;
    private static final String UNAUTHORIZED_CONTENT;
    static {
        NOT_FOUND_CONTENT = MangooUtils.readResourceToString(TEMPLATES_FOLDER + NOT_FOUND_TEMPLATE_PATH);
        UNAUTHORIZED_CONTENT = MangooUtils.readResourceToString(TEMPLATES_FOLDER + UNAUTHORIZED_TEMPLATE_PATH);
        SERVER_ERROR_CONTENT = MangooUtils.readResourceToString(TEMPLATES_FOLDER + INTERNAL_SERVER_ERROR_TEMPLATE_PATH);
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
     * @return The relative path of the cache template
     */
    public static String cachePath() {
        return CACHE_TEMPLATE_PATH;
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
    public static String twoFactorPath() {
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
        return NOT_FOUND_CONTENT;
    }

    /**
     * @return The content of the default internal server error template
     */
    public static String serverError() {
        return SERVER_ERROR_CONTENT;
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
        return UNAUTHORIZED_CONTENT;
    }
}
