package io.mangoo.email;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.mangoo.core.Config;
import io.mangoo.enums.Required;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

@Singleton
public class PostOffice {
    private static final Logger LOG = LogManager.getLogger(PostOffice.class);
    private final Session session;

    @Inject
    public PostOffice(Config config) {
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
     */
    public void send(Mail mail) {
        Objects.requireNonNull(mail, Required.MAIL.toString());

        try {
            var mimeMessage = new MimeMessage(session);
            mimeMessage.setSentDate(new Date());
            mimeMessage.setSubject(mail.getMailSubject());

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
        }
    }

    private void setAttachments(Mail mail, Part part) throws MessagingException, IOException {
        Objects.requireNonNull(mail, Required.MAIL.toString());
        Objects.requireNonNull(part, Required.PART.toString());

        if (mail.hasAttachments()) {
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(mail.getMailText());

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            for (Path path : mail.getMailAttachments()) {
                messageBodyPart = new MimeBodyPart();
                var filename = path.toRealPath().toString();
                DataSource source = new FileDataSource(filename);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(filename);
                multipart.addBodyPart(messageBodyPart);
            }

            part.setContent(multipart);
        }
    }

    private void setContent(Mail mail, Part part) throws MessagingException {
        Objects.requireNonNull(mail, Required.MAIL.toString());
        Objects.requireNonNull(part, Required.PART.toString());

        if (mail.isMailHtml()) {
            part.setContent(mail.getMailText(), "text/html; charset=utf-8");
        } else {
            part.setText(mail.getMailText());
        }
    }

    private void setFrom(Mail mail, MimeMessage mimeMessage) throws MessagingException, UnsupportedEncodingException {
        Objects.requireNonNull(mail, Required.MAIL.toString());
        Objects.requireNonNull(mimeMessage, Required.MIME_MESSAGE.toString());

        String messageFromName = mail.getMailFromName();
        String messageFromAddress = mail.getMailFromAddress();

        if (StringUtils.isNotBlank(messageFromName) && StringUtils.isNotBlank(messageFromAddress)) {
            mimeMessage.setFrom(new InternetAddress(messageFromAddress, messageFromName));
        } else {
            mimeMessage.setFrom(new InternetAddress(messageFromAddress));
        }
    }

    private void setBccs(Mail mail, MimeMessage mimeMessage) throws MessagingException {
        for (String recipient : mail.getMailBccs()) {
            mimeMessage.addRecipients(Message.RecipientType.BCC, recipient);
        }
    }

    private void setCcs(Mail mail, MimeMessage mimeMessage) throws MessagingException {
        for (String recipient : mail.getMailCcs()) {
            mimeMessage.addRecipients(Message.RecipientType.CC, recipient);
        }
    }

    private void setRecipients(Mail mail, MimeMessage mimeMessage) throws MessagingException { //NOSONAR
        for (String recipient : mail.getMailTos()) {
            mimeMessage.addRecipients(Message.RecipientType.TO, recipient);
        }
    }

    private void setHeaders(Mail mail, MimeMessage mimeMessage) throws MessagingException {
        for (Entry<String, String> entry : mail.getMailHeaders().entrySet()) {
            mimeMessage.addHeader(entry.getKey(), entry.getValue());
        }
    }

    private void setReplyTo(Mail mail, MimeMessage mimeMessage) throws MessagingException {
        String replyTo = mail.getMailReplyTo();
        if (StringUtils.isNotBlank(replyTo)) {
            InternetAddress[] replyToAddress = {new InternetAddress(replyTo)};
            mimeMessage.setReplyTo(replyToAddress);
        }
    }
}