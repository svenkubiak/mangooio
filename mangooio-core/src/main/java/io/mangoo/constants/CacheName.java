package io.mangoo.constants;

public final class CacheName {
    public static final String APPLICATION = "mangooio-application-cache";
    public static final String AUTH = "mangooio-auth-cache";
    public static final String BLACKLIST = "mangooio-auth-blacklist";
    public static final String AUTH_PASSWORD_PREFIX = "pwd:";
    public static final String AUTH_SECOND_FACTOR_PREFIX = "2fa:";

    private CacheName() {
    }
}
