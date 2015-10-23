package io.mangoo.test;

import org.fluentlenium.adapter.FluentTest;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.inject.Injector;

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

    public void beforeMangooStartup() {
        //Intentionally left blank for overwriting
    }

    @Override
    public WebDriver getDefaultDriver() {
        return webDriver;
    }
}