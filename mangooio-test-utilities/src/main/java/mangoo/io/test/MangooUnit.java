package mangoo.io.test;

import mangoo.io.core.Application;
import mangoo.io.enums.Key;
import mangoo.io.enums.Mode;

import org.fluentlenium.adapter.FluentTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.inject.Injector;
import com.icegreen.greenmail.util.GreenMail;

@SuppressWarnings("all")
public class MangooUnit {
   
	@Before
    public final void mangooStartup() {
    	beforeMangooStartup();
    	MangooTest.INSTANCE.getInstance();
    }

	public void beforeMangooStartup() {
	}

	public final Injector getInject() {
        return MangooTest.INSTANCE.getInjector();
    }
    
    public final GreenMail getFakeSMTP() {
    	return MangooTest.INSTANCE.getFakeSMTP();
    }
}