package mangoo.io.test;

import org.fluentlenium.adapter.FluentTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.inject.Injector;
import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import com.icegreen.greenmail.util.GreenMail;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/*Test.class"})
@SuppressWarnings("all")
public class MangooRunner {
	
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