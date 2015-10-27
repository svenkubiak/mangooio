package io.mangoo.test;

import org.fluentlenium.adapter.FluentTest;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

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
        MangooInstance.TEST.start();
    }

    @Override
    public WebDriver getDefaultDriver() {
        return webDriver;
    }

    public void beforeMangooStartup() {
        //Intentionally left blank for overwriting
    }
}