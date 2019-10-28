package io.mangoo.email;

import java.util.Map;
import java.util.Objects;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooMailerException;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.services.EventBusService;
import io.mangoo.templating.TemplateContext;
import io.mangoo.templating.TemplateEngine;
import jodd.mail.Email;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailAttachmentBuilder;

/**
 * 
 * @author svenkubiak
 *
 */
public class Mail {
    private Email email = Email.create();

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
        this.email.to(tos);
        
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
        this.email.cc(ccs);
        
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
        this.email.bcc(bccs);
        
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
        this.email.subject(subject, Default.ENCODING.toString());
        
        return this;
    }
    
    /**
     * Sets the FROM address.
     *
     * @param from Address may be specified with personal name like this: {@code Jenny Doe <email@foo.com>}.
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
        
        this.email.header(name, value);
        
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
        this.email.replyTo(replyTo);
        
        return this;
    }
    
    /**
     * Appends one or more REPLY-TO address
     *
     * @param replyTos array of {@link String}s to set
     * @return A mail object instance
     */
    public Mail replyTo(String... replyTos) {
        Objects.requireNonNull(replyTos, Required.REPLY_TOS.toString());
        this.email.replyTo(replyTos);
        
        return this;
    }
    
    /**
     * Sets email priority
     *
     * @param priority - 1 being the highest priority, 3 = normal and 5 = lowest priority.
     *                 
     * @return A mail object instance
     */
    public Mail priority(int priority) {
        Preconditions.checkArgument(priority >= 1 && priority <= 5, Required.PRIORITY.toString());
        this.email.priority(priority);
        
        return this;
    }
    
    /**
     * Adds {@link EmailAttachment}. Content ID will be set to {@code null}.
     *
     * @param builder {@link EmailAttachmentBuilder}
     * @return A mail object instance
     */
    public Mail attachment(EmailAttachmentBuilder builder) {
        Objects.requireNonNull(builder, Required.BUILDER.toString());
        this.email.attachment(builder);
        
        return this;
    }
    
    /**
     * Attaches the embedded attachment: Content ID will be set if missing from attachment's file name.
     *
     * @param builder {@link EmailAttachmentBuilder}
     * @return A mail object instance
     */
    public Mail embeddedAttachment(EmailAttachmentBuilder builder) {
        Objects.requireNonNull(builder, Required.BUILDER.toString());
        this.email.embeddedAttachment(builder);
        
        return this;
    }
    
    /**
     * Adds plain message text
     *
     * @param message The text to add as a {@link String}.
     * @return A mail object instance
     */
    public Mail textMessage(String message) {
        this.email.textMessage(message, Default.ENCODING.toString());
        
        return this;
    }
    
    /**
     * Adds html message text.
     *
     * @param message The text to add as a {@link String}.
     * @return A mail object instance
     */
    public Mail htmlMessage(String message) {
        this.email.htmlMessage(message, Default.ENCODING.toString());
        
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
        this.email.textMessage(render(template, content), Charsets.UTF_8.toString());
        
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
        this.email.htmlMessage(render(template, content), Charsets.UTF_8.toString());
        
        return this;
    }
    
    /**
     * Sends the mail
     * 
     * @throws MangooMailerException when sending the mail failed
     */
    public void send() throws MangooMailerException {
        Application.getInstance(EventBusService.class).publish(this.email);
    }
    
    private String render(String template, Map<String, Object> content) throws MangooTemplateEngineException {
        if (template.charAt(0) == '/' || template.startsWith("\\")) {
            template = template.substring(1, template.length());
        } 
        
        TemplateContext templateContext = new TemplateContext(content).withTemplatePath(template);
        Application.getInstance(TemplateEngine.class).renderTemplate(templateContext);
        return template;
    }
    
    /**
     * @deprecated use {@link #create()} instead. 
     * 
     * Creates a new mail instance
     * @return A mail object instance
     */
    @Deprecated
    public static Mail build() {
        return new Mail();
    }

    /**
     *  @deprecated No need to wrap an Email instance anymore
     *  us specific (e.g. to(), cc(), etc.) methods directly
     * 
     * Sets the org.Jodd.Email instance that the email is based on
     * 
     * @param email The Email instance
     * @return A mail object instance
     */
    @Deprecated
    public Mail withBuilder(Email email) {
        Objects.requireNonNull(email, Required.EMAIL.toString());
        this.email = email;
        
        return this;
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
    @Deprecated
    public Mail templateMessage(String template, Map<String, Object> content) throws MangooTemplateEngineException {
        Objects.requireNonNull(template, Required.TEMPLATE.toString());
        Objects.requireNonNull(content, Required.CONTENT.toString());

        if (template.charAt(0) == '/' || template.startsWith("\\")) {
            template = template.substring(1, template.length());
        } 
        
        this.email.htmlMessage(
                Application.getInstance(TemplateEngine.class).renderTemplate(new TemplateContext(content).withTemplatePath(template)),
                Default.ENCODING.toString());

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
    @Deprecated
    public Mail templateMessage(String template) throws MangooTemplateEngineException {
        Objects.requireNonNull(template, Required.TEMPLATE.toString());

        if (template.charAt(0) == '/' || template.startsWith("\\")) {
            template = template.substring(1, template.length());
        } 
        
        this.email.htmlMessage(
                Application.getInstance(TemplateEngine.class).renderTemplate(new TemplateContext().withTemplatePath(template)),
                Default.ENCODING.toString());

        return this;
    }    
}