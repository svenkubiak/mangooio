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
    private static GreenMail greenMail;
    private static Smtp fakeSMTP;

    @BeforeClass
    public static void init() {
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
            .withTemplate("emails/simple.ftl")
            .withContent("king", "geofrey")
            .send();
        
        //then
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
            .withTemplate("emails/html.ftl")
            .withContent("king", "kong")
            .send();
        
        //then
    }

    @Test
    public void MultiPartEmailTest() throws MangooMailerException, IOException {
        //given
        File file = new File(UUID.randomUUID().toString());
        file.createNewFile();
        
        //when
        Mail.newMail()
            .withFrom("Jon Snow <jon.snow@winterfell.com>")
            .withRecipient("sansa.stark@winterfell.com")
            .withCC("bram.stark@winterfell.com")
            .withBCC("nightswatch@castkeblack.org")
            .withSubject("Lord of light")
            .withTemplate("emails/multipart.ftl")
            .withContent("name", "raven")
            .withContent("king", "none")
            .withAttachment(file)
            .send();
        
        //then
        assertThat(file.delete(), equalTo(true));
    }
}