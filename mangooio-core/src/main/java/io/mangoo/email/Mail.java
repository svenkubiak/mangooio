package io.mangoo.email;

import com.google.common.base.Preconditions;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.templating.TemplateContext;
import io.mangoo.templating.TemplateEngine;
import io.mangoo.utils.Argument;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class Mail {
    private static final int LOWEST_PRIORITY = 5;
    private static final int HIGHEST_PRIORITY = 1;
    // RFC 5322 field name: printable ASCII except colon
    private static final Pattern HEADER_NAME = Pattern.compile("[\\x21-\\x39\\x3B-\\x7E]+");
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

    public static Mail newMail() {
        return new Mail();
    }
    
    /** Addresses may include a personal name, e.g. {@code Jenny Doe <email@foo.com>}. */
    public Mail to(String... tos) {
        Objects.requireNonNull(tos, Required.TOS);
        mailTos.addAll(Arrays.asList(tos));
        
        return this;
    }
    
    public Mail cc(String... ccs) {
        Argument.requireNonBlank(Required.CCS, ccs);
        mailCcs.addAll(Arrays.asList(ccs));
        
        return this;
    }
    
    public Mail bcc(String... bccs) {
        Argument.requireNonBlank(Required.BCCS, bccs);
        mailBccs.addAll(Arrays.asList(bccs));
        
        return this;
    }
    
    /** Line breaks in the subject are folded by Jakarta Mail and can not inject headers, but are not removed. */
    public Mail subject(String subject) {
        Objects.requireNonNull(subject, Required.SUBJECT);
        mailSubject = subject;
            
        return this;
    }
    
    public Mail from(String fromName, String fromAddress) {
        Objects.requireNonNull(fromName, Required.FROM);
        Objects.requireNonNull(fromAddress, Required.NAME);
        mailFromName = fromName;
        mailFromAddress = fromAddress;
        
        return this;
    }
    
    public Mail from(String fromAddress) {
        Objects.requireNonNull(fromAddress, Required.FROM);
        mailFromAddress = fromAddress;
        
        return this;
    }
    
    /**
     * Rejects a name that is not a valid header field name and a value with line breaks, as both would allow header injection.
     */
    public Mail header(String name, String value) {
        Objects.requireNonNull(name, Required.NAME);
        Objects.requireNonNull(value, Required.VALUE);
        Preconditions.checkArgument(HEADER_NAME.matcher(name).matches(), "Invalid mail header name");
        Preconditions.checkArgument(value.indexOf('\r') < 0 && value.indexOf('\n') < 0, "Mail header value must not contain line breaks");
        mailHeaders.put(name, value);
        
        return this;
    }
    
    /** The address may include a personal name, e.g. {@code Jenny Doe <email@foo.com>}. */
    public Mail replyTo(String replyTo) {
        Objects.requireNonNull(replyTo, Required.REPLY_TO);
        mailReplyTo = replyTo;
        
        return this;
    }
    
    /** 1 is the highest, 3 the normal and 5 the lowest priority. */
    public Mail priority(int priority) {
        Preconditions.checkArgument(priority >= HIGHEST_PRIORITY && priority <= LOWEST_PRIORITY, Required.PRIORITY);
        mailHeaders.put("X-Priority", String.valueOf(priority));
        
        return this;
    }
    
    public Mail attachment(Path path) {
        Objects.requireNonNull(path, Required.PATH);
        Preconditions.checkArgument(path.toFile().length() != 0, Required.CONTENT);
        
        mailAttachments.add(path);
        
        return this;
    }
    
    public Mail attachments(List<Path> paths) {
        Objects.requireNonNull(paths, Required.PATH);
        paths.forEach(path -> {
            Objects.requireNonNull(path, Required.PATH);
            Preconditions.checkArgument(path.toFile().length() != 0, Required.PATH);
        });
        
        mailAttachments.addAll(paths);
        
        return this;
    }

    public Mail textMessage(String message) {
        mailText = message;
        
        return this;
    }
    
    public Mail htmlMessage(String message) {
        mailText = message;
        mailHtml = true;
        
        return this;
    }
    
    public Mail textMessage(String template, Map<String, Object> content) throws MangooTemplateEngineException {
        Objects.requireNonNull(template, Required.TEMPLATE);
        mailText = render(template, content);
        
        return this;
    }
    
    public Mail htmlMessage(String template, Map<String, Object> content) throws MangooTemplateEngineException {
        Objects.requireNonNull(template, Required.TEMPLATE);
        mailText = render(template, content);
        mailHtml = true;
        
        return this;
    }
    
    public void send() {
        Thread.ofVirtual().start(() -> Application.getInstance(PostOffice.class).send(this));
    }
    
    private String render(String template, Map<String, Object> content) throws MangooTemplateEngineException {
        Objects.requireNonNull(template, Required.TEMPLATE);
        Objects.requireNonNull(template, Required.CONTENT);
        
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