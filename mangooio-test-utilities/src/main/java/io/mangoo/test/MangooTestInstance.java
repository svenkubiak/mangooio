package io.mangoo.test;

import com.google.inject.Injector;
import com.icegreen.greenmail.util.GreenMail;

import io.mangoo.core.Application;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;

public enum MangooTestInstance {
    IO;
    private GreenMail fakeSMTP;
    private Injector injector;

    MangooTestInstance() {
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        Application.main(null);
        this.fakeSMTP = Application.getFakeSMTP();
        this.injector = Application.getInjector();
    }

    public MangooTestInstance get() {
        return IO;
    }

    public GreenMail getFakeSMTP() {
        return this.fakeSMTP;
    }

    public Injector getInjector() {
        return this.injector;
    }
}