package mangoo.io.test;

import org.fluentlenium.adapter.FluentTest;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.inject.Injector;
import com.icegreen.greenmail.util.GreenMail;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooFluent extends FluentTest {
    private WebDriver webDriver = new HtmlUnitDriver();

    @Before
    public final void mangooStartup() {
        beforeMangooStartup();
        MangooTestInstance.IO.getInjector();
    }

    public final Injector getInject() {
        return MangooTestInstance.IO.getInjector();
    }

    public final GreenMail getFakeSMTP() {
        return MangooTestInstance.IO.getFakeSMTP();
    }

    public void beforeMangooStartup() {
        //Intentionally left blank for overwriting
    }

    @Override
    public WebDriver getDefaultDriver() {
        return webDriver;
    }
}