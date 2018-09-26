package io.mangoo.test;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.mangoo.core.Application;
import io.mangoo.enums.Mode;

/**
 *
 * @author svenkubiak
 *
 */ 
@SuppressWarnings("all")
public class TestRunner implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    private static boolean started = false;
    
    protected void init() {
    }
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!started) {
            init();
            Application.start(Mode.TEST);  
            started = true;
        }
    }

    @Override
    public void close() throws Throwable {
    }
}