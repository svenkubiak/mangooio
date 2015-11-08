package io.mangoo.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.Singleton;

import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Validation;

/**
 * Convinient class for handling translations
 *
 * @author skubiak
 *
 */
@Singleton
public class Messages {
    private final Map<String, String> defaults = new ConcurrentHashMap<>(16, 0.9f, 1);
    private ResourceBundle bundle;
    private Locale locale;

    public Messages() {
        this.bundle = ResourceBundle.getBundle(Default.BUNDLE_NAME.toString(), Locale.getDefault());

        defaults.put(Key.VALIDATION_REQUIRED.toString(), Validation.REQUIRED.toString());
        defaults.put(Key.VALIDATION_MIN.toString(), Validation.MIN.toString());
        defaults.put(Key.VALIDATION_MAX.toString(), Validation.MAX.toString());
        defaults.put(Key.VALIDATION_EXACT_MATCH.toString(), Validation.EXACT_MATCH.toString());
        defaults.put(Key.VALIDATION_MATCH.toString(), Validation.MATCH.toString());
        defaults.put(Key.VALIDATION_EMAIL.toString(), Validation.EMAIL.toString());
        defaults.put(Key.VALIDATION_IPV4.toString(), Validation.IPV4.toString());
        defaults.put(Key.VALIDATION_IPV6.toString(), Validation.IPV6.toString());
        defaults.put(Key.VALIDATION_RANGE.toString(), Validation.RANGE.toString());
        defaults.put(Key.VALIDATION_URL.toString(), Validation.URL.toString());
        defaults.put(Key.VALIDATION_REGEX.toString(), Validation.REGEX.toString());
    }

    /**
     * Refreshes the resource bundle by reloading the bundle with the default locale
     */
    public void reload() {
        if (!Locale.getDefault().equals(this.locale)) {
            this.locale = Locale.getDefault();
            this.bundle = ResourceBundle.getBundle(Default.BUNDLE_NAME.toString(), Locale.getDefault());
        }
    }

    /**
     * Returns a localized value for a given key stored in messages_xx.properties
     *
     * @param key The key to look up the localized value
     * @return The localized value or an empty value if the given key is not configured
     */
    public String get(String key) {
        return this.bundle.getString(key);
    }

    /**
     * Returns a localized value for a given key stored in messages_xx.properties and passing the
     * given arguments
     *
     * @param key The key to look up the localized value
     * @param arguments The arguments to use
     * @return The localized value or null value if the given key is not configured
     */
    @SuppressWarnings("all")
    public String get(String key, Object... arguments) {
        if (this.bundle.containsKey(key)) {
            return MessageFormat.format(this.bundle.getString(key), arguments);
        } else if (this.defaults.containsKey(key)) {
            return MessageFormat.format(this.defaults.get(key), arguments);
        }

        return "";
    }

    /**
     * Returns a localized value for a given key stored in messages_xx.properties and passing the
     * given arguments
     *
     * @param key The key enum to lookup up the localized value
     * @param arguments The arguments to use
     * @return The localized value or null value if the given key is not configured
     */
    public String get(Key key, Object... arguments) {
        return get(key.toString(), arguments);
    }
}