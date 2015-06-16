package mangoo.io.testing;

import mangoo.io.core.Application;
import mangoo.io.enums.Key;

import org.fluentlenium.adapter.FluentTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.inject.Injector;
import com.icegreen.greenmail.util.GreenMail;

@SuppressWarnings("all")
public class MangooUnit extends FluentTest {
    private static volatile Injector injector;
    private static volatile GreenMail fakeSMTP;
    public WebDriver webDriver = new HtmlUnitDriver();

    @BeforeClass
    public static final void mangooStartup() {
        System.setProperty(Key.APPLICATION_MODE.toString(), mangoo.io.enums.Mode.TEST.toString());
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

    @Override
    public WebDriver getDefaultDriver() {
        return webDriver;
    }
}