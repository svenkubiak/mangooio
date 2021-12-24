package io.mangoo.email;

import java.util.Objects;

import org.simplejavamail.MailException;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.internal.MailerRegularBuilderImpl;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.core.Config;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooMailerException;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class MailEvent {
    private final Mailer mailer;
    
    @Inject
    public MailEvent(Config config) {
        Objects.requireNonNull(config, Required.CONFIG.toString());
        
        MailerRegularBuilderImpl mailerBuilder = MailerBuilder
            .withDebugLogging(config.isSmtpDebug())
            .withSMTPServerHost(config.getSmtpHost())
            .withSMTPServerPort(config.getSmtpPort());
        
        if (config.isSmtpAuthentication()) {
            mailerBuilder.withSMTPServerUsername(config.getSmtpUsername());
            mailerBuilder.withSMTPServerPassword(config.getSmtpPassword());
        }
        
        if (("smtps").equalsIgnoreCase(config.getSmtpProtocol())) {
            mailerBuilder.withTransportStrategy(TransportStrategy.SMTPS);
        } else if (("smtptls").equalsIgnoreCase(config.getSmtpProtocol())) {
            mailerBuilder.withTransportStrategy(TransportStrategy.SMTP_TLS);
        } else {
            mailerBuilder.withTransportStrategy(TransportStrategy.SMTP);
        }
        
        mailer = mailerBuilder.buildMailer();
    }

    public void send(Email email) throws MangooMailerException {
        try {
            mailer.sendMail(email, true); 
        } catch (MailException e) {
            throw new MangooMailerException(e);
        }
    }
}