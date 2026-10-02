package io.mangoo.i18n;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.Default;
import io.mangoo.constants.Required;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class Messages implements Serializable {
    @Serial
    private static final long serialVersionUID = -1713264225655435037L;
    private static final Logger LOG = LogManager.getLogger(Messages.class);
    private static final int MAX_REPORTED_KEYS = 1000;
    // Each missing key is reported once, the size limit keeps dynamically built keys from growing the set without bound
    private static final Set<String> REPORTED_KEYS = ConcurrentHashMap.newKeySet();

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

    /**
     * Falls back to the framework defaults and returns an empty string if the key is not configured.
     * The text is returned as is, without MessageFormat processing.
     */
    @SuppressFBWarnings(justification = "Key access as intended", value = "MUI_CONTAINSKEY_BEFORE_GET")
    public String get(String key) {
        var resourceBundle = bundle();
        if (resourceBundle.containsKey(key)) {
            return resourceBundle.getString(key);
        } else if (defaults.containsKey(key)) {
            return defaults.get(key);
        }

        reportMissing(key);
        return Strings.EMPTY;
    }

    @SuppressFBWarnings(justification = "Key access as intended", value = "MUI_CONTAINSKEY_BEFORE_GET")
    public String get(String key, Object... arguments) {
        var resourceBundle = bundle();
        if (resourceBundle.containsKey(key)) {
            return MessageFormat.format(resourceBundle.getString(key), arguments);
        } else if (defaults.containsKey(key)) {
            return MessageFormat.format(defaults.get(key), arguments);
        }

        reportMissing(key);
        return Strings.EMPTY;
    }

    private void reportMissing(String key) {
        if (REPORTED_KEYS.size() < MAX_REPORTED_KEYS && REPORTED_KEYS.add(locale + ":" + key)) {
            LOG.warn("Missing i18n key '{}' for locale '{}'", key, locale);
        }
    }

    // ResourceBundle is not serializable, so the transient bundle is resolved again after deserialization.
    private ResourceBundle bundle() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(Default.BUNDLE_NAME, locale, NO_FALLBACK_CONTROL);
        }

        return bundle;
    }
}
