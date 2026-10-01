package io.mangoo.test;

import io.mangoo.core.Application;
import io.mangoo.enums.Mode;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.suite.api.Suite;

@SuppressWarnings("all")
@Suite(failIfNoTests = false)
public class TestRunner implements BeforeAllCallback, AutoCloseable {
    private static final Object LOCK = new Object();
    private static boolean started = false;

    // JUnit may run beforeAll of several test classes concurrently, so the startup is guarded globally to avoid binding the ports twice.
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