package io.mangoo.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;
import com.google.inject.Injector;
import com.icegreen.greenmail.util.GreenMail;

import io.mangoo.annotations.Schedule;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;

/**
 *
 * @author svenkubiak
 *
 */
public final class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private static volatile Mode mode;
    private static volatile Injector injector;
    private static volatile GreenMail fakeSMTP;

    private Application() {
    }

    public static void main( String[] args ) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.prepareApplication();

        bootstrap.prepareMode();
        mode = bootstrap.getMode();

        bootstrap.prepareLogging();
        bootstrap.prepareInjector();
        injector = bootstrap.getInjector();

        bootstrap.prepareConfig();
        bootstrap.prepareRoutes();
        bootstrap.startFakeSMTP();
        fakeSMTP = bootstrap.getFakeSMTP();

        bootstrap.startScheduler();
        bootstrap.startServer();
        bootstrap.applicationStarted();

        if (!bootstrap.isStarted()) {
            System.out.print("Failed to start mangoo I/O"); //NOSONAR
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

    public static Mode getMode() {
        return mode;
    }

    public static Injector getInjector() {
        return injector;
    }

    public static GreenMail getFakeSMTP() {
        return fakeSMTP;
    }

    public static String getVersion() {
        String version = Default.VERSION.toString();
        try (InputStream inputStream = Resources.getResource(Default.VERSION_PROPERTIES.toString()).openStream()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            version = String.valueOf(properties.get(Key.VERSION.toString()));
        } catch (IOException e) {
            LOG.error("Failed to get application version", e);
        }

        return version;
    }
}