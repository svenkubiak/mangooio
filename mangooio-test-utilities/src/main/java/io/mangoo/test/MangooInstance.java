package io.mangoo.test;

import com.google.inject.Injector;

import io.mangoo.core.Application;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;

/**
 * 
 * @author svenkubiak
 *
 */
public enum MangooInstance {
    TEST;
    private Injector injector;

    MangooInstance() {
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        Application.main(null);
        this.injector = Application.getInjector();
    }

    public void start() {
    }

    public Injector getInjector() {
        return this.injector;
    }
    
    public <T> T getInstance(Class<T> clazz) {
        return this.injector.getInstance(clazz);
    }
}