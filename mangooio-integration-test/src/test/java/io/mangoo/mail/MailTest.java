package io.mangoo.mail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;

import io.mangoo.core.Application;
import io.mangoo.email.Mail;
import io.mangoo.exceptions.MangooMailerException;
import io.mangoo.test.email.SmtpMock;

/**
 * 
 * @author svenkubiak
 *
 */
public class MailTest {
    private static GreenMail greenMail;
    private static SmtpMock smtp;

    @BeforeClass
    public static void init() throws FolderException {
        smtp = Application.getInstance(SmtpMock.class);
        smtp.start();
        greenMail = smtp.getGreenMail();
    }
    
    @Test
    public void testSimpleEmail() throws MangooMailerException, MessagingException, IOException, FolderException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("winterfell.com").length, equalTo(0));
        
        //when
        Mail.newMail()
            .withFrom("Jon Snow <jon.snow@winterfell.com>")
            .withRecipient("sansa.stark@winterfell.com")
            .withSubject("Lord of light")
            .withTemplate("emails/simple.ftl")
            .withContent("king", "geofrey")
            .send();
        
        //then
        assertThat(greenMail.getReceivedMessagesForDomain("winterfell.com")[0].getContent().toString(), containsString("geofrey"));
        assertThat(greenMail.getReceivedMessagesForDomain("winterfell.com").length, equalTo(1));
    }
    
    @Test
    public void testHtmlEmail() throws MangooMailerException, FolderException, IOException, MessagingException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("thewall.com").length, equalTo(0));
        
        //when
        Mail.newMail()
            .withFrom("Jon Snow <jon.snow@thewall.com>")
            .withRecipient("sansa.stark@thewall.com")
            .withSubject("Lord of light")
            .withTemplate("emails/html.ftl")
            .withContent("king", "kong")
            .send();
        
        //then
        assertThat(greenMail.getReceivedMessagesForDomain("thewall.com").length, equalTo(1));
        assertThat(greenMail.getReceivedMessagesForDomain("thewall.com")[0].getContent().toString(), containsString("kong"));
    }

    @Test
    public void testMultiPartEmail() throws MangooMailerException, IOException, FolderException, MessagingException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(0));
        File file = new File(UUID.randomUUID().toString());
        file.createNewFile();
        
        //when
        Mail.newMail()
            .withFrom("Jon Snow <jon.snow@westeros.com>")
            .withRecipient("sansa.stark@westeros.com")
            .withSubject("Lord of light")
            .withTemplate("emails/multipart.ftl")
            .withContent("name", "raven")
            .withContent("king", "none")
            .withAttachment(file)
            .send();
        
        //then
        assertThat(file.delete(), equalTo(true));
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1));
    }
    
    @Test
    public void testBody() throws MangooMailerException, IOException, FolderException, MessagingException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(0));
        
        //when
        Mail.newMail()
            .withFrom("Jon Snow <jon.snow@westeros.com>")
            .withRecipient("sansa.stark@westeros.com")
            .withSubject("Lord of light")
            .withBody("what is dead may never die")
            .send();
        
        //then
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1));
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getContent().toString(), containsString("what is dead may never die"));
    }
    
    @Test
    public void testTemplatePath() {
        //given
        Mail mail1 = Mail.newMail();
        Mail mail2 = Mail.newMail();
        
        //when
        mail1.withTemplate("/foo/vbar");
        mail2.withTemplate("\\foo\\vbar");
        
        //then
        assertThat(mail1.getTemplate(), equalTo("foo/vbar"));
        assertThat(mail2.getTemplate(), equalTo("foo\\vbar"));
    }
    
    @Test
    public void testPlainEncoding() throws FolderException, MangooMailerException, IOException, MessagingException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(0));
        
        //when
        Mail.newMail()
            .withFrom("Jon Snow <jon.snow@westeros.com>")
            .withRecipient("sansa.stark@westeros.com")
            .withSubject("ÄÜÖ")
            .withBody("This is a body with üäö")
            .send();
        
        //then
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1));
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getSubject().toString(), equalTo("ÄÜÖ"));
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getContent().toString(), equalTo("This is a body with üäö\r\n"));
    }
    
    @Test
    public void testHtmlEncoding() throws FolderException, MangooMailerException, MessagingException, IOException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(0));
        
        //when
        Mail.newHtmlMail()
            .withFrom("Jon Snow <jon.snow@westeros.com>")
            .withRecipient("sansa.stark@westeros.com")
            .withSubject("ÄÜÖ")
            .withBody("This is a body with üäö")
            .send();
        
        //then
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1));
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getSubject().toString(), equalTo("ÄÜÖ"));
        assertThat(((MimeMultipart)(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getContent())).getBodyPart(0).getContent().toString(), equalTo("This is a body with üäö"));
    }
    
    @Test
    public void testMultipartEncoding() throws FolderException, MangooMailerException, MessagingException, IOException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(0));
        File file = new File(UUID.randomUUID().toString());
        file.createNewFile();
        
        //when
        Mail.newMail()
            .withFrom("Jon Snow <jon.snow@westeros.com>")
            .withRecipient("sansa.stark@westeros.com")
            .withSubject("ÄÜÖ")
            .withBody("This is a body with üäö")
            .withAttachment(file)
            .send();
        
        //then
        assertThat(file.delete(), equalTo(true));
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1));
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getSubject().toString(), equalTo("ÄÜÖ"));
        assertThat(((MimeMultipart)(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getContent())).getBodyPart(0).getContent().toString(), equalTo("This is a body with üäö"));
    }
    
    @AfterClass
    public static void shutdown() {
        smtp.stop();
    }
}