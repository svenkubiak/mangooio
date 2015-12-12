package io.mangoo.test;

import org.junit.Before;
import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

/**
 *
 * @author svenkubiak
 *
 */
@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/*Test.class"})
public class MangooRunner {

    @Before
    public final void mangooStart() {
        Mangoo.TEST.start();
    }
}