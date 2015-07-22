package io.mangoo.test;

import org.junit.Before;

import com.google.inject.Injector;
import com.icegreen.greenmail.util.GreenMail;

@SuppressWarnings("all")
public class MangooUnit {

    @Before
    public final void mangooStartup() {
        beforeMangooStartup();
        MangooTestInstance.IO.get();
    }

    public void beforeMangooStartup() {
    }

    public final Injector getInject() {
        return MangooTestInstance.IO.getInjector();
    }

    public final GreenMail getFakeSMTP() {
        return MangooTestInstance.IO.getFakeSMTP();
    }
}