package mangoo.io.testing;

import mangoo.io.core.Application;
import mangoo.io.enums.Key;
import mangoo.io.enums.Mode;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.google.inject.Injector;
import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import com.icegreen.greenmail.util.GreenMail;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/*Test.class"})
@SuppressWarnings("all")
public class MangooRunner {
    public static volatile Injector injector;
    public static volatile GreenMail fakeSMTP;
    
    @BeforeClass
    public static final void mangooStartup() {
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        Application.main(null);
        fakeSMTP = Application.getFakeSMTP();
        injector = Application.getInjector();
    }
}