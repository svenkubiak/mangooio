package io.mangoo.email;

import com.google.common.base.Preconditions;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Application;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.templating.TemplateContext;
import io.mangoo.templating.TemplateEngine;

import java.nio.file.Path;
import java.util.*;

public class Mail {
    private static final int LOWEST_PRIORITY = 5;
    private static final int HIGHEST_PRIORITY = 1;
    private final Map<String, String> mailHeaders = new HashMap<>();
    private final List<String> mailTos = new ArrayList<>();
    private final List<String> mailCcs = new ArrayList<>();
    private final List<String> mailBccs = new ArrayList<>();
    private final List<Path> mailAttachments = new ArrayList<>();
    private String mailSubject;
    private String mailReplyTo;
    private String mailText;
    private String mailFromName;
    private String mailFromAddress;
    private boolean mailHtml;

    /**
     * Creates a new mail instance
     * 
     * @return A mail object instance
     */
    public static Mail newMail() {
        return new Mail();
    }
    
    /**
     * Sets one or more TO address
     *
     * @param tos Address may be specified with personal name like this: {@code Jenny Doe <email@foo.com>}.
     * @return A mail object instance
     */
    public Mail to(String... tos) {
        Objects.requireNonNull(tos, NotNull.TOS);
        mailTos.addAll(Arrays.asList(tos));
        
        return this;
    }
    
    /**
     * Sets one or more CC address
     *
     * @param ccs array of {@link String}s to set.
     * @return A mail object instance
     */
    public Mail cc(String... ccs) {
        Objects.requireNonNull(ccs, NotNull.CCS);
        mailCcs.addAll(Arrays.asList(ccs));
        
        return this;
    }
    
    /**
     * Sets one or more BCC address
     *
     * @param bccs array of {@link String}s to set.
     * @return A mail object instance
     */
    public Mail bcc(String... bccs) {
        Objects.requireNonNull(bccs, NotNull.BCCS);
        mailBccs.addAll(Arrays.asList(bccs));
        
        return this;
    }
    
    /**
     * Sets message subject with specified encoding to override default platform encoding.
     * The application must ensure that the subject does not contain any line breaks.
     *
     * @param subject The message subject
     * @return A mail object instance
     */
    public Mail subject(String subject) {
        Objects.requireNonNull(subject, NotNull.SUBJECT);
        mailSubject = subject;
            
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
        Objects.requireNonNull(fromName, NotNull.FROM);
        Objects.requireNonNull(fromAddress, NotNull.NAME);
        mailFromName = fromName;
        mailFromAddress = fromAddress;
        
        return this;
    }
    
    /**
     * Sets the FROM address
     * 
     * @param fromAddress Address may be specified with personal name like this: {@code email@foo.com}
     * @return A mail object instance
     */
    public Mail from(String fromAddress) {
        Objects.requireNonNull(fromAddress, NotNull.FROM);
        mailFromAddress = fromAddress;
        
        return this;
    }
    
    /**
     * Adds a header value
     *
     * @param name  The name of the header
     * @param value The value of the header
     * @return A mail object instance
     */
    public Mail header(String name, String value) {
        Objects.requireNonNull(name, NotNull.NAME);
        Objects.requireNonNull(value, NotNull.VALUE);
        mailHeaders.put(name, value);
        
        return this;
    }
    
    /**
     * Sets REPLY-TO address
     *
     * @param replyTo Address may be specified with personal name like this: {@code Jenny Doe <email@foo.com>}
     * @return A mail object instance
     */
    public Mail replyTo(String replyTo) {
        Objects.requireNonNull(replyTo, NotNull.REPLY_TO);
        mailReplyTo = replyTo;
        
        return this;
    }
    
    /**
     * Sets the email priority
     *
     * @param priority - 1 being the highest priority, 3 = normal priority and 5 = lowest priority
     *                 
     * @return A mail object instance
     */
    public Mail priority(int priority) {
        Preconditions.checkArgument(priority >= HIGHEST_PRIORITY && priority <= LOWEST_PRIORITY, NotNull.PRIORITY);
        mailHeaders.put("X-Priority", String.valueOf(priority));
        
        return this;
    }
    
    /**
     * Adds a file as attachment to the mail
     *
     * @param path The Path to attach
     * @return A mail object instance   
     */
    public Mail attachment(Path path) {
        Objects.requireNonNull(path, NotNull.PATH);
        Preconditions.checkArgument(path.toFile().length() != 0, NotNull.CONTENT);
        
        mailAttachments.add(path);
        
        return this;
    }
    
    /**
     * Adds a list of files as attachment to the mail
     *
     * @param paths The Path files to attach
     * @return A mail object instance   
     */
    public Mail attachments(List<Path> paths) {
        Objects.requireNonNull(paths, NotNull.PATH);
        paths.forEach(path -> {
            Objects.requireNonNull(path, NotNull.PATH);
            Preconditions.checkArgument(path.toFile().length() != 0, NotNull.PATH);
        });
        
        mailAttachments.addAll(paths);
        
        return this;
    }

    /**
     * Adds plain message text
     *
     * @param message The text to add as a {@link String}.
     * @return A mail object instance
     */
    public Mail textMessage(String message) {
        mailText = message;
        
        return this;
    }
    
    /**
     * Adds html message text.
     *
     * @param message The text to add as a {@link String}.
     * @return A mail object instance
     */
    public Mail htmlMessage(String message) {
        mailText = message;
        mailHtml = true;
        
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
        Objects.requireNonNull(template, NotNull.TEMPLATE);
        mailText = render(template, content);
        
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
        Objects.requireNonNull(template, NotNull.TEMPLATE);
        mailText = render(template, content);
        mailHtml = true;
        
        return this;
    }
    
    public void send() {
        Thread.ofVirtual().start(() -> Application.getInstance(PostOffice.class).send(this));
    }
    
    private String render(String template, Map<String, Object> content) throws MangooTemplateEngineException {
        Objects.requireNonNull(template, NotNull.TEMPLATE);
        Objects.requireNonNull(template, NotNull.CONTENT);
        
        if (template.charAt(0) == '/' || template.startsWith("\\")) {
            template = template.substring(1, template.length());
        } 
        
        var templateContext = new TemplateContext(content).withTemplatePath(template);
        
        return Application.getInstance(TemplateEngine.class).renderTemplate(templateContext);
    }

    public Map<String, String> getMailHeaders() {
        return mailHeaders;
    }

    public List<String> getMailTos() {
        return mailTos;
    }

    public List<String> getMailCcs() {
        return mailCcs;
    }

    public List<String> getMailBccs() {
        return mailBccs;
    }

    public List<Path> getMailAttachments() {
        return mailAttachments;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public String getMailReplyTo() {
        return mailReplyTo;
    }

    public String getMailText() {
        return mailText;
    }

    public String getMailFromName() {
        return mailFromName;
    }
    
    public String getMailFromAddress() {
        return mailFromAddress;
    }

    public boolean isMailHtml() {
        return mailHtml;
    }
    
    public boolean hasAttachments() {
        return !mailAttachments.isEmpty();
    }
}