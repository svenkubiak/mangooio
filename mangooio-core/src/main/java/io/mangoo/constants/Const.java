package io.mangoo.constants;

import java.util.regex.Pattern;

public final class Const {
    public static final Pattern NAME_PATTERN = Pattern.compile("[^a-zA-Z0-9]");
    public static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{(.*?)\\}");
    public static final Pattern COOKIE_PATTERN = Pattern.compile("\"");
    public static final String INVOKE_CONTROLLER = "Invoke controller";
    public static final String FRAMEWORK = "io.mangoo";
    public static final String BLACKLIST_PREFIX = "invalid_";
    public static final String CSRF_TOKEN = "x-csrf-token";
    public static final String CONFIG_FILE = "config.yaml";
    public static final String KEYSTORE_FILENAME = "vault.p12";

    private Const() {}
}
