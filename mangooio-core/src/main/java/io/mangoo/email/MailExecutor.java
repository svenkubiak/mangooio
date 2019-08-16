package io.mangoo.email;

import java.util.Objects;

import jodd.mail.Email;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;
import jodd.mail.SmtpSslServer;

/**
 * 
 * @author svenkubiak
 *
 */
public class MailExecutor implements Runnable {
    private SmtpSslServer smtpSslServer;
    private SmtpServer smtpServer;
    private Email mail;
    
    public MailExecutor(SmtpSslServer smtpSslServer, SmtpServer smtpServer, Email email) {
        this.smtpSslServer = smtpSslServer;
        this.smtpServer = smtpServer;
        this.mail = Objects.requireNonNull(email, "email can not be null");
    }

    @Override
    public void run() {
        SendMailSession session = null;
        if (this.smtpSslServer != null) {
            session = this.smtpSslServer.createSession();
        } else if (this.smtpServer != null) {
            session = this.smtpServer.createSession();
        } else {
            //do nothing
        }
        
        if (session != null) {
            try {
                session.open();
                session.sendMail(this.mail);
            } finally {
                session.close();             
            } 
        }
    }
}