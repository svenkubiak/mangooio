package io.mangoo.utils;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import com.google.common.reflect.ClassPath;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public final class MangooUtils {
    private static final Logger LOG = LogManager.getLogger(MangooUtils.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String [] UNITS = new String[] { "B", "kB", "MB", "GB", "TB" };
    private static final String VERSION_PROPERTIES = "version.properties";
    private static final String VERSION_UNKNOWN = "unknown";
    private static final char[] CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int MAX_LENGTH = 256;
    private static final int MIN_LENGTH = 0;
    private static final int CONVERSION = 1024;

    private MangooUtils() {
    }

    /**
     * Retrieves the current version of the framework from the version.properties file
     *
     * @return Current mangoo I/O version
     */
    @SuppressFBWarnings(justification = "Only used to retrieve the version of mangoo I/O", value = "URLCONNECTION_SSRF_FD")
    public static String getVersion() {
        var version = VERSION_UNKNOWN;
        try (var inputStream = Resources.getResource(VERSION_PROPERTIES).openStream()) {
            final var properties = new Properties();
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
        Objects.requireNonNull(originalMap, NotNull.MAP);
        
        return new HashMap<>(originalMap);
    }

    /**
     * Copies a given map to a new map instance
     *
     * @param originalMap The map to copy
     * @return A new Map instance with value from originalMap
     */
    public static Map<String, String> toStringMap(Map<String, Object> originalMap) {
        Objects.requireNonNull(originalMap, NotNull.MAP);

        return originalMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Objects.toString(e.getValue(), null)
                ));
    }

    /**
     * Generates a random string with the given length.
     * <p>
     * Based on commons-lang3 RandomStringUtils using SecureRandom
     * <p>
     * Uses: uppercase letters, lowercase letters and numbers 0-9
     * 
     * @param length The length of the random string
     * @return A random String
     */
    public static String randomString(int length) {
        Preconditions.checkArgument(length > MIN_LENGTH, "random string length must be at least 1 character");
        Preconditions.checkArgument(length <= MAX_LENGTH, "random string length must be at most 256 character");

        return RandomStringUtils.random(length, 0, CHARACTERS.length, false, false, CHARACTERS, SECURE_RANDOM);
    }

    /**
     * Closes a closeable without throwing an exception
     * 
     * @param closeable The closeable
     */
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
        
        int index = (int) (Math.log10(size) / Math.log10(CONVERSION));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(CONVERSION, index)) + " " + UNITS[index];
    }
    
    /**
     * Checks if a resource exists in the classpath
     * 
     * @param name The name of the resource
     * @return True if the resources exists, false otherwise
     */
    public static boolean resourceExists(String name) {
        Objects.requireNonNull(name, NotNull.NAME);
        
        URL resource = null;
        try {
            resource = Resources.getResource(name);            
        } catch (IllegalArgumentException e) { // NOSONAR Intentionally not logging or throwing this exception
           // Intentionally left blank
        }
        
        return resource != null;
    }

    /**
     * Reads the content of a file to a String
     * 
     * @param path The path of the file
     * @return The content of the file or null
     */
    public static String readFileToString(Path path) {
        Objects.requireNonNull(path, NotNull.PATH);
        
        var content = Strings.EMPTY;
        try {
            content = Files.readString(path);
        } catch (IOException e) {
            // Intentionally left blank
        }
        
        return content;
    }
    
    /**
     * Reads the content of a file to a String
     * 
     * @param path The path of the file
     * @return The content of the file or null
     */
    public static String readFileToString(String path) {
        Objects.requireNonNull(path, NotNull.PATH);

        return readFileToString(Path.of(path));
    }

    /**
     * Reads the content of a local resource to String
     * 
     * @param resource The resource path 
     * @return The content of the resource or null
     */
    public static String readResourceToString(String resource) {
        Objects.requireNonNull(resource, NotNull.RESOURCE);
        
        var content = Strings.EMPTY;
        try {
            content = Resources.toString(Resources.getResource(resource), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Intentionally left blank
        }
        
        return content;
    }

    /**
     * Checks the translations folder and retrieves all configured languages / messages bundles
     *
     * @return A set of configure messages bundles
     */
    public static Set<String> getLanguages() {
        var classLoader = Thread.currentThread().getContextClassLoader();
        Set<String> languages = new HashSet<>();

        try {
            var classPath = ClassPath.from(classLoader);
            for (ClassPath.ResourceInfo resourceInfo : classPath.getResources()) {
                String resourceName = resourceInfo.getResourceName();
                if (resourceName.startsWith("translations/") && resourceName.endsWith(".properties")) {
                    String fileName = resourceName.replace("translations/", "");
                    var langCode = StringUtils.substringBetween(fileName, "messages_", ".properties");
                    if (StringUtils.isNotBlank(langCode)) {
                        languages.add(langCode);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return languages;
    }
}