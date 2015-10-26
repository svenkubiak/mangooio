package io.mangoo.test;

import org.junit.Before;
import org.junit.runner.RunWith;

import com.google.inject.Injector;
import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

/**
 * 
 * @author svenkubiak
 *
 */
@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/*Test.class"})
@SuppressWarnings("all")
public class MangooRunner {

    @Before
    public final void mangooStartup() {
        beforeMangooStartup();
        MangooTestInstance.IO.get();
    }

    public void beforeMangooStartup() {
    }

    public final Injector getInject() {
        return MangooTestInstance.IO.getInjector();
    }
}