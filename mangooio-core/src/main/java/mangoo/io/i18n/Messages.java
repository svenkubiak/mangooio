package mangoo.io.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import mangoo.io.enums.Default;

import com.google.inject.Singleton;

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
    public String get(String key, Object... arguments) {
        if (this.bundle.containsKey(key)) {
            return MessageFormat.format(this.bundle.getString(key), arguments);
        }

        return null;
    }
}