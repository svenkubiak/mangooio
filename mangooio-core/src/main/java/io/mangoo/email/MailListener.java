package io.mangoo.email;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.core.Config;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooMailerException;
import io.mangoo.services.EventBusService;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

@Singleton
public class MailListener {
    private static final Logger LOG = LogManager.getLogger(MailListener.class);
    private Session session;
    
    @Inject
    public MailListener(Config config, EventBusService events) {
        Objects.requireNonNull(config, Required.CONFIG.toString());
        
        var properties = new Properties();
        properties.put("mail.smtp.host", config.getSmtpHost());
        properties.put("mail.smtp.port", String.valueOf(config.getSmtpPort()));
        properties.put("mail.from", config.getSmtpFrom());
        properties.put("mail.debug", String.valueOf(config.isSmtpDebug()));
        
        if (("smtps").equalsIgnoreCase(config.getSmtpProtocol())) {
            properties.put("mail.smtp.ssl.enable", "true");
        } else if (("smtptls").equalsIgnoreCase(config.getSmtpProtocol())) {
            properties.put("mail.smtp.starttls.enable", "true");
        }
        
        Authenticator authenticator = null;
        if (config.isSmtpAuthentication()) {
            properties.put("mail.smtp.auth", "true");
            authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getSmtpUsername(), config.getSmtpPassword());
                }
            };
        } else {
            properties.put("mail.smtp.auth", "false");
        }
        
        this.session = Session.getInstance(properties, authenticator);
    }
    
    /**
     * Processes the given mail message and passes it to the underlying SMTP handling
     * 
     * @param mail The mail to send
     * @throws MangooMailerException 
     */
    @Subscribe
    public void process(Mail mail) throws MangooMailerException {
        Objects.requireNonNull(mail, Required.MAIL.toString());

        try {
            var mimeMessage = new MimeMessage(session);
            mimeMessage.setSentDate(new Date());
            mimeMessage.setSubject(mail.getMessageSubject());

            setReplyTo(mail, mimeMessage);
            setHeaders(mail, mimeMessage);
            setRecipients(mail, mimeMessage);
            setCcs(mail, mimeMessage);
            setBccs(mail, mimeMessage);
            setFrom(mail, mimeMessage);
            setContent(mail, mimeMessage);
            setAttachments(mail, mimeMessage);

            Transport.send(mimeMessage);
        } catch (IOException | MessagingException e) {
            LOG.error("Failed to send mail", e);
            throw new MangooMailerException(e);
        }
    }

    private void setAttachments(Mail mail, MimeMessage mimeMessage) throws MessagingException, IOException {
        if (mail.hasAttachments()) {
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(mail.getMessageText());

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            for (Path path : mail.getMessageAttachments()) {
                messageBodyPart = new MimeBodyPart();
                var filename = path.toRealPath().toString();
                DataSource source = new FileDataSource(filename);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(filename);
                multipart.addBodyPart(messageBodyPart);
            }
            
            mimeMessage.setContent(multipart);
        }
    }

    private void setContent(Mail mail, MimeMessage mimeMessage) throws MessagingException {
        if (mail.isMessageHtml()) {
            mimeMessage.setContent(mail.getMessageText(), "text/html; charset=utf-8");
        } else {
            mimeMessage.setText(mail.getMessageText());
        }
    }

    private void setFrom(Mail mail, MimeMessage mimeMessage) throws MessagingException, UnsupportedEncodingException {
        String messageFromName = mail.getMessageFromName();
        String messageFromAddress = mail.getMessageFromAddress();
        
        if (StringUtils.isNotBlank(messageFromName) && StringUtils.isNotBlank(messageFromAddress)) {
            mimeMessage.setFrom(new InternetAddress(messageFromAddress, messageFromName));
        } else {
            mimeMessage.setFrom(new InternetAddress(messageFromAddress));
        }
    }

    private void setBccs(Mail mail, MimeMessage mimeMessage) throws MessagingException {
        for (String recipient : mail.getMessageBccs()) {
            mimeMessage.addRecipients(RecipientType.BCC, recipient);
        }
    }

    private void setCcs(Mail mail, MimeMessage mimeMessage) throws MessagingException {
        for (String recipient : mail.getMessageCcs()) {
            mimeMessage.addRecipients(RecipientType.CC, recipient);
        }
    }

    private void setRecipients(Mail mail, MimeMessage mimeMessage) throws MessagingException {
        for (String recipient : mail.getMessageTos()) {
            mimeMessage.addRecipients(RecipientType.TO, recipient);
        }
    }

    private void setHeaders(Mail mail, MimeMessage mimeMessage) throws MessagingException {
        for (Entry<String, String> entry : mail.getMessageHeaders().entrySet()) {
            mimeMessage.addHeader(entry.getKey(), entry.getValue());
        }
    }

    private void setReplyTo(Mail mail, MimeMessage mimeMessage) throws MessagingException {
        String replyTo = mail.getMessageReplyTo();
        if (StringUtils.isNotBlank(replyTo)) {
            InternetAddress[] replyToAddress = { new InternetAddress(replyTo) };
            mimeMessage.setReplyTo(replyToAddress);
        }
    }
}