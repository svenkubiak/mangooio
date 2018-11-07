package io.mangoo.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;

import io.mangoo.enums.Default;
import io.mangoo.enums.Required;

/**
 * 
 * @author svenkubiak
 *
 */
public final class MangooUtils {
    private static final Logger LOG = LogManager.getLogger(MangooUtils.class);
    private static final String [] UNITS = new String[] { "B", "kB", "MB", "GB", "TB" };
    private static final char[] CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int MAX_PASSWORD_LENGTH = 256;
    private static final int MIN_PASSWORD_LENGTH = 0;
    private static final int CONVERTION = 1024;
    
    private MangooUtils() {
    }

    /**
     * Retrieves the current version of the framework from the version.properties file
     *
     * @return Current mangoo I/O version
     */
    public static String getVersion() {
        String version = Default.VERSION_UNKNOW.toString();
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
     * Copies a given map to a new map instance
     * 
     * @param originalMap The map to copy
     * @return A new Map instance with value from originalMap
     */
    public static Map<String, String> copyMap(Map<String, String> originalMap) {
        Objects.requireNonNull(originalMap, Required.MAP.toString());
        
        return new HashMap<>(originalMap);
    }
    
    /**
     * Generates a random string with the given length.
     * 
     * Based on commons-lang3 RandomStringUtils using SecureRandom
     * 
     * Uses: uppercase letters, lowercase letters and numbers
     * 
     * @param length The length of the random string
     * @return A random String
     */
    public static String randomString(int length) {
        Preconditions.checkArgument(length > MIN_PASSWORD_LENGTH, "random string length must be at least 1 character");
        Preconditions.checkArgument(length <= MAX_PASSWORD_LENGTH, "random string length must be at most 256 character");
        
        return RandomStringUtils.random(length, 0, CHARACTERS.length-1, false, false, CHARACTERS, new SecureRandom());
    }

    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException e) {
            LOG.error("Failed to close resource quietly", e);
        }
    }
    
    /**
     * Converts a given file size into a readable file size including unit
     * 
     * @param size The size in bytes to convert
     * @return Readable files size, e.g. 24 MB
     */
    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        
        int index = (int) (Math.log10(size) / Math.log10(CONVERTION));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(CONVERTION, index)) + " " + UNITS[index];
    }
    
    /**
     * Checks if a resource exists in the classpath
     * 
     * @param name The name of the resource
     * @return True if the resources exists, false otherwise
     */
    public static boolean resourceExists(String name) {
        Objects.requireNonNull(name, Required.NAME.toString());
        
        URL resource = null;
        try {
            resource = Resources.getResource(name);            
        } catch (IllegalArgumentException e) { // NOSONAR Intentionally not logging or throwing this exception
           // Intentionally left blank
        }
        
        return resource != null;
    }
}