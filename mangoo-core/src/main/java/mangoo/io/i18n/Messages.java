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

    public void reload() {
        if (!Locale.getDefault().equals(this.locale)) {
            this.locale = Locale.getDefault();
            this.bundle = ResourceBundle.getBundle(Default.BUNDLE_NAME.toString(), Locale.getDefault());
        }
    }

    public String get(String key) {
        return this.bundle.getString(key);
    }

    public String get(String key, Object... arguments) {
        if (this.bundle.containsKey(key)) {
            return MessageFormat.format(this.bundle.getString(key), arguments);
        }

        return null;
    }
}