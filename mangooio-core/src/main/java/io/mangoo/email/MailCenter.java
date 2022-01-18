package io.mangoo.email;

import java.util.Objects;
import java.util.Properties;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.core.Config;
import io.mangoo.enums.Required;
import jakarta.mail.Authenticator;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;

@Singleton
public class MailCenter {
    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private Session session;
    
    @Inject
    public MailCenter(Config config) {
        Objects.requireNonNull(config, Required.CONFIG.toString());
        
        var properties = new Properties();
        properties.put("mail.smtp.host", config.getSmtpHost());
        properties.put("mail.smtp.port", config.getSmtpPort());
        properties.put("mail.from", config.getSmtpFrom());
        properties.put("mail.debug", config.isSmtpDebug() ? TRUE : FALSE);
        
        if (("smtps").equalsIgnoreCase(config.getSmtpProtocol())) {
            properties.put("mail.smtp.ssl.enable", TRUE);
        } else if (("smtptls").equalsIgnoreCase(config.getSmtpProtocol())) {
            properties.put("mail.smtp.ssl.enable", TRUE);
            properties.put("mail.smtp.starttls.enable", TRUE);
        }
        
        Authenticator authenticator = null;
        if (config.isSmtpAuthentication()) {
            authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getSmtpUsername(), config.getSmtpPassword());
                }
            };
        }
        
        this.session = Session.getInstance(properties, authenticator);
    }
    
    /**
     * Processes the given mail message and passes it to the underlying SMTP handling
     * 
     * @param mimeMessage The mimeMessage to send
     * @throws MessagingException
     */
    public void process(MimeMessage mimeMessage) throws MessagingException {
        Transport.send(mimeMessage);
    }
    
    public Session getSession() {
        return this.session;
    }
}