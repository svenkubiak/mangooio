package io.mangoo.test;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

import io.mangoo.core.Application;
import io.mangoo.enums.Mode;

/**
 *
 * @author svenkubiak
 *
 */ 
@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/*Test.class"})
@SuppressWarnings("all")
public class TestRunner {
    @BeforeClass
    public static final void start() {
        Application.start(Mode.TEST);
    }
}