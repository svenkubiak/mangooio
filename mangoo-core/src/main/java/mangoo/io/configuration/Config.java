package mangoo.io.configuration;

import java.util.Properties;

import mangoo.io.core.Application;
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
    private static final int SECRET_MIN_LENGTH = 16;
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

    public String getString(String key) {
        return this.compositeConfiguration.getString(key);
    }

    public String getString(String key, String defaultValue) {
        return this.compositeConfiguration.getString(key, defaultValue);
    }

    public int getInt(String key) {
        return this.compositeConfiguration.getInt(key);
    }

    public int getInt(String key, int defaultValue) {
        return this.compositeConfiguration.getInt(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return this.compositeConfiguration.getBoolean(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return this.compositeConfiguration.getBoolean(key, defaultValue);
    }

    public String getString(Key key) {
        return getString(key.toString());
    }

    public String getString(Key key, String defaultValue) {
        return getString(key.toString(), defaultValue);
    }

    public int getInt(Key key) {
        return getInt(key.toString());
    }

    public int getInt(Key key, int defaultValue) {
        return getInt(key.toString(), defaultValue);
    }

    public boolean getBoolean(Key key) {
        return getBoolean(key.toString());
    }

    public boolean getBoolean(Key key, boolean defaultValue) {
        return getBoolean(key.toString(), defaultValue);
    }

    public Properties getAllConfigurations() {
        return ConfigurationConverter.getProperties(this.compositeConfiguration);
    }

    public boolean hasValidSecret() {
        String secret = this.compositeConfiguration.getString(Key.APPLICATION_SECRET.toString());
        return StringUtils.isNotBlank(secret) && secret.length() >= SECRET_MIN_LENGTH;
    }
}