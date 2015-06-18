package mangoo.io.enums;

/**
 *
 * @author svenkubiak
 *
 */
public enum Default {
    LANGUAGE("en"),
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
    CONFIG_PATH("/src/main/resources/application.conf"),
    FAKE_SMTP_PROTOCOL("smtp"),
    STYLESHEET_FOLDER("stylesheets"),
    JAVSCRIPT_FOLDER("javascripts"),
    VERSION_PROPERTIES("version.properties"),
    CONTENT_TYPE("text/html; charset=UTF-8"),
    SCHEDULER_PREFIX("org.quartz."),
    APPLICATION_SECRET_MIN_LENGTH("16"),
    SERVER("Undertow"),
    CACHE_NAME("mangoo"),
    TEMPLATES_FOLDER("/templates/"),
    TEMPLATE_SUFFIX(".ftl");

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