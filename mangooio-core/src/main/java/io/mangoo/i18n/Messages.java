package io.mangoo.i18n;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.Default;
import io.mangoo.constants.Required;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.util.Strings;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

@Singleton
public class Messages implements Serializable {
    @Serial
    private static final long serialVersionUID = -1713264225655435037L;

    /**
     * Resolving a bundle must never fall back to the JVM default locale, as that would
     * mix an unrelated language into the lookup. Missing locales fall back to the base bundle.
     */

    private static final ResourceBundle.Control NO_FALLBACK_CONTROL =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

    private final Map<String, String> defaults = Default.getMessages();
    private final Locale locale;
    private transient ResourceBundle bundle;

    public Messages() {
        this(Locale.getDefault());
    }

    public Messages(Locale locale) {
        this.locale = Objects.requireNonNull(locale, Required.LOCALE);
        this.bundle = ResourceBundle.getBundle(Default.BUNDLE_NAME, locale, NO_FALLBACK_CONTROL);
    }

    /**
     * @return The locale this instance resolves its messages with
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns a localized value for a given key stored in messages_xx.properties
     *
     * @param key The key to look up the localized value
     * @return The localized value or an empty value if the given key is not configured
     */
    public String get(String key) {
        return bundle().getString(key);
    }

    /**
     * Returns a localized value for a given key stored in messages_xx.properties and passing the
     * given arguments
     *
     * @param key The key to look up the localized value
     * @param arguments The arguments to use
     * @return The localized value or null value if the given key is not configured
     */
    @SuppressFBWarnings(justification = "Key access as intended", value = "MUI_CONTAINSKEY_BEFORE_GET")
    public String get(String key, Object... arguments) {
        var resourceBundle = bundle();
        if (resourceBundle.containsKey(key)) {
            return MessageFormat.format(resourceBundle.getString(key), arguments);
        } else if (defaults.containsKey(key)) {
            return MessageFormat.format(defaults.get(key), arguments);
        } else {
            // Ignore anything else
        }

        return Strings.EMPTY;
    }

    /**
     * The bundle is transient, as a ResourceBundle is not serializable. It is resolved from the
     * locale when this instance was restored from a serialized state. ResourceBundle lookups are
     * cached, so resolving is cheap and always yields the same effectively immutable instance.
     *
     * @return The resource bundle for the locale of this instance
     */
    private ResourceBundle bundle() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(Default.BUNDLE_NAME, locale, NO_FALLBACK_CONTROL);
        }

        return bundle;
    }
}
