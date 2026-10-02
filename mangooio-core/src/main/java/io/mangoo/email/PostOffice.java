package io.mangoo.email;

import io.mangoo.constants.Required;
import io.mangoo.core.Config;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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
    private final String defaultFrom;

    @Inject
    public PostOffice(Config config) {
        Objects.requireNonNull(config, Required.CONFIG);
        this.defaultFrom = config.getSmtpFrom();

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

    /** Sends the mail synchronously; failures are logged and not thrown to the caller. */
    public void send(Mail mail) {
        Objects.requireNonNull(mail, Required.MAIL);

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
        } catch (IOException | MessagingException | RuntimeException e) {
            // Sending runs on a virtual thread nobody waits for, so every failure has to end up in the log
            LOG.error("Failed to send mail", e);
        }
    }

    private void setAttachments(Mail mail, Part part) throws MessagingException {
        Objects.requireNonNull(mail, Required.MAIL);
        Objects.requireNonNull(part, Required.PART);

        if (mail.hasAttachments()) {
            var messageBodyPart = new MimeBodyPart();
            setContent(mail, messageBodyPart);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            for (Path path : mail.getMailAttachments()) {
                var fileName = path.getFileName();
                if (fileName == null || StringUtils.isBlank(fileName.toString())) {
                    throw new MessagingException("Attachment has no file name: " + path);
                }

                // The file is read from the given path, its name is only used as display name
                var attachmentPart = new MimeBodyPart();
                attachmentPart.setDataHandler(new DataHandler(new FileDataSource(path.toFile())));
                attachmentPart.setFileName(fileName.toString());
                multipart.addBodyPart(attachmentPart);
            }

            part.setContent(multipart);
        }
    }

    private void setContent(Mail mail, Part part) throws MessagingException {
        Objects.requireNonNull(mail, Required.MAIL);
        Objects.requireNonNull(part, Required.PART);

        if (mail.isMailHtml()) {
            part.setContent(mail.getMailText(), "text/html; charset=utf-8");
        } else {
            part.setText(mail.getMailText());
        }
    }

    private void setFrom(Mail mail, MimeMessage mimeMessage) throws MessagingException, UnsupportedEncodingException {
        Objects.requireNonNull(mail, Required.MAIL);
        Objects.requireNonNull(mimeMessage, Required.MIME_MESSAGE);

        String messageFromName = mail.getMailFromName();
        String messageFromAddress = mail.getMailFromAddress();

        if (StringUtils.isNotBlank(messageFromAddress)) {
            mimeMessage.setFrom(StringUtils.isNotBlank(messageFromName)
                    ? new InternetAddress(messageFromAddress, messageFromName)
                    : new InternetAddress(messageFromAddress));
        } else if (StringUtils.isNotBlank(defaultFrom)) {
            mimeMessage.setFrom(new InternetAddress(defaultFrom));
        } else {
            throw new MessagingException("Mail has no sender, set from() or smtp.from");
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

    private void setHeaders(Mail mail, Part mimeMessage) throws MessagingException {
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