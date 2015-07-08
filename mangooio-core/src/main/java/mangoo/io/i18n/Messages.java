package mangoo.io.i18n;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.inject.Singleton;

import mangoo.io.enums.Default;
import mangoo.io.enums.Key;
import mangoo.io.enums.Validation;

/**
 *
 * @author skubiak
 *
 */
@Singleton
public class Messages {
    private Map<String, String> defaults = new HashMap<String, String>();
    private ResourceBundle bundle;
    private Locale locale;

    public Messages() {
        this.bundle = ResourceBundle.getBundle(Default.BUNDLE_NAME.toString(), Locale.getDefault());

        defaults.put(Key.FORM_REQUIRED.toString(), Validation.REQUIRED.toString());
        defaults.put(Key.FORM_MIN.toString(), Validation.MIN.toString());
        defaults.put(Key.FORM_MAX.toString(), Validation.MAX.toString());
        defaults.put(Key.FORM_EXACT_MATCH.toString(), Validation.EXACT_MATCH.toString());
        defaults.put(Key.FORM_MATCH.toString(), Validation.MATCH.toString());
        defaults.put(Key.FORM_EMAIL.toString(), Validation.EMAIL.toString());
        defaults.put(Key.FORM_IPV4.toString(), Validation.IPV4.toString());
        defaults.put(Key.FORM_IPV6.toString(), Validation.IPV6.toString());
        defaults.put(Key.FORM_RANGE.toString(), Validation.RANGE.toString());
        defaults.put(Key.FORM_URL.toString(), Validation.URL.toString());
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