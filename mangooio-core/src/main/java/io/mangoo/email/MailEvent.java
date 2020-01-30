package io.mangoo.email;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.internal.MailerRegularBuilderImpl;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.core.Config;
import io.mangoo.enums.Required;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class MailEvent {
    private Mailer mailer;
    
    @Inject
    public MailEvent(Config config) {
        Objects.requireNonNull(config, Required.CONFIG.toString());
        
        MailerRegularBuilderImpl mailerBuilder = MailerBuilder
            .withDebugLogging(config.isSmtpDebug())
            .withSMTPServerHost(config.getSmtpHost())
            .withSMTPServerPort(config.getSmtpPort());
        
        //FIX ME: refactor to config.isSMTPAuthentication()
        if (StringUtils.isNotBlank(config.getSmtpUsername()) && StringUtils.isNotBlank(config.getSmtpPassword())) {
            mailerBuilder.withSMTPServerUsername(config.getSmtpUsername());
            mailerBuilder.withSMTPServerPassword(config.getSmtpPassword());
        }
        
        //FIX ME: refactor to different SMTP strategies
        if (config.isSmtpSSL()) {
            mailerBuilder.withTransportStrategy(TransportStrategy.SMTPS);
            //FIX ME: Missing SMTP_TLS
        } else {
            mailerBuilder.withTransportStrategy(TransportStrategy.SMTP);
        }
        
        this.mailer = mailerBuilder.buildMailer();
    }

    public void send(Email email) {
        this.mailer.sendMail(email, true);
    }
}