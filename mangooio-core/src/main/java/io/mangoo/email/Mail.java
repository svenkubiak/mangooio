package io.mangoo.email;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import io.mangoo.core.Application;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooMailerException;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.interfaces.MangooTemplateEngine;
import io.mangoo.services.EventBusService;
import jodd.mail.Email;
import jodd.mail.EmailAttachment;

/**
 * 
 * @author svenkubiak
 *
 */
public class Mail {
    private Email email;
    private List<File> files = new ArrayList<>();
    private List<String> recipients = new ArrayList<>();
    private List<String> ccRecipients = new ArrayList<>();
    private List<String> bccRecipients = new ArrayList<>();
    private Map<String, Object> content = new HashMap<>();
    private String template;
    private String subject;
    private String from;
    private String body;
    private String charset;
    private boolean html;
    private boolean attachment;

    /**
     * Creates a new mail instance
     * @return A mail object instance
     */
    public static Mail build() {
        return new Mail();
    }

    /**
     * Sets the org.Jodd.Email instance that the email is based on
     * 
     * @param email The Email instance
     * @return A mail object instance
     */
    public Mail withBuilder(Email email) {
        Objects.requireNonNull(email, Required.EMAIL.toString());
        this.email = email;
        
        return this;
    }
    
    /**
     * Creates a new simple mail instance
     * @deprecated As of 4.13.0, use build() instead
     * 
     * @return Simple mail instance
     */
    @Deprecated
    public static Mail newMail(){
        return new Mail();
    }
    
    /**
     * Creates a new HTML mail instance
     * @deprecated As of 4.13.0, use build() instead
     * 
     * @return HTML mail instance
     */
    @Deprecated
    public static Mail newHtmlMail() {
        Mail mail = new Mail();
        mail.html = true;
        
        return mail;
    }
    
    /**
     * Adds a recipient to the mail
     * @deprecated As of 4.13.0, use specific to, cc and bcc methods instead
     * 
     * @param recipient The mail address of the recipient
     * @return A mail object instance
     */
    @Deprecated
    public Mail withRecipient(String recipient) {
        Objects.requireNonNull(recipient, Required.RECIPIENT.toString());
        
        this.recipients.add(recipient);
        return this;
    }
    
    /**
     * Adds a cc recipient to the mail
     * @deprecated As of 4.13.0, use specific to, cc and bcc methods instead
     * 
     * @param recipient The mail address of the cc recipient
     * @return A mail object instance
     */
    @Deprecated
    public Mail withCC(String recipient) {
        Objects.requireNonNull(recipient, Required.CC_RECIPIENT.toString());
        
        this.ccRecipients.add(recipient);
        return this;
    }
    
    /**
     * Adds a subject to the mail
     * @deprecated As of 4.13.0, use subject() instead
     * 
     * @param subject The subject of the email
     * @return A mail object instance
     */
    @Deprecated
    public Mail withSubject(String subject) {
        Objects.requireNonNull(subject, Required.SUBJECT.toString());
        
        this.subject = subject;
        return this;
    }
    
    /**
     * Add a template to the mail which will be rendered before the mail is send
     * @deprecated As of 4.13.0, use templateMessage(...) instead
     * 
     * @param template The path to the template fail (e.g. emails/mail.ftl)
     * @return A mail object instance
     */
    @Deprecated
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
     * Sets a template to be rendered for the email. Using a template
     * will make it a HTML Email by default. Charset defaults to
     * Charset.defaultCharset()
     * 
     * @param template The template to use
     * @param content The content for the template
     * 
     * @return A mail object instance
     */
    public Mail templateMessage(String template, Map<String, Object> content) {
        return templateMessage(template, content, Charset.defaultCharset().name());
    }

    /**
     * Sets a template to be rendered for the email. Using a template
     * will make it a HTML Email by default.
     *
     * @param template The template to use
     * @param content The content for the template
     * @param charset The charset of the email
     * @return A mail object instance
     */
    public Mail templateMessage(String template, Map<String, Object> content, String charset) {
        Objects.requireNonNull(template, Required.TEMPLATE.toString());
        Objects.requireNonNull(content, Required.CONTENT.toString());
        Objects.requireNonNull(charset, Required.CHARSET.toString());

        this.content = content;
        this.charset = charset;
        if (template.charAt(0) == '/' || template.startsWith("\\")) {
            this.template = template.substring(1, template.length());
        } else {
            this.template = template;
        }

        return this;
    }
    
    /**
     * @deprecated As of 4.13.0, will be removed in 5.0.0
     * 
     * @return The current template path
     */
    @Deprecated
    public String getTemplate() {
        return this.template;
    }
    
    /**
     * Adds a bcc recipient to the mail
     * @deprecated As of 4.13.0, use specific to, cc and bcc methods instead
     * 
     * @param recipient The subject of the email
     * @return A mail object instance
     */
    @Deprecated
    public Mail withBCC(String recipient) {
        Objects.requireNonNull(recipient, Required.BCC_RECIPIENT.toString());
        
        this.bccRecipients.add(recipient);
        return this;
    }
    
    /**
     * Adds a custom to the mail
     * 
     * This will overwrite rendering of the template!
     * @deprecated As of 4.13.0, use textMessage(), htmlMessage() or templateMessage() instead
     * 
     * 
     * @param body The body of the email
     * @return A mail object instance
     */
    @Deprecated
    public Mail withBody(String body) {
        Objects.requireNonNull(body, Required.BODY.toString());
        
        this.body = body;
        return this;
    }
    
    /**
     * Adds the from address to the mail
     * @deprecated As of 4.13.0, use from() instead 
     * 
     * @param from The from address, e.g. jon.snow@winterfell.com
     * @return A mail object instance
     */
    @Deprecated
    public Mail withFrom(String from) {
        Objects.requireNonNull(from, Required.FROM.toString());
        
        this.from = from;
        return this;
    }
    
    /**
     * Adds a file attachment to the mail
     * @deprecated As of 4.13.0, use attachment() instead
     * 
     * @param file The file to add
     * @return A mail object instance
     */
    @Deprecated
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
     * @deprecated As of version 4.13.0, use templateMessage(...) instead
     * 
     * @param key The key 
     * @param value The value
     * @return A mail object instance
     */
    @Deprecated
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
        transform();
        Application.getInstance(EventBusService.class).publish(this.email);
    }

    /**
     * Transforms the content from mail object into jodd email object
     * @deprecated Needs to be removed with 5.0.0
     * 
     * @throws MangooMailerException 
     */
    @Deprecated
    private void transform() throws MangooMailerException {
        if (!this.bccRecipients.isEmpty()) {
            this.email.bcc(this.bccRecipients.toArray(new String[this.bccRecipients.size()]));
        }
        
        if (!this.ccRecipients.isEmpty()) {
            this.email.cc(this.ccRecipients.toArray(new String[this.ccRecipients.size()]));
        }
        
        if (!this.recipients.isEmpty()) {
            this.email.to(this.recipients.toArray(new String[this.recipients.size()]));
        }
        
        if (StringUtils.isNotBlank(this.from)) {
            this.email.from(this.from);
        }
        
        if (StringUtils.isNotBlank(this.subject)) {
            this.email.subject(this.subject);
        }
        
        if (this.attachment) {
            for (File file : this.files) {
                this.email.attachment(EmailAttachment.with().name(file.getName()).content(file));
            }
        }
        
        if (StringUtils.isNotBlank(this.body)) {
            if (this.html) {
                this.email.htmlMessage(body);
            } else {
                this.email.textMessage(this.body);                
            }
        }
        
        if (StringUtils.isNotBlank(this.template)) {
            try {
                this.email.htmlMessage(render(), charset);
            } catch (MangooTemplateEngineException e) {
                throw new MangooMailerException(e);
            }
        }
    }

    private String render() throws MangooTemplateEngineException {
        return StringUtils.isNotBlank(this.body) ? this.body : Application.getInstance(MangooTemplateEngine.class).render("", this.template, this.content);
    }
}