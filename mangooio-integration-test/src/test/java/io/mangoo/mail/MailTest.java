package io.mangoo.mail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.mail.MessagingException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;

import io.mangoo.core.Application;
import io.mangoo.email.Mail;
import io.mangoo.email.Smtp;
import io.mangoo.exceptions.MangooMailerException;

/**
 * 
 * @author svenkubiak
 *
 */
public class MailTest {
    private static GreenMail greenMail;
    private static Smtp smtp;

    @BeforeClass
    public static void init() throws FolderException {
        smtp = Application.getInstance(Smtp.class);
        smtp.start();
        greenMail = smtp.getGreenMail();
    }
    
    @Test
    public void SimpleEmailTest() throws MangooMailerException, MessagingException, IOException, FolderException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceviedMessagesForDomain("winterfell.com").length, equalTo(0));
        
        //when
        Mail.newMail()
            .withFrom("Jon Snow <jon.snow@winterfell.com>")
            .withRecipient("sansa.stark@winterfell.com")
            .withSubject("Lord of light")
            .withTemplate("emails/simple.ftl")
            .withContent("king", "geofrey")
            .send();
        
        //then
        assertThat(greenMail.getReceviedMessagesForDomain("winterfell.com")[0].getContent().toString(), containsString("geofrey"));
        assertThat(greenMail.getReceviedMessagesForDomain("winterfell.com").length, equalTo(1));
    }
    
    @Test
    public void HtmlEmailTest() throws MangooMailerException, FolderException, IOException, MessagingException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceviedMessagesForDomain("thewall.com").length, equalTo(0));
        
        //when
        Mail.newMail()
            .withFrom("Jon Snow <jon.snow@thewall.com>")
            .withRecipient("sansa.stark@thewall.com")
            .withSubject("Lord of light")
            .withTemplate("emails/html.ftl")
            .withContent("king", "kong")
            .send();
        
        //then
        assertThat(greenMail.getReceviedMessagesForDomain("thewall.com").length, equalTo(1));
        assertThat(greenMail.getReceviedMessagesForDomain("thewall.com")[0].getContent().toString(), containsString("kong"));
    }

    @Test
    public void MultiPartEmailTest() throws MangooMailerException, IOException, FolderException, MessagingException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceviedMessagesForDomain("westeros.com").length, equalTo(0));
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
        assertThat(greenMail.getReceviedMessagesForDomain("westeros.com").length, equalTo(1));
    }
    
    @Test
    public void BodyTest() throws MangooMailerException, IOException, FolderException, MessagingException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceviedMessagesForDomain("westeros.com").length, equalTo(0));
        
        //when
        Mail.newMail()
            .withFrom("Jon Snow <jon.snow@westeros.com>")
            .withRecipient("sansa.stark@westeros.com")
            .withSubject("Lord of light")
            .withBody("what is dead may never die")
            .send();
        
        //then
        assertThat(greenMail.getReceviedMessagesForDomain("westeros.com").length, equalTo(1));
        assertThat(greenMail.getReceviedMessagesForDomain("westeros.com")[0].getContent().toString(), containsString("what is dead may never die"));
    }
    
    @AfterClass
    public static void shutdown() {
        smtp.stop();
    }
}