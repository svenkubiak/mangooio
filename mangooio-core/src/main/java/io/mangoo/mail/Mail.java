package io.mangoo.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import io.mangoo.exceptions.MangooMailerException;

/**
 * 
 * @author svenkubiak
 *
 */
public class Mail {
    private Map<String, Object> content = new HashMap<String, Object>();
    private List<File> files = new ArrayList<File>();
    private List<String> recipients = new ArrayList<String>();
    private List<String> cc = new ArrayList<String>();
    private List<String> bcc = new ArrayList<String>();
    private String subject;
    private String from;
    private boolean html;
    private boolean attachment;

    public static Mail newMail(){
        return new Mail();
    }
    
    public Mail withRecipient(String recipient) {
        this.recipients.add(recipient);
        return this;
    }
    
    public Mail withCC(String recipient) {
        this.cc.add(recipient);
        return this;
    }
    
    public Mail withSubject(String subject) {
        this.subject = subject;
        return this;
    }
    
    public Mail withBCC(String recipient) {
        this.bcc.add(recipient);
        return this;
    }
    
    public Mail withFrom(String from) {
        this.from = from;
        return this;
    }
    
    public Mail withAttachment(File file) {
        this.attachment = true;
        this.files.add(file);
        return this;
    }
    
    public Mail isHTML() {
        this.html = true;
        return this;
    }
    
    public Mail withContent(String key, Object value) {
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
            
            for (String recipient : this.recipients) {
                email.addTo(recipient);
            }
            
            for (String cc : this.cc) {
                email.addCc(cc);
            }
            
            for (String bcc : this.bcc) {
                email.addBcc(bcc);
            }
        } catch (EmailException e) {
            throw new MangooMailerException(e);
        }        
    }

    private void sendMultipartEmail() {
        // TODO Auto-generated method stub
    }

    private void sendHtmlEmail() {
        // TODO Auto-generated method stub
    }
}