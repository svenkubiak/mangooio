package io.mangoo.email;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import javax.activation.FileDataSource;

import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.email.EmailBuilder;

import com.google.common.base.Preconditions;

import io.mangoo.core.Application;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooMailerException;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.templating.TemplateContext;
import io.mangoo.templating.TemplateEngine;

/**
 * 
 * @author svenkubiak
 *
 */
public class Mail {
    private static final int LOWEST_PRIORITY = 5;
    private static final int HIGHEST_PRIORITY = 1;
    private EmailPopulatingBuilder email = EmailBuilder.startingBlank();

    /**
     * Creates a new mail instance
     * @return A mail object instance
     */
    public static Mail newMail() {
        return new Mail();
    }
    
    /**
     * Appends one or more TO address
     *
     * @param tos Address may be specified with personal name like this: {@code Jenny Doe <email@foo.com>}.
     * @return A mail object instance
     */
    public Mail to(String... tos) {
        Objects.requireNonNull(tos, Required.TOS.toString());
        email.toMultiple(tos);
        
        return this;
    }
    
    /**
     * Appends TO address
     *
     * @param to Address may be specified with personal name like this: {@code Jenny Doe <email@foo.com>}.
     * @return A mail object instance
     */
    public Mail to(String to) {
        Objects.requireNonNull(to, Required.TO.toString());
        email.to(to);
        
        return this;
    }
    
    /**
     * Sets one or more CC address
     *
     * @param ccs array of {@link String}s to set.
     * @return A mail object instance
     */
    public Mail cc(String... ccs) {
        Objects.requireNonNull(ccs, Required.CCS.toString());
        email.ccMultiple(ccs);
        
        return this;
    }
    
    /**
     * Appends CC address
     *
     * @param cc Address may be specified with personal name like this: {@code Jenny Doe <email@foo.com>}.
     * @return A mail object instance
     */
    public Mail cc(String cc) {
        Objects.requireNonNull(cc, Required.CC.toString());
        email.cc(cc);
        
        return this;
    }
    
    /**
     * Appends BCC address
     *
     * @param bccs array of {@link String}s to set.
     * @return A mail object instance
     */
    public Mail bcc(String... bccs) {
        Objects.requireNonNull(bccs, Required.BCCS.toString());
        email.bccMultiple(bccs);
        
        return this;
    }
    
    /**
     * Appends BCC address.
     *
     * @param bcc Address may be specified with personal name like this: {@code Jenny Doe <email@foo.com>}.
     * @return A mail object instance
     */
    public Mail bcc(String bcc) {
        Objects.requireNonNull(bcc, Required.BCC.toString());
        email.bcc(bcc);
        
        return this;
    }
    
    /**
     * Sets message subject with specified encoding to override default platform encoding.
     * The application must ensure that the subject does not contain any line breaks.
     * See {@link javax.mail.internet.MimeMessage#setSubject(String, String)}.
     *
     * @param subject The message subject
     * @return A mail object instance
     */
    public Mail subject(String subject) {
        Objects.requireNonNull(subject, Required.SUBJECT.toString());
        email.withSubject(subject);
        
        return this;
    }
    
    /**
     * Sets the FROM address and name
     * 
     * @param fromName The name of the sender e.g. Peter Parker
     * @param fromAddress Address may be specified with personal name like this: {@code email@foo.com}
     * @return A mail object instance
     */
    public Mail from(String fromName, String fromAddress) {
        Objects.requireNonNull(fromName, Required.FROM.toString());
        Objects.requireNonNull(fromAddress, Required.NAME.toString());
        email.from(fromName, fromAddress);
        
        return this;
    }
    
    /**
     * Sets header value.
     *
     * @param name  The name of the header
     * @param value The value of the header
     * @return A mail object instance
     */
    public Mail header(String name, String value) {
        Objects.requireNonNull(name, Required.NAME.toString());
        Objects.requireNonNull(value, Required.VALUE.toString());
        email.withHeader(name, value);
        
        return this;
    }
    
    /**
     * Appends REPLY-TO address
     *
     * @param replyTo Address may be specified with personal name like this: {@code Jenny Doe <email@foo.com>}
     * @return A mail object instance
     */
    public Mail replyTo(String replyTo) {
        Objects.requireNonNull(replyTo, Required.REPLY_TO.toString());
        email.withReplyTo(replyTo);
        
        return this;
    }
    
    /**
     * Sets the email priority
     *
     * @param priority - 1 being the highest priority, 3 = normal and 5 = lowest priority.
     *                 
     * @return A mail object instance
     */
    public Mail priority(int priority) {
        Preconditions.checkArgument(priority >= HIGHEST_PRIORITY && priority <= LOWEST_PRIORITY, Required.PRIORITY.toString());
        email.withHeader("X-Priority", priority);
        
        return this;
    }
    
    /**
     * Adds a file as attachment to the mail
     *
     * @param file The File to attach
     * @return A mail object instance
     */
    public Mail attachment(File file) {
        Objects.requireNonNull(file, Required.FILE.toString());
        email.withAttachment(file.getName(), new FileDataSource(file));
        
        return this;
    }

    /**
     * Adds plain message text
     *
     * @param message The text to add as a {@link String}.
     * @return A mail object instance
     */
    public Mail textMessage(String message) {
        email.appendText(message);
        
        return this;
    }
    
    /**
     * Adds html message text.
     *
     * @param message The text to add as a {@link String}.
     * @return A mail object instance
     */
    public Mail htmlMessage(String message) {
        email.appendTextHTML(message);
        
        return this;
    }
    
    /**
     * Adds plain message text which uses a given template and content to render
     *
     * @param template The template to render
     * @param content The content to pass to the template
     * @throws MangooTemplateEngineException when rendering the template failed
     * 
     * @return A mail object instance
     */
    public Mail textMessage(String template, Map<String, Object> content) throws MangooTemplateEngineException {
        Objects.requireNonNull(template, Required.TEMPLATE.toString());
        email.appendText(render(template, content));
        
        return this;
    }
    
    /**
     * Adds html message text which uses a given template and content to render
     *
     * @param template The template to render
     * @param content The content to pass to the template
     * @throws MangooTemplateEngineException when rendering the template failed
     * 
     * @return A mail object instance
     */
    public Mail htmlMessage(String template, Map<String, Object> content) throws MangooTemplateEngineException {
        Objects.requireNonNull(template, Required.TEMPLATE.toString());
        email.appendTextHTML(render(template, content));
        
        return this;
    }
    
    /**
     * Sends the mail
     * 
     * @throws MangooMailerException when sending the mail failed
     */
    public void send() throws MangooMailerException {
        Application.getInstance(MailEvent.class).send(email.buildEmail());
    }
    
    private String render(String template, Map<String, Object> content) throws MangooTemplateEngineException {
        if (template.charAt(0) == '/' || template.startsWith("\\")) {
            template = template.substring(1, template.length());
        } 
        
        TemplateContext templateContext = new TemplateContext(content).withTemplatePath(template);
        return Application.getInstance(TemplateEngine.class).renderTemplate(templateContext);
    }
}