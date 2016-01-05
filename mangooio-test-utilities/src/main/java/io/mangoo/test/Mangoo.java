package io.mangoo.test;

import java.util.Objects;

import com.google.inject.Injector;

import io.mangoo.core.Application;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;

/**
 * 
 * @author svenkubiak
 *
 */
public enum Mangoo {
    TEST;
    
    private Mangoo() {
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        Application.main();
        
        if (!Application.isStarted()) {
            System.out.print("Failed to start mangoo I/O application"); //NOSONAR
            System.exit(1); //NOSONAR
        }
    }

    public void start() {
        //intentionally does nothing, just used for better readability
    }

    public Injector getInjector() {
        return Application.getInjector();
    }
    
    public <T> T getInstance(Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz can not be null");
        return Application.getInstance(clazz);
    }
}