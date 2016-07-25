package io.mangoo.email;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.exceptions.MangooMailerException;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.templating.TemplateEngine;
import net.jawr.web.resource.FileNameUtils;

/**
 * 
 * @author svenkubiak
 *
 */
public class Mail {
    private static volatile DefaultAuthenticator defaultAuthenticator;
    private final Map<String, Object> content = new HashMap<>();
    private final List<File> files = new ArrayList<>();
    private final List<String> recipients = new ArrayList<>();
    private final List<String> ccRecipients = new ArrayList<>();
    private final List<String> bccRecipients = new ArrayList<>();
    private String template;
    private String subject;
    private String from;
    private String body;
    private static volatile String host;
    private static int port;
    private boolean html;
    private boolean attachment;
    private static boolean ssl;

    public static Mail newMail(){
        init();
        return new Mail();
    }
    
    public static Mail newHtmlMail() {
        FileNameUtils.getName("fofofof");
        
        init();
        return new Mail().isHtml();
    }

    private static void init() {
        Config config = Application.getInstance(Config.class);
        host = config.getSmtpHost();
        port = config.getSmtpPort();
        ssl = config.isSmtpSSL();

        final String username = config.getSmtpUsername();
        final String password = config.getSmtpPassword();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            defaultAuthenticator = new DefaultAuthenticator(username, password);
        }
    }
    
    /**
     * Adds a recipient to the mail
     * 
     * @param recipient The mail address of the recipient
     * @return A mail object instance
     */
    public Mail withRecipient(String recipient) {
        Objects.requireNonNull(recipient, "recipient can not be null");
        
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
        Objects.requireNonNull(recipient, "cc recipient can not be null");
        
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
        Objects.requireNonNull(subject, "subject can not be null");
        
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
        Objects.requireNonNull(template, "template can not be null");
        
        if (template.charAt(0) == '/' || template.startsWith("\\")) {
            template = template.replaceFirst("/", "");
            template = template.replaceFirst("\\", "");
        }
        
        this.template = template;
        return this;
    }
    
    /**
     * Adds a bcc recipient to the mail
     * 
     * @param recipient The subject of the email
     * @return A mail object instance
     */
    public Mail withBCC(String recipient) {
        Objects.requireNonNull(recipient, "bcc recipient can not be null");
        
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
        Objects.requireNonNull(body, "body can not be null");
        
        this.body = body;
        return this;
    }
    
    /**
     * Adds the from address to the mail

     * @param from The from address, e.g. jon.snow@winterfell.com
     * @return A mail object instance
     */
    public Mail withFrom(String from) {
        Objects.requireNonNull(from, "from can not be null");
        
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
        Objects.requireNonNull(file, "file can not be null");
        
        this.attachment = true;
        this.files.add(file);
        return this;
    }
    
    /**
     * Mark the mail as an HTML mail
     * 
     * @return A mail object instance
     */
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
        Objects.requireNonNull(key, "key can not be null");
        Objects.requireNonNull(value, "value can not be null");
        
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
        try {
            Email email = new SimpleEmail();
            email.setHostName(host);
            email.setSmtpPort(port);
            email.setAuthenticator(defaultAuthenticator);
            email.setSSLOnConnect(ssl);
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
        try {
            MultiPartEmail multiPartEmail = new MultiPartEmail();
            multiPartEmail.setHostName(host);
            multiPartEmail.setSmtpPort(port);
            multiPartEmail.setAuthenticator(defaultAuthenticator);
            multiPartEmail.setSSLOnConnect(ssl);
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
        try {
            HtmlEmail htmlEmail = new HtmlEmail();
            htmlEmail.setHostName(host);
            htmlEmail.setSmtpPort(port);
            htmlEmail.setAuthenticator(defaultAuthenticator);
            htmlEmail.setSSLOnConnect(ssl);
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

    private String render() throws MangooTemplateEngineException {
        return StringUtils.isNotBlank(this.body) ? this.body : Application.getInstance(TemplateEngine.class).render("", this.template, this.content);
    }
}