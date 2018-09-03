package io.mangoo.email;

import java.util.Objects;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Required;
import jodd.mail.Email;
import jodd.mail.MailServer;
import jodd.mail.MailServer.Builder;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;

@Singleton
public class MailEventListener {
    private final SmtpServer smtpServer;
    
    @Inject
    public MailEventListener(Config config) {
        Objects.requireNonNull(config,  Required.CONFIG.toString());
        
        Builder builder = MailServer.create()
                .host(config.getSmtpHost())
                .port(config.getSmtpPort());
        
        if (config.isSmtpSSL()) {
            builder.ssl(true).auth(config.getSmtpUsername(), config.getSmtpPassword());
        }
        
        this.smtpServer = builder.buildSmtpMailServer();
    }
    
    @Subscribe
    public void listen(Email email) {
        SendMailSession session = smtpServer.createSession();
        try {
            session.open();
            session.sendMail(email);  
        } finally {
            session.close();
        }
    }
}