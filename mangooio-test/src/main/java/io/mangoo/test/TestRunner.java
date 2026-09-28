package io.mangoo.test;

import io.mangoo.core.Application;
import io.mangoo.enums.Mode;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.suite.api.Suite;

/**
 *
 * @author svenkubiak
 *
 */ 
@SuppressWarnings("all")
@Suite(failIfNoTests = false)
public class TestRunner implements BeforeAllCallback, AutoCloseable {
    private static final Object LOCK = new Object();
    private static boolean started = false;

    /*
     * JUnit creates one extension instance per test class and may invoke beforeAll of
     * several classes at the same time. The startup must therefore be guarded across
     * instances, a second concurrent Application#start would bind the same ports twice
     */
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        synchronized (LOCK) {
            if (!started) {
                beforeStartup();
                Application.start(Mode.TEST);
                started = true;
                afterStartup();
            }
        }
    }
    
    protected void beforeStartup() {
    }
    
    protected void afterStartup() {
    }

    @Override
    public void close() throws Exception {
    }
}