package io.mangoo.test;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.suite.api.Suite;

import io.mangoo.core.Application;
import io.mangoo.enums.Mode;

/**
 *
 * @author svenkubiak
 *
 */ 
@SuppressWarnings("all")
@Suite(failIfNoTests = false)
public class TestRunner implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    private boolean started = false;
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!started) {
            beforeStartup();
            Application.start(Mode.TEST);  
            started = true;
            afterStartup();
        }
    }
    
    protected void beforeStartup() {
    }
    
    protected void afterStartup() {
    }
    
    @Override
    public void close() throws Throwable {
    }
}