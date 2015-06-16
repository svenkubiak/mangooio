package mangoo.io.enums;

/**
 *
 * @author svenkubiak
 *
 */
public enum Key {
    APPLICATION_NAME("application.name"),
    APPLICATION_MINIFY_JS("application.minify.js"),
    APPLICATION_MINIFY_CSS("application.minify.css"),
    APPLICATION_MINIFY_JSFOLDER("application.minify.jsfolder"),
    APPLICATION_MINIFY_CSSFOLDER("application.minify.css"),
    APPLICATION_GZIP_JS("application.minify.gzipjs"),
    APPLICATION_GZIP_CSS("application.minify.gzipcss"),
    APPLICATION_SECRET("application.secret"),
    APPLICATION_PORT("application.port"),
    APPLICATION_HOST("application.host"),
    APPLICATION_LANGUAGE("application.language"),
    APPLICATION_MODE("application.mode"),
    AUTH_COOKIE_NAME("auth.cookie.name"),
    AUTH_REDIRECT_URL("auth.redirect.url"),
    AUTH_COOKIE_ENCRYPT("auth.cookie.encrypt"),
    AUTH_COOKIE_EXPIRES("auth.cookie.expires"),
    SMTP_HOST("smtp.host"),
    SMTP_PORT("smtp.port"),
    SMTP_USERNAME("smtp.username"),
    SMTP_PASSWORD("smtp.password"),
    SMTP_SSL("smtp.ssl"),
    COOKIE_NAME("cookie.name"),
    COOKIE_ENCRYPTION("cookie.encryption"),
    COOKIE_EXPIRES("cookie.expires");

    private final String value;

    Key (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}