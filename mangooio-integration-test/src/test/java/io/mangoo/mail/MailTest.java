package io.mangoo.mail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.BeforeClass;
import org.junit.Test;

import com.icegreen.greenmail.util.GreenMail;

import io.mangoo.core.Application;
import io.mangoo.exceptions.MangooMailerException;

/**
 * 
 * @author svenkubiak
 *
 */
public class MailTest {
    private static Mailer mailer;
    private static GreenMail greenMail;
    private static Smtp fakeSMTP;

    @BeforeClass
    public static void init() {
        mailer = Application.getInstance(Mailer.class);
        fakeSMTP = Application.getInstance(Smtp.class);
        fakeSMTP.start();
        greenMail = fakeSMTP.getGreenMail();
    }
    
    @Test
    public void SimpleEmailTest() throws MangooMailerException, MessagingException, IOException {
        //given
        assertThat(greenMail.getReceviedMessagesForDomain("winterfell.com").length, equalTo(0));
        
        //when
        Mail.newMail()
            .withFrom("Jon Snow <jon.snow@winterfell.com>")
            .withRecipient("sansa.stark@winterfell.com")
            .withCC("bram.stark@winterfell.com")
            .withBCC("nightswatch@castkeblack.org")
            .withSubject("Lord of light")
            .withTemplate("/emails/simple.ftl")
            .withContent("king", "geofrey")
            .send();
        
        //then
        MimeMessage[] receviedMessagesForDomain = greenMail.getReceviedMessagesForDomain("winterfell.com");
        MimeMessage mimeMessage = receviedMessagesForDomain[0];
        assertEquals(receviedMessagesForDomain.length, equalTo(1));
        assertEquals(mimeMessage.getFrom()[0], equalTo("Jon Snow <jon.snow@winterfell.com>"));
    }
    
    @Test
    public void HtmlEmailTest() throws MangooMailerException {
        //when
        Mail.newMail()
            .withFrom("Jon Snow <jon.snow@winterfell.com>")
            .withRecipient("sansa.stark@winterfell.com")
            .withCC("bram.stark@winterfell.com")
            .withBCC("nightswatch@castkeblack.org")
            .withSubject("Lord of light")
            .withTemplate("/emails/html.ftl")
            .withContent("king", "kong")
            .send();
        
        //then
    }

    @Test
    public void MultiPartEmailTest() throws MangooMailerException {
        //given
        File file = new File(UUID.randomUUID().toString());
        
        //when
        Mail.newMail()
            .withFrom("Jon Snow <jon.snow@winterfell.com>")
            .withRecipient("sansa.stark@winterfell.com")
            .withCC("bram.stark@winterfell.com")
            .withBCC("nightswatch@castkeblack.org")
            .withSubject("Lord of light")
            .withTemplate("/emails/multipart.ftl")
            .withContent("name", "raven")
            .withContent("king", "none")
            .withAttachment(file)
            .send();
    }
}
