package io.mangoo.configuration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;

import io.mangoo.core.Application;
import io.mangoo.enums.Key;

/**
 * 
 * @author svenkubiak
 *
 */
public class ConfigFactory extends ConfigurationFactory {
    private static final String[] SUFFIXES = new String[] { ".yaml", ".yml" };

    @Override
    @SuppressWarnings("all")
    public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource configurationSource) {
        String configurationFile = System.getProperty(Key.APPLICATION_LOG.toString());
        
        URL url = null;
        if (StringUtils.isNotBlank(configurationFile)) {
            try {
                url = Paths.get(configurationFile).toUri().toURL(); //NOSONAR
                System.setProperty(Key.LOGGER_MESSAGE.toString(), "Found Log4j2 configuration file reference as JVM arugment. Using configuration file: " + configurationFile);
            } catch (MalformedURLException e) {
                e.printStackTrace(); //NOSONAR
            }
        } else {
            configurationFile = "log4j2." + Application.getMode() + ".yaml";
            if (Thread.currentThread().getContextClassLoader().getResource(configurationFile) != null) {
                url = Thread.currentThread().getContextClassLoader().getResource(configurationFile);
                System.setProperty(Key.LOGGER_MESSAGE.toString(), "Found mode specific Log4j2 configuration in classpath. Using configuration file: " + configurationFile);
            }
        }

        YamlConfiguration yamlConfiguration = new YamlConfiguration(loggerContext, configurationSource);
        if (url != null) {
            try {
                yamlConfiguration = new YamlConfiguration(loggerContext, new ConfigurationSource(url.openStream()));
            } catch (IOException e) {
                e.printStackTrace(); //NOSONAR
            }
        } else {
            System.setProperty(Key.LOGGER_MESSAGE.toString(), "Found default Log4j2 configuration in class path. Using configuration file: " + configurationSource.getFile().getName());
        }

        return yamlConfiguration;
    }

    @Override
    protected String[] getSupportedTypes() {
        return Arrays.copyOf(SUFFIXES, SUFFIXES.length);
    }
}