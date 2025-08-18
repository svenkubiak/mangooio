package io.mangoo;

import io.mangoo.constants.Key;
import io.mangoo.test.TestRunner;

public class TestExtension extends TestRunner {
    public static final int THREADS = 10;

    @Override
    public void beforeStartup() {
        System.setProperty(Key.APPLICATION_NAME, "namefromarg");
        System.setProperty("application.test", "valuefromarg");
    }
}