package io.mangoo.test;

import com.google.inject.Injector;

import io.mangoo.core.Application;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;

public enum MangooTestInstance {
    IO;
    private Injector injector;

    MangooTestInstance() {
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        Application.main(null);
        this.injector = Application.getInjector();
    }

    public MangooTestInstance get() {
        return IO;
    }

    public Injector getInjector() {
        return this.injector;
    }
}