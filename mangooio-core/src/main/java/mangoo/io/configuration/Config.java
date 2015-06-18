package mangoo.io.configuration;

import java.util.Properties;

import mangoo.io.core.Application;
import mangoo.io.enums.Default;
import mangoo.io.enums.Key;
import mangoo.io.enums.Mode;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Config {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private CompositeConfiguration compositeConfiguration = new CompositeConfiguration();

    public Config() {
        init("application.conf", Application.getMode());
    }

    public Config(String path, Mode mode) {
        init(path, mode);
    }

    private void init(String configFile, Mode mode) {
        try {
            PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(configFile);

            Configuration configuration = propertiesConfiguration.subset("%" + mode.toString());
            this.compositeConfiguration.addConfiguration(configuration);
            this.compositeConfiguration.addConfiguration(propertiesConfiguration);
        } catch (ConfigurationException e) {
            LOG.error("Failed to load application.conf", e);
        }
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as String or null if the key is not configured
     */
    public String getString(String key) {
        return this.compositeConfiguration.getString(key);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as String or the passed defautlValue if the key is not configured
     */
    public String getString(String key, String defaultValue) {
        return this.compositeConfiguration.getString(key, defaultValue);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as int or 0 if the key is not configured
     */
    public int getInt(String key) {
        return this.compositeConfiguration.getInt(key);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as int or the passed defautlValue if the key is not configured
     */
    public int getInt(String key, int defaultValue) {
        return this.compositeConfiguration.getInt(key, defaultValue);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as boolean or false if the key is not configured
     */
    public boolean getBoolean(String key) {
        return this.compositeConfiguration.getBoolean(key);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as boolean or the passed defautlValue if the key is not configured
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return this.compositeConfiguration.getBoolean(key, defaultValue);
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as String or null if the key is not configured
     */
    public String getString(Key key) {
        return getString(key.toString());
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as String or the passed defautlValue if the key is not configured
     */
    public String getString(Key key, String defaultValue) {
        return getString(key.toString(), defaultValue);
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as int or 0 if the key is not configured
     */
    public int getInt(Key key) {
        return getInt(key.toString());
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as int or the passed defautlValue if the key is not configured
     */
    public int getInt(Key key, int defaultValue) {
        return getInt(key.toString(), defaultValue);
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as boolean or false if the key is not configured
     */
    public boolean getBoolean(Key key) {
        return getBoolean(key.toString());
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as boolean or the passed defautlValue if the key is not configured
     */
    public boolean getBoolean(Key key, boolean defaultValue) {
        return getBoolean(key.toString(), defaultValue);
    }

    public Properties getAllConfigurations() {
        return ConfigurationConverter.getProperties(this.compositeConfiguration);
    }

    /**
     * Checks if the application.conf stored in conf/application.conf contains an application
     * secret property (application.secret) that has at least 16 characters (128-Bit)
     *
     * @return True if the configuration contains an application.secret property with at least 16 characters
     */
    public boolean hasValidSecret() {
        String secret = this.compositeConfiguration.getString(Key.APPLICATION_SECRET.toString());
        return StringUtils.isNotBlank(secret) && secret.length() >= Default.APPLICATION_SECRET_MIN_LENGTH.toInt();
    }
}