package mangoo.io.testing;

import mangoo.io.core.Application;
import mangoo.io.enums.Key;
import mangoo.io.enums.Mode;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.google.inject.Injector;
import com.icegreen.greenmail.util.GreenMail;

@SuppressWarnings("all")
public class MangooUnit {
    private static volatile Injector injector;
    private static volatile GreenMail fakeSMTP;
    
    @BeforeClass
    public static final void mangooStartup() {
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        Application.main(null);
        fakeSMTP = Application.getFakeSMTP();
        injector = Application.getInjector();
    }
    
    @AfterClass
    public static final void mangooShutdown() {
        Application.stopFakeSMTP();
        Application.stopServer();
    }
    
    public static Injector getInject() {
        return injector;
    }
    
    public static GreenMail getFakeSMTP() {
        return fakeSMTP;
    }
}