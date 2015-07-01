package mangoo.io.enums;

/**
 *
 * @author svenkubiak
 *
 */
public enum Default {
    LANGUAGE("en"),
    AUTH_COOKIE_NAME("MANGOOIO-AUTH"),
    COOKIE_SUFFIX("-AUTH"),
    COOKIE_EXPIRES("86400"),
    LOCALHOST("127.0.0.1"), //NOSONAR
    APPLICATION_HOST("127.0.0.1"), //NOSONAR
    JBCRYPT_ROUNDS("12"),
    SMTP_PORT("25"),
    SMTP_SSL("false"),
    APPLICATION_PORT("8080"),
    BUNDLE_NAME("translations/messages"),
    ASSETS_PATH("src/main/resources/files/assets/"),
    CONFIG_PATH("/src/main/resources/application.yaml"),
    FAKE_SMTP_PROTOCOL("smtp"),
    STYLESHEET_FOLDER("stylesheets"),
    JAVSCRIPT_FOLDER("javascripts"),
    CONFIGURATION_FILE("application.yaml"),
    DEFAULT_CONFIGURATION("default"),
    VERSION_PROPERTIES("version.properties"),
    CONTENT_TYPE("text/html; charset=UTF-8"),
    SCHEDULER_PREFIX("org.quartz."),
    APPLICATION_SECRET_MIN_LENGTH("16"),
    SERVER("Undertow"),
    CACHE_NAME("mangooio"),
    TEMPLATES_FOLDER("/templates/"),
    TEMPLATE_SUFFIX(".ftl"),
    FLASH_SUFFIX("-FLASH"),
    AUTH_COOKIE_EXPIRES("3600"),
    SESSION_COOKIE_NAME("MANGOOIO-SESSION");

    private final String value;

    Default (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public int toInt() {
        return Integer.valueOf(this.value);
    }

    public boolean toBoolean() {
        return Boolean.valueOf(this.value);
    }
}