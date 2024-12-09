package io.mangoo.test.email;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Application;
import io.mangoo.core.Config;

import java.util.Objects;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class SmtpMock {
    private static final String SMTP_SERVER_NAME = "smtp";
    private final Config config;
    private GreenMail greenMail;


    @Inject
    public SmtpMock(Config config) {
        this.config = Objects.requireNonNull(config, NotNull.CONFIG);
    }

    public void start() {
        if (Application.inDevMode() || Application.inTestMode()) {
            this.greenMail = new GreenMail(new ServerSetup(this.config.getSmtpPort(), this.config.getSmtpHost(), SMTP_SERVER_NAME));
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