package io.mangoo.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

import io.mangoo.core.Application;
import io.mangoo.exceptions.MangooMailerException;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.templating.TemplateEngine;

/**
 * 
 * @author svenkubiak
 *
 */
public class Mail {
    private Map<String, Object> content = new HashMap<>();
    private List<File> files = new ArrayList<>();
    private List<String> recipients = new ArrayList<>();
    private List<String> cc = new ArrayList<>();
    private List<String> bcc = new ArrayList<>();
    private String template;
    private String subject;
    private String from;
    private boolean html;
    private boolean attachment;

    public static Mail newMail(){
        return new Mail();
    }
    
    public Mail withRecipient(String recipient) {
        Objects.requireNonNull(recipient, "recipient can not be null");
        
        this.recipients.add(recipient);
        return this;
    }
    
    public Mail withCC(String recipient) {
        Objects.requireNonNull(recipient, "cc recipient can not be null");
        
        this.cc.add(recipient);
        return this;
    }
    
    public Mail withSubject(String subject) {
        Objects.requireNonNull(subject, "sobject can not be null");
        
        this.subject = subject;
        return this;
    }
    
    public Mail withTemplate(String template) {
        Objects.requireNonNull(template, "template can not be null");
        
        if (template.startsWith("/") || template.startsWith("\\")) {
            template = template.replaceFirst("/", "");
            template = template.replaceFirst("\\", "");
        }
        
        this.template = template;
        return this;
    }
    
    public Mail withBCC(String recipient) {
        Objects.requireNonNull(recipient, "bcc recipient can not be null");
        
        this.bcc.add(recipient);
        return this;
    }
    
    public Mail withFrom(String from) {
        Objects.requireNonNull(from, "from can not be null");
        
        this.from = from;
        return this;
    }
    
    public Mail withAttachment(File file) {
        Objects.requireNonNull(file, "file can not be null");
        
        this.attachment = true;
        this.files.add(file);
        return this;
    }
    
    public Mail isHTML() {
        this.html = true;
        return this;
    }
    
    public Mail withContent(String key, Object value) {
        Objects.requireNonNull(key, "key can not be null");
        Objects.requireNonNull(value, "value can not be null");
        
        content.put(key, value);
        return this;
    }
    
    public void send() throws MangooMailerException {
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
            email.setFrom(this.from);
            email.setSubject(this.subject);
            email.setMsg(render());
            
            for (String recipient : this.recipients) {
                email.addTo(recipient);
            }
            
            for (String cc : this.cc) {
                email.addCc(cc);
            }
            
            for (String bcc : this.bcc) {
                email.addBcc(bcc);
            }
        } catch (EmailException | MangooTemplateEngineException e) {
            throw new MangooMailerException(e);
        }        
    }

    private void sendMultipartEmail() throws MangooMailerException {
        try {
            MultiPartEmail multiPartEmail = new MultiPartEmail();
            multiPartEmail.setFrom(this.from);
            multiPartEmail.setSubject(this.subject);
            multiPartEmail.setMsg(render());
            
            for (String recipient : this.recipients) {
                multiPartEmail.addTo(recipient);
            }
            
            for (String cc : this.cc) {
                multiPartEmail.addCc(cc);
            }
            
            for (String bcc : this.bcc) {
                multiPartEmail.addBcc(bcc);
            }
            
            for (File file : this.files) {
                multiPartEmail.attach(file);
            }
        } catch (EmailException | MangooTemplateEngineException e) {
            throw new MangooMailerException(e);
        }   
    }

    private void sendHtmlEmail() throws MangooMailerException {
        try {
            HtmlEmail htmlEmail = new HtmlEmail();
            htmlEmail.setFrom(this.from);
            htmlEmail.setSubject(this.subject);
            htmlEmail.setHtmlMsg(render());
            
            for (String recipient : this.recipients) {
                htmlEmail.addTo(recipient);
            }
            
            for (String cc : this.cc) {
                htmlEmail.addCc(cc);
            }
            
            for (String bcc : this.bcc) {
                htmlEmail.addBcc(bcc);
            }
            
            for (File file : this.files) {
                htmlEmail.attach(file);
            }
        } catch (EmailException | MangooTemplateEngineException e) {
            throw new MangooMailerException(e);
        } 
    }

    private String render() throws MangooTemplateEngineException {
        return Application.getInstance(TemplateEngine.class).render("", this.template, this.content);
    }
}