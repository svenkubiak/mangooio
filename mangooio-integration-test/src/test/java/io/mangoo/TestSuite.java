package io.mangoo;

import org.junit.BeforeClass;

import io.mangoo.core.Application;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;
import io.mangoo.test.SimpleTestRunner;

public class TestSuite extends SimpleTestRunner {
    public static final int THREADS = 100;
    
    @BeforeClass
    public static final void start() {
        System.setProperty(Key.APPLICATION_PRIVATEKEY.toString(), "./key/privatekey.txt");
        Application.start(Mode.TEST);
    }
}