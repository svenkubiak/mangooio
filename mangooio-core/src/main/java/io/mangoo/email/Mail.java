package io.mangoo.email;

import com.google.common.base.Preconditions;
import io.mangoo.core.Application;
import io.mangoo.enums.Queue;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.reactive.Stream;
import io.mangoo.templating.TemplateContext;
import io.mangoo.templating.TemplateEngine;

import java.nio.file.Path;
import java.util.*;

public class Mail {
    private static final int LOWEST_PRIORITY = 5;
    private static final int HIGHEST_PRIORITY = 1;
    private Map<String, String> messageHeaders = new HashMap<>();
    private List<String> messageTos = new ArrayList<>(); 
    private List<String> messageCcs = new ArrayList<>(); 
    private List<String> messageBccs = new ArrayList<>(); 
    private List<Path> messageAttachments = new ArrayList<>();
    private String messageSubject;
    private String messageReplyTo;
    private String messageText;
    private String messageFromName;
    private String messageFromAddress;
    private boolean messageHtml;

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
        Objects.requireNonNull(tos, Required.TOS.toString());
        messageTos.addAll(Arrays.asList(tos));
        
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
        messageCcs.addAll(Arrays.asList(ccs));
        
        return this;
    }
    
    /**
     * Sets one or more BCC address
     *
     * @param bccs array of {@link String}s to set.
     * @return A mail object instance
     */
    public Mail bcc(String... bccs) {
        Objects.requireNonNull(bccs, Required.BCCS.toString());
        messageBccs.addAll(Arrays.asList(bccs));
        
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
        Objects.requireNonNull(subject, Required.SUBJECT.toString());
        messageSubject = subject;
            
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
        messageFromName = fromName;
        messageFromAddress = fromAddress;
        
        return this;
    }
    
    /**
     * Sets the FROM address
     * 
     * @param fromAddress Address may be specified with personal name like this: {@code email@foo.com}
     * @return A mail object instance
     */
    public Mail from(String fromAddress) {
        Objects.requireNonNull(fromAddress, Required.FROM.toString());
        messageFromAddress = fromAddress;
        
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
        Objects.requireNonNull(name, Required.NAME.toString());
        Objects.requireNonNull(value, Required.VALUE.toString());
        messageHeaders.put(name, value);
        
        return this;
    }
    
    /**
     * Sets REPLY-TO address
     *
     * @param replyTo Address may be specified with personal name like this: {@code Jenny Doe <email@foo.com>}
     * @return A mail object instance
     */
    public Mail replyTo(String replyTo) {
        Objects.requireNonNull(replyTo, Required.REPLY_TO.toString());
        messageReplyTo = replyTo;
        
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
        Preconditions.checkArgument(priority >= HIGHEST_PRIORITY && priority <= LOWEST_PRIORITY, Required.PRIORITY.toString());
        messageHeaders.put("X-Priority", String.valueOf(priority));
        
        return this;
    }
    
    /**
     * Adds a file as attachment to the mail
     *
     * @param path The Path to attach
     * @return A mail object instance   
     */
    public Mail attachment(Path path) {
        Objects.requireNonNull(path, Required.PATH.toString());
        Preconditions.checkArgument(path.toFile().length() != 0, Required.CONTENT.toString());
        
        messageAttachments.add(path);
        
        return this;
    }
    
    /**
     * Adds a list of files as attachment to the mail
     *
     * @param paths The Path files to attach
     * @return A mail object instance   
     */
    public Mail attachments(List<Path> paths) {
        Objects.requireNonNull(paths, Required.PATH.toString());
        paths.forEach(path -> {
            Objects.requireNonNull(path, Required.PATH.toString());
            Preconditions.checkArgument(path.toFile().length() != 0, Required.PATH.toString());
        });
        
        messageAttachments.addAll(paths);
        
        return this;
    }

    /**
     * Adds plain message text
     *
     * @param message The text to add as a {@link String}.
     * @return A mail object instance
     */
    public Mail textMessage(String message) {
        messageText = message;
        
        return this;
    }
    
    /**
     * Adds html message text.
     *
     * @param message The text to add as a {@link String}.
     * @return A mail object instance
     */
    public Mail htmlMessage(String message) {
        messageText = message;
        messageHtml = true;
        
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
        messageText = render(template, content);
        
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
        messageText = render(template, content);
        messageHtml = true;
        
        return this;
    }
    
    /**
     * Sends the mail
     */
    @SuppressWarnings("unchecked")
    public void send() {
        Application.getInstance(Stream.class).publish(Queue.MAIL.toString(), this);
    }
    
    private String render(String template, Map<String, Object> content) throws MangooTemplateEngineException {
        Objects.requireNonNull(template, Required.TEMPLATE.toString());
        Objects.requireNonNull(template, Required.CONTENT.toString());
        
        if (template.charAt(0) == '/' || template.startsWith("\\")) {
            template = template.substring(1, template.length());
        } 
        
        var templateContext = new TemplateContext(content).withTemplatePath(template);
        
        return Application.getInstance(TemplateEngine.class).renderTemplate(templateContext);
    }

    public Map<String, String> getMessageHeaders() {
        return messageHeaders;
    }

    public List<String> getMessageTos() {
        return messageTos;
    }

    public List<String> getMessageCcs() {
        return messageCcs;
    }

    public List<String> getMessageBccs() {
        return messageBccs;
    }

    public List<Path> getMessageAttachments() {
        return messageAttachments;
    }

    public String getMessageSubject() {
        return messageSubject;
    }

    public String getMessageReplyTo() {
        return messageReplyTo;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageFromName() {
        return messageFromName;
    }
    
    public String getMessageFromAddress() {
        return messageFromAddress;
    }

    public boolean isMessageHtml() {
        return messageHtml;
    }
    
    public boolean hasAttachments() {
        return !messageAttachments.isEmpty();
    }
}