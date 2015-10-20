package io.mangoo.core;

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
        bootstrap.prepareLogging();
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

    public static Mode getMode() {
        return mode;
    }

    public static Injector getInjector() {
        return injector;
    }

    public static GreenMail getGreenMail() {
        return greenMail;
    }
}