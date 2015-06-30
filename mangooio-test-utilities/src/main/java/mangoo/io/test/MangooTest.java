package mangoo.io.test;

import com.google.inject.Injector;
import com.icegreen.greenmail.util.GreenMail;

import mangoo.io.core.Application;
import mangoo.io.enums.Key;
import mangoo.io.enums.Mode;

public enum MangooTest {
    INSTANCE;
    private GreenMail fakeSMTP;
    private Injector injector;

    MangooTest() {
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        Application.main(null);
        this.fakeSMTP = Application.getFakeSMTP();
        this.injector = Application.getInjector();
    }

    public MangooTest getInstance() {
        return INSTANCE;
    }

    public GreenMail getFakeSMTP() {
        return this.fakeSMTP;
    }

    public Injector getInjector() {
        return this.injector;
    }
}