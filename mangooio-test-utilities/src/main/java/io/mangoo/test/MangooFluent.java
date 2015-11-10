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
    private final WebDriver webDriver = new HtmlUnitDriver();
    
    @Before
    public final void mangooStartup() {
        if (!MangooInstance.TEST.isStarted()) {
            beforeMangooStartup();
            MangooInstance.TEST.start();
        }
    }

    @Override
    public WebDriver getDefaultDriver() {
        return webDriver;
    }

    public void beforeMangooStartup() {
        //Intentionally left blank for overwriting
    }
}