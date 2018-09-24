package io.mangoo.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Resources;

import io.mangoo.enums.Default;
import io.mangoo.enums.Required;

/**
 *
 * @author svenkubiak
 *
 */
public final class BootstrapUtils {
    private static final Logger LOG = LogManager.getLogger(BootstrapUtils.class);

    private BootstrapUtils() {
    }

    /**
     * Retrieves the current version of the framework from the version.properties file
     *
     * @return Current mangoo I/O version
     */
    public static String getVersion() {
        String version = Default.VERSION.toString();
        try (InputStream inputStream = Resources.getResource(Default.VERSION_PROPERTIES.toString()).openStream()) {
            final Properties properties = new Properties();
            properties.load(inputStream);
            version = String.valueOf(properties.get("version"));
        } catch (final IOException e) {
            LOG.error("Failed to get application version", e);
        }

        return version;
    }

    /**
     * Retrieves the logo from the logo file and returns the string
     * 
     * @return The mangoo I/O logo string
     */
    public static String getLogo() {
        String logo = "";
        try (InputStream inputStream = Resources.getResource(Default.LOGO_FILE.toString()).openStream()) {
            logo = IOUtils.toString(inputStream, Default.ENCODING.toString());
        } catch (final IOException e) {
            LOG.error("Failed to get application logo", e);
        }

        return logo;
    }
    
    /**
     * Checks that a given package name ends with an .
     * 
     * @param packageName The package name to check
     * @return A valid package name
     */
    public static String getPackageName(String packageName) {
        Objects.requireNonNull(packageName, Required.PACKAGE_NAME.toString());

        if (!packageName.endsWith(".")) {
            return packageName + '.';
        }

        return packageName;
    }

    /**
     * @return The OS specific path to src/main/java
     */
    public static String getBaseDirectory() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(System.getProperty("user.dir"))
        .append(File.separator)
        .append("src")
        .append(File.separator)
        .append("main")
        .append(File.separator)
        .append("java");
        
        return buffer.toString();
    }
    
    /**
     * Checks if a given method exists in a given class
     * @param controllerMethod The method to check
     * @param controllerClass The class to check 
     * 
     * @return True if the method exists, false otherweise
     */
    public static boolean methodExists(String controllerMethod, Class<?> controllerClass) {
        boolean exists = false;
        for (final Method method : controllerClass.getMethods()) {
            if (method.getName().equals(controllerMethod)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            LOG.error("Could not find controller method '{}' in controller class '{}'", controllerMethod, controllerClass.getSimpleName());
        }

        return exists;
    }
}