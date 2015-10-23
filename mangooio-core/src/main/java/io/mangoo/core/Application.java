package io.mangoo.core;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.icegreen.greenmail.util.GreenMail;

import io.mangoo.enums.Mode;

/**
 * Main class that starts all components of a mangoo I/O application
 * 
 * @author svenkubiak
 *
 */
public final class Application {
    private static volatile Mode mode;
    private static volatile Injector injector;
    private static volatile GreenMail greenMail;

    private Application() {
    }

    public static void main( String[] args ) {
        Bootstrap bootstrap = new Bootstrap();
        mode = bootstrap.prepareMode();
        injector = bootstrap.prepareInjector();
        bootstrap.applicationInitialized();
        bootstrap.prepareConfig();
        bootstrap.prepareRoutes();
        greenMail = bootstrap.startGreenMail();
        bootstrap.startQuartzScheduler();
        bootstrap.startUndertow();
        bootstrap.showLogo();
        bootstrap.applicationStarted();

        if (!bootstrap.isApplicationStarted()) {
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

    public static GreenMail getGreenMail() {
        return greenMail;
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
        Preconditions.checkNotNull(clazz, "Missing class instance for getInstance");
        
        return injector.getInstance(clazz);
    }
}