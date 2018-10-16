package io.mangoo.i18n;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import io.mangoo.enums.Default;
import io.mangoo.enums.Key;

/**
 * Convenient class for handling translations
 *
 * @author svenkubiak
 *
 */
public class Messages implements Serializable {
    private static final long serialVersionUID = 7560110796880143546L;
    private Map<String, String> defaults = Default.getMessages();
    private transient ResourceBundle bundle;

    public Messages() {
        this.bundle = ResourceBundle.getBundle(Default.BUNDLE_NAME.toString(), Locale.getDefault());
    }

    /**
     * Refreshes the resource bundle by reloading the bundle with the default locale
     * 
     * @param locale The locale to use
     */
    public void reload(Locale locale) {
        this.bundle = ResourceBundle.getBundle(Default.BUNDLE_NAME.toString(), locale);
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
        } else if (this.defaults.containsKey(key)) {
            return MessageFormat.format(this.defaults.get(key), arguments);
        } else {
            // Ignore anything else
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