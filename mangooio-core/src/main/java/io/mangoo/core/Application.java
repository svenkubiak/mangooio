package io.mangoo.core;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.inject.Injector;

import io.mangoo.cache.Cache;
import io.mangoo.cache.GuavaCache;
import io.mangoo.cache.HazlecastCache;
import io.mangoo.configuration.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Mode;
import io.mangoo.templating.TemplateEngine;
import io.mangoo.templating.freemarker.TemplateEngineFreemarker;

/**
 * Main class that starts all components of a mangoo I/O application
 *
 * @author svenkubiak
 *
 */
public final class Application {
    private static volatile Cache cache;
    private static volatile TemplateEngine templateEngine;
    private static volatile Config config;
    private static volatile Mode mode;
    private static volatile Injector injector;
    private static volatile LocalDateTime start;
    private static volatile boolean started;
    private static volatile String baseDirectory;

    private Application() {
    }

    public static void main(String... args) {
        final Bootstrap bootstrap = new Bootstrap();
        start = bootstrap.getStart();
        mode = bootstrap.prepareMode();
        injector = bootstrap.prepareInjector();
        baseDirectory = bootstrap.preparteBaseDirectory();
        bootstrap.prepareLogger();
        bootstrap.applicationInitialized();
        bootstrap.prepareConfig();
        bootstrap.parseRoutes();
        bootstrap.startQuartzScheduler();
        bootstrap.startUndertow();
        bootstrap.showLogo();
        bootstrap.applicationStarted();

        if (bootstrap.isBootstrapSuccessful()) {
            getInstance(Config.class).decrypt();
            started = true;
        } else {
            System.out.print("Failed to start mangoo I/O application"); //NOSONAR
            System.exit(1); //NOSONAR
        }
    }

    /**
     * Checks if the application is running in dev mode
     *
     * @return True if the application is running in dev mode, false otherwise
     */
    public static boolean inDevMode() {
        return Mode.DEV.equals(mode);
    }

    /**
     * Checks if the application is running in prod mode
     *
     * @return True if the application is running in prod mode, false otherwise
     */
    public static boolean inProdMode() {
        return Mode.PROD.equals(mode);
    }

    /**
     * Checks if the application is running in test mode
     *
     * @return True if the application is running in test mode, false otherwise
     */
    public static boolean inTestMode() {
        return Mode.TEST.equals(mode);
    }

    /**
     * Returns the current mode the application is running in
     *
     * @return Enum Mode
     */
    public static Mode getMode() {
        return mode;
    }

    /**
     * Returns the Google Guice Injector
     *
     * @return Google Guice injector instance
     */
    public static Injector getInjector() {
        return injector;
    }

    /**
     * @return True if the application started successfully, false otherwise
     */
    public static boolean isStarted() {
        return started;
    }

    /**
     * @return The LocalDateTime of the application start
     */
    public static LocalDateTime getStart() {
        return start;
    }

    /**
     * @return An instance of the current application config
     */
    public static Config getConfig() {
        Objects.requireNonNull(mode, "cant't create config instance without application mode");

        if (config == null) {
            config = new Config();
        }

        return config;
    }

    /**
     * @return An instance of the internal template engine freemarker
     */
    public static TemplateEngine getInternalTemplateEngine() {
        if (templateEngine == null) {
            templateEngine = new TemplateEngineFreemarker();
        }

        return templateEngine;
    }

    /**
     * @return An instance of the internal cache
     */
    public static Cache getInternalCache() {
        if (cache == null) {
            if (Default.CACHE_CLASS.toString().equals(config.getCacheClass())) {
                cache = new GuavaCache();
            } else {
                cache = new HazlecastCache();
            }
        }

        return cache;
    }

    /**
     * @return The duration of the application uptime
     */
    public static Duration getUptime() {
        Objects.requireNonNull(start, "Can not calculate duration without application start time");

        return Duration.between(start, LocalDateTime.now());
    }

    /**
     * Short form for getting an Goolge Guice injected class by
     * calling injector.getInstance(...)
     *
     * @param clazz The class to retrieve from the injector
     * @param <T> JavaDoc requires this (just ignore it)
     *
     * @return An instance of the requested class
     */
    public static <T> T getInstance(Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz can not be null");

        return injector.getInstance(clazz);
    }

    /**
     * @return A list of all administrative URLs
     */
    public static List<String> getAdministrativeURLs() {
        return Arrays.asList("@cache", "@metrics", "@config", "@routes", "@health", "@scheduler", "@memory", "@system");
    }

    /**
     * @return The system specific base directory to
     */
    public static String getBaseDirectory() {
        return baseDirectory;
    }
}