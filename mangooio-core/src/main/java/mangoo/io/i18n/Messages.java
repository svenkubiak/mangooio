package mangoo.io.i18n;

import java.text.MessageFormat;
import java.util.Locale;
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
    private ResourceBundle bundle;
    private Locale locale;

    public Messages() {
        this.bundle = ResourceBundle.getBundle(Default.BUNDLE_NAME.toString(), Locale.getDefault());
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
        }

        if (Key.FORM_REQUIRED.toString().equals(key)) {
            return MessageFormat.format(Validation.REQUIRED.toString(), arguments);
        } else if (Key.FORM_MIN.toString().equals(key)) {
            return MessageFormat.format(Validation.MIN.toString(), arguments);
        } else if (Key.FORM_MAX.toString().equals(key)) {
            return MessageFormat.format(Validation.MAX.toString(), arguments);
        } else if (Key.FORM_EXACT_MATCH.toString().equals(key)) {
            return MessageFormat.format(Validation.EXACT_MATCH.toString(), arguments);
        } else if (Key.FORM_MATCH.toString().equals(key)) {
            return MessageFormat.format(Validation.MATCH.toString(), arguments);
        } else if (Key.FORM_EMAIL.toString().equals(key)) {
            return MessageFormat.format(Validation.EMAIL.toString(), arguments);
        } else if (Key.FORM_IPV4.toString().equals(key)) {
            return MessageFormat.format(Validation.IPV4.toString(), arguments);
        } else if (Key.FORM_IPV6.toString().equals(key)) {
            return MessageFormat.format(Validation.IPV6.toString(), arguments);
        } else if (Key.FORM_RANGE.toString().equals(key)) {
            return MessageFormat.format(Validation.RANGE.toString(), arguments);
        } else if (Key.FORM_URL.toString().equals(key)) {
            return MessageFormat.format(Validation.URL.toString(), arguments);
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