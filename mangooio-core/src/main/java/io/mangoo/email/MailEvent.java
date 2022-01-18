package io.mangoo.email;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooMailerException;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

/**
 * 
 * @author svenkubiak
 *
 */
public class MailEvent {
    private static final Logger LOG = LogManager.getLogger(MailEvent.class);
    private MailCenter mailCenter;
    
    @Inject
    public MailEvent(MailCenter mailCenter) {
        this.mailCenter = Objects.requireNonNull(mailCenter, Required.MAIL_CENTER.toString());
    }

    /**
     * Sends a mail through the configured SMTP server
     * 
     * @param mail The mail to send
     * @throws MangooMailerException
     */
    public void send(Mail mail) throws MangooMailerException {
        Objects.requireNonNull(mail, Required.MAIL.toString());
        
        try {
            MimeMessage mimeMessage = new MimeMessage(mailCenter.getSession());
            mimeMessage.setSentDate(new Date());
            mimeMessage.setSubject(mail.getMessageSubject());
            
            String replyTo = mail.getMessageReplyTo();
            if (StringUtils.isNotBlank(replyTo)) {
                InternetAddress [] replyToAddress = { new InternetAddress(replyTo) };
                mimeMessage.setReplyTo(replyToAddress);
            }
            
            for (Entry<String, String> entry : mail.getMessageHeaders().entrySet()) {
                mimeMessage.addHeader(entry.getKey(), entry.getValue());
            }
            
            for (String recipient : mail.getMessageTos()) {
                mimeMessage.addRecipients(RecipientType.TO, recipient);
            }
            
            for (String recipient : mail.getMessageCcs()) {
                mimeMessage.addRecipients(RecipientType.CC, recipient);
            }
            
            for (String recipient : mail.getMessageBccs()) {
                mimeMessage.addRecipients(RecipientType.BCC, recipient);
            }
            
            String messageFromName = mail.getMessageFromName();
            String messageFromAddress = mail.getMessageFromAddress();
            if (StringUtils.isNotBlank(messageFromName) && StringUtils.isNotBlank(messageFromAddress)) {
                mimeMessage.setFrom(new InternetAddress(messageFromAddress, messageFromName));
            } else {
                mimeMessage.setFrom(new InternetAddress(messageFromAddress));
            }
            
            if (mail.isMessageHtml()) {
                mimeMessage.setContent(mail.getMessageText(), "text/html; charset=utf-8");
            } else {
                mimeMessage.setText(mail.getMessageText());
            }
            
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
            
            mailCenter.process(mimeMessage);
        } catch (IOException | MessagingException e) {
            LOG.error("Failed to send email", e);
            throw new MangooMailerException(e);
        } 
    }
}