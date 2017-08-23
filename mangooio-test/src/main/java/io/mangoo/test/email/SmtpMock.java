package io.mangoo.test.email;

import java.util.Objects;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Required;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class SmtpMock {
    private GreenMail greenMail;
    private final Config config;

    @Inject
    public SmtpMock(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }

    public void start() {
        if (Application.inDevMode() || Application.inTestMode()) {
            this.greenMail = new GreenMail(new ServerSetup(this.config.getSmtpPort(), this.config.getSmtpHost(), Default.SMTP_SERVER_NAME.toString()));
            this.greenMail.start();
        }
    }

    public void stop() {
        if (this.greenMail != null) {
            this.greenMail.stop();
        }
    }

    public GreenMail getGreenMail() {
        return this.greenMail;
    }
}