package io.mangoo.constants;

import io.mangoo.utils.CommonUtils;

public final class Template {
    private static final String ADMIN_TEMPLATE_PATH = "@admin/index.ftl";
    private static final String CACHE_TEMPLATE_PATH = "@admin/cache.ftl";
    private static final String INTERNAL_SERVER_ERROR_TEMPLATE_PATH = "defaults/500.html";
    private static final String OK_TEMPLATE_PATH = "defaults/200.html";
    private static final String BAD_REQUEST_TEMPLATE_PATH = "defaults/400.html";
    private static final String FORBIDDEN_TEMPLATE_PATH = "defaults/403.html";
    private static final String XXX_TEMPLATE_PATH = "defaults/xxx.html";
    private static final String SCHEDULER_TEMPLATE_PATH = "@admin/scheduler.ftl";
    private static final String LOGIN_TEMPLATE_PATH = "@admin/login.ftl";
    private static final String NOT_FOUND_TEMPLATE_PATH = "defaults/404.html";
    private static final String TWO_FACTOR_TEMPLATE_PATH = "@admin/twoFactor.ftl";
    private static final String TEMPLATES_FOLDER = "templates/";
    private static final String TOOLS_TEMPLATE_PATH = "@admin/security.ftl";
    private static final String UNAUTHORIZED_TEMPLATE_PATH = "defaults/401.html";
    private static final String NOT_FOUND_CONTENT;
    private static final String INTERNAL_SERVER_ERROR_CONTENT;
    private static final String UNAUTHORIZED_CONTENT;
    private static final String OK_CONTENT;
    private static final String BAD_REQUEST_CONTENT;
    private static final String FORBIDDEN_CONTENT;
    private static final String XXX_CONTENT;
    static {
        OK_CONTENT = CommonUtils.readResourceToString(TEMPLATES_FOLDER + OK_TEMPLATE_PATH);
        BAD_REQUEST_CONTENT = CommonUtils.readResourceToString(TEMPLATES_FOLDER + BAD_REQUEST_TEMPLATE_PATH);
        UNAUTHORIZED_CONTENT = CommonUtils.readResourceToString(TEMPLATES_FOLDER + UNAUTHORIZED_TEMPLATE_PATH);
        FORBIDDEN_CONTENT = CommonUtils.readResourceToString(TEMPLATES_FOLDER + FORBIDDEN_TEMPLATE_PATH);
        NOT_FOUND_CONTENT = CommonUtils.readResourceToString(TEMPLATES_FOLDER + NOT_FOUND_TEMPLATE_PATH);
        INTERNAL_SERVER_ERROR_CONTENT = CommonUtils.readResourceToString(TEMPLATES_FOLDER + INTERNAL_SERVER_ERROR_TEMPLATE_PATH);
        XXX_CONTENT = CommonUtils.readResourceToString(TEMPLATES_FOLDER + XXX_TEMPLATE_PATH);
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
    public static String internalServerError() {
        return INTERNAL_SERVER_ERROR_CONTENT;
    }

    /**
     * @return The content of the default ok template
     */
    public static String ok() {
        return OK_CONTENT;
    }

    /**
     * @return The content of the default bad request template
     */
    public static String badRequest() {
        return BAD_REQUEST_CONTENT;
    }

    /**
     * @return The content of the default forbidden template
     */
    public static String forbidden() {
        return FORBIDDEN_CONTENT;
    }

    /**
     * @return The content of the default xxx template
     */
    public static String xxx() {
        return XXX_CONTENT;
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
