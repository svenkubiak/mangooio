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

    // Never fall back to the JVM default locale, as that would mix an unrelated language into the lookup.
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

    public Locale getLocale() {
        return locale;
    }

    /** Throws a MissingResourceException if the key is not configured, as there is no fallback to the defaults. */
    public String get(String key) {
        return bundle().getString(key);
    }

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

    // ResourceBundle is not serializable, so the transient bundle is resolved again after deserialization.
    private ResourceBundle bundle() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(Default.BUNDLE_NAME, locale, NO_FALLBACK_CONTROL);
        }

        return bundle;
    }
}
