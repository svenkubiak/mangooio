package mangoo.io.core;

import io.undertow.Undertow;
import mangoo.io.enums.Mode;

import com.google.inject.Injector;
import com.icegreen.greenmail.util.GreenMail;

/**
 *
 * @author svenkubiak
 *
 */
public final class Application {
    private static volatile Mode mode;
    private static volatile Injector injector;
    private static volatile GreenMail fakeSMTP;
    private static volatile Undertow undertow;
    
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

        bootstrap.startServer();
        undertow = bootstrap.getServer();

        bootstrap.applicationStarted();
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

    public static void stopServer() {
        undertow.stop();
    }

    public static void stopFakeSMTP() {
        fakeSMTP.stop();
    }
}