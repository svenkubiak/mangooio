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
    public static Mail create() {
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
        this.email.toMultiple(tos);
        
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
        this.email.to(to);
        
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
        this.email.ccMultiple(ccs);
        
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
        this.email.cc(cc);
        
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
        this.email.bccMultiple(bccs);
        
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
        this.email.bcc(bcc);
        
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
        this.email.withSubject(subject);
        
        return this;
    }
    
    /**
     * Sets the FROM address.
     *
     * @param from Address may be specified with personal name like this: {@code Jenny Doe <email@foo.com>}
     * @return A mail object instance
     */
    public Mail from(String from) {
        Objects.requireNonNull(from, Required.FROM.toString());
        this.email.from(from);
        
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
        this.email.withHeader(name, value);
        
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
        this.email.withReplyTo(replyTo);
        
        return this;
    }
    
    /**
     * Appends one or more REPLY-TO address
     * 
     * @deprecated As of release 5.14.0, will be removed 6.0.0
     *
     * @param replyTos array of {@link String}s to set
     * @return A mail object instance
     */
    @Deprecated(since = "5.14.0", forRemoval = true)
    public Mail replyTo(String... replyTos) {
        Objects.requireNonNull(replyTos, Required.REPLY_TOS.toString());
        
        for (String replyTo : replyTos) {
            this.email.withReplyTo(replyTo);
        }
        
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
        this.email.withHeader("X-Priority", priority);
        
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
        this.email.withAttachment(file.getName(), new FileDataSource(file));
        
        return this;
    }

    /**
     * Adds plain message text
     *
     * @param message The text to add as a {@link String}.
     * @return A mail object instance
     */
    public Mail textMessage(String message) {
        this.email.appendText(message);
        
        return this;
    }
    
    /**
     * Adds html message text.
     *
     * @param message The text to add as a {@link String}.
     * @return A mail object instance
     */
    public Mail htmlMessage(String message) {
        this.email.appendTextHTML(message);
        
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
        this.email.appendText(render(template, content));
        
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
        this.email.appendTextHTML(render(template, content));
        
        return this;
    }
    
    /**
     * Sends the mail
     * 
     * @throws MangooMailerException when sending the mail failed
     */
    public void send() throws MangooMailerException {
        Application.getInstance(MailEvent.class).send(this.email.buildEmail());
    }
    
    private String render(String template, Map<String, Object> content) throws MangooTemplateEngineException {
        if (template.charAt(0) == '/' || template.startsWith("\\")) {
            template = template.substring(1, template.length());
        } 
        
        TemplateContext templateContext = new TemplateContext(content).withTemplatePath(template);
        return Application.getInstance(TemplateEngine.class).renderTemplate(templateContext);
    }
    
    /**
     * @deprecated use {@link #create()} instead. 
     * 
     * Creates a new mail instance
     * @return A mail object instance
     */
    @Deprecated(since = "5.12.0", forRemoval = true)
    public static Mail build() {
        return new Mail();
    }

    /**
     * @deprecated Use {@link #textMessage(String, Map)} or {@link #htmlMessage(String, Map)} instead
     * 
     * Sets a template to be rendered for the email. Using a template
     * will make it a HTML Email by default.
     *
     * @param template The template to use
     * @param content The content for the template
     * @return A mail object instance
     * @throws MangooTemplateEngineException if rendering of template fails
     */
    @Deprecated(since = "5.12.0", forRemoval = true)
    public Mail templateMessage(String template, Map<String, Object> content) throws MangooTemplateEngineException {
        Objects.requireNonNull(template, Required.TEMPLATE.toString());
        Objects.requireNonNull(content, Required.CONTENT.toString());

        if (template.charAt(0) == '/' || template.startsWith("\\")) {
            template = template.substring(1, template.length());
        } 
        
        this.email.withHTMLText(Application.getInstance(TemplateEngine.class).renderTemplate(new TemplateContext(content).withTemplatePath(template)));
        
        return this;
    }
    
    /**
     * @deprecated Use {@link #textMessage(String, Map)} or {@link #htmlMessage(String, Map)} instead
     * 
     * Sets a template to be rendered for the email. Using a template
     * will make it a HTML Email by default.
     *
     * @param template The template to use
     * @return A mail object instance
     * @throws MangooTemplateEngineException if rendering of template fails
     */
    @Deprecated(since = "5.12.0", forRemoval = true)
    public Mail templateMessage(String template) throws MangooTemplateEngineException {
        Objects.requireNonNull(template, Required.TEMPLATE.toString());

        if (template.charAt(0) == '/' || template.startsWith("\\")) {
            template = template.substring(1, template.length());
        } 
        
        this.email.withHTMLText(Application.getInstance(TemplateEngine.class).renderTemplate(new TemplateContext().withTemplatePath(template)));

        return this;
    }
}