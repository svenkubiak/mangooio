package io.mangoo.email;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.mail.Authenticator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooMailerException;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.interfaces.MangooTemplateEngine;

/**
 * 
 * @author svenkubiak
 *
 */
public class Mail {
    private List<File> files = new ArrayList<>();
    private List<String> recipients = new ArrayList<>();
    private List<String> ccRecipients = new ArrayList<>();
    private List<String> bccRecipients = new ArrayList<>();
    private Map<String, Object> content = new HashMap<>();
    private String template;
    private String subject;
    private String from;
    private String body;
    private boolean html;
    private boolean attachment;

    /**
     * Creates a new simple mail instance
     * 
     * @return Simple mail instance
     */
    public static Mail newMail(){
        return new Mail();
    }
    
    /**
     * Creates a new HTML mail instance
     * 
     * @return HTML mail instance
     */
    public static Mail newHtmlMail() {
        Mail mail = new Mail();
        mail.html = true;
        
        return mail;
    }
    
    /**
     * Adds a recipient to the mail
     * 
     * @param recipient The mail address of the recipient
     * @return A mail object instance
     */
    public Mail withRecipient(String recipient) {
        Objects.requireNonNull(recipient, Required.RECIPIENT.toString());
        
        this.recipients.add(recipient);
        return this;
    }
    
    /**
     * Adds a cc recipient to the mail
     * 
     * @param recipient The mail address of the cc recipient
     * @return A mail object instance
     */
    public Mail withCC(String recipient) {
        Objects.requireNonNull(recipient, Required.CC_RECIPIENT.toString());
        
        this.ccRecipients.add(recipient);
        return this;
    }
    
    /**
     * Adds a subject to the mail
     * 
     * @param subject The subject of the email
     * @return A mail object instance
     */
    public Mail withSubject(String subject) {
        Objects.requireNonNull(subject, Required.SUBJECT.toString());
        
        this.subject = subject;
        return this;
    }
    
    /**
     * Add a template to the mail which will be rendered before the mail is send
     * 
     * @param template The path to the template fail (e.g. emails/mail.ftl)
     * @return A mail object instance
     */
    public Mail withTemplate(String template) {
        Objects.requireNonNull(template, Required.TEMPLATE.toString());
        
        if (template.charAt(0) == '/' || template.startsWith("\\")) {
            this.template = template.substring(1, template.length());
        } else {
            this.template = template;
        }

        return this;
    }
    
    /**
     * @return The current template path
     */
    public String getTemplate() {
        return this.template;
    }
    
    /**
     * Adds a bcc recipient to the mail
     * 
     * @param recipient The subject of the email
     * @return A mail object instance
     */
    public Mail withBCC(String recipient) {
        Objects.requireNonNull(recipient, Required.BCC_RECIPIENT.toString());
        
        this.bccRecipients.add(recipient);
        return this;
    }
    
    /**
     * Adds a custom to the mail
     * 
     * This will overwrite rendering of the template!
     * 
     * @param body The body of the email
     * @return A mail object instance
     */
    public Mail withBody(String body) {
        Objects.requireNonNull(body, Required.BODY.toString());
        
        this.body = body;
        return this;
    }
    
    /**
     * Adds the from address to the mail

     * @param from The from address, e.g. jon.snow@winterfell.com
     * @return A mail object instance
     */
    public Mail withFrom(String from) {
        Objects.requireNonNull(from, Required.FROM.toString());
        
        this.from = from;
        return this;
    }
    
    /**
     * Adds a file attachment to the mail
     * 
     * @param file The file to add
     * @return A mail object instance
     */
    public Mail withAttachment(File file) {
        Objects.requireNonNull(file, Required.FILE.toString());
        
        this.attachment = true;
        this.files.add(file);
        return this;
    }
    
    /**
     * Mark the mail as an HTML mail
     * 
     * @deprecated As of version 4.4.0, will be private in 5.0.0., use {@link #newHtmlMail()} instead.
     * @return A mail object instance
     */
    @Deprecated
    public Mail isHtml() {
        this.html = true;
        return this;
    }
    
    /**
     * Adds content to the template which will be rendered
     * 
     * @param key The key 
     * @param value The value
     * @return A mail object instance
     */
    public Mail withContent(String key, Object value) {
        Objects.requireNonNull(key, Required.KEY.toString());
        Objects.requireNonNull(value, Required.VALUE.toString());
        
        content.put(key, value);
        return this;
    }
    
    /**
     * Sends the mail
     * 
     * @throws MangooMailerException when sending the mail failed
     */
    public void send() throws MangooMailerException {
        Config config = Application.getInstance(Config.class);
        if (StringUtils.isBlank(this.from)) {
            this.from = config.getSmtpFrom();
        }
        
        if (this.html) {
            sendHtmlEmail();
        } else if (this.attachment) {
            sendMultipartEmail();
        } else {
            sendSimpleEmail();
        }
    }

    private void sendSimpleEmail() throws MangooMailerException {
        Config config = Application.getInstance(Config.class);
        try {
            Email email = new SimpleEmail();
            email.setCharset(Default.ENCODING.toString());
            email.setHostName(config.getSmtpHost());
            email.setSmtpPort(config.getSmtpPort());
            email.setAuthenticator(getDefaultAuthenticator());
            email.setSSLOnConnect(config.isSmtpSSL());
            email.setFrom(this.from);
            email.setSubject(this.subject);
            email.setMsg(render());
            
            for (String recipient : this.recipients) {
                email.addTo(recipient);
            }
            
            for (String cc : this.ccRecipients) {
                email.addCc(cc);
            }
            
            for (String bcc : this.bccRecipients) {
                email.addBcc(bcc);
            }
            
            email.send();
        } catch (EmailException | MangooTemplateEngineException e) {
            throw new MangooMailerException(e);
        }        
    }

    private void sendMultipartEmail() throws MangooMailerException {
        Config config = Application.getInstance(Config.class);
        try {
            MultiPartEmail multiPartEmail = new MultiPartEmail();
            multiPartEmail.setCharset(Default.ENCODING.toString());
            multiPartEmail.setHostName(config.getSmtpHost());
            multiPartEmail.setSmtpPort(config.getSmtpPort());
            multiPartEmail.setAuthenticator(getDefaultAuthenticator());
            multiPartEmail.setSSLOnConnect(config.isSmtpSSL());
            multiPartEmail.setFrom(this.from);
            multiPartEmail.setSubject(this.subject);
            multiPartEmail.setMsg(render());
            
            for (String recipient : this.recipients) {
                multiPartEmail.addTo(recipient);
            }
            
            for (String cc : this.ccRecipients) {
                multiPartEmail.addCc(cc);
            }
            
            for (String bcc : this.bccRecipients) {
                multiPartEmail.addBcc(bcc);
            }
            
            for (File file : this.files) {
                multiPartEmail.attach(file);
            }
            
            multiPartEmail.send();
        } catch (EmailException | MangooTemplateEngineException e) {
            throw new MangooMailerException(e);
        }   
    }

    private void sendHtmlEmail() throws MangooMailerException {
        Config config = Application.getInstance(Config.class);
        try {
            HtmlEmail htmlEmail = new HtmlEmail();
            htmlEmail.setCharset(Default.ENCODING.toString());
            htmlEmail.setHostName(config.getSmtpHost());
            htmlEmail.setSmtpPort(config.getSmtpPort());
            htmlEmail.setAuthenticator(getDefaultAuthenticator());
            htmlEmail.setSSLOnConnect(config.isSmtpSSL());
            htmlEmail.setFrom(this.from);
            htmlEmail.setSubject(this.subject);
            htmlEmail.setHtmlMsg(render());
            
            for (String recipient : this.recipients) {
                htmlEmail.addTo(recipient);
            }
            
            for (String cc : this.ccRecipients) {
                htmlEmail.addCc(cc);
            }
            
            for (String bcc : this.bccRecipients) {
                htmlEmail.addBcc(bcc);
            }
            
            for (File file : this.files) {
                htmlEmail.attach(file);
            }
            
            htmlEmail.send();
        } catch (EmailException | MangooTemplateEngineException e) {
            throw new MangooMailerException(e);
        } 
    }
    
    private Authenticator getDefaultAuthenticator() {
        Config config = Application.getInstance(Config.class);
        
        DefaultAuthenticator defaultAuthenticator = null;
        final String username = config.getSmtpUsername();
        final String password = config.getSmtpPassword();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            defaultAuthenticator = new DefaultAuthenticator(username, password);
        }
        
        return defaultAuthenticator;
    }

    private String render() throws MangooTemplateEngineException {
        return StringUtils.isNotBlank(this.body) ? this.body : Application.getInstance(MangooTemplateEngine.class).render("", this.template, this.content);
    }
}