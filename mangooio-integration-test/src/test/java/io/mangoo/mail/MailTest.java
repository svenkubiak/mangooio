package io.mangoo.mail;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.email.Mail;
import io.mangoo.exceptions.MangooMailerException;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.test.email.SmtpMock;
import jodd.mail.Email;
import jodd.mail.EmailAttachment;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class MailTest {
    private static GreenMail greenMail;
    private static SmtpMock smtp;

    @BeforeAll
    public static void init() throws FolderException {
        smtp = Application.getInstance(SmtpMock.class);
        smtp.start();
        greenMail = smtp.getGreenMail();
    }
    
    @Test
    public void testSimpleEmail() throws MangooMailerException, MessagingException, IOException, FolderException, MangooTemplateEngineException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("winterfell.com").length, equalTo(0));
        Map<String, Object> content = new HashMap<>();
        content.put("king", "geofrey");
        
        //when
        Mail.build()
            .withBuilder(Email.create()
                    .from("Jon Snow <jon.snow@winterfell.com>")
                    .to("sansa.stark@winterfell.com")
                    .subject("Lord of light"))
            .templateMessage("emails/simple.ftl", content)
            .send();
        
        //then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("winterfell.com")[0].getContent().toString(), containsString("geofrey")));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("winterfell.com").length, equalTo(1)));
    }
    
    @Test
    public void testHtmlEmail() throws MangooMailerException, FolderException, IOException, MessagingException, MangooTemplateEngineException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("thewall.com").length, equalTo(0));
        Map<String, Object> content = new HashMap<>();
        content.put("king", "kong");
        
        //when
        Mail.build()
            .withBuilder(Email.create()
                    .from("Jon Snow <jon.snow@winterfell.com>")
                    .to("sansa.stark@thewall.com")
                    .subject("Lord of light"))
            .templateMessage("emails/html.ftl", content)
            .send();
        
        //then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("thewall.com").length, equalTo(1)));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("thewall.com")[0].getContent().toString(), containsString("kong")));
    }

    @Test
    public void testMultiPartEmail() throws MangooMailerException, IOException, FolderException, MessagingException, MangooTemplateEngineException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(0));
        File file = new File(UUID.randomUUID().toString());
        file.createNewFile();
        Map<String, Object> content = new HashMap<>();
        content.put("name", "raven");
        content.put("king", "none");
        
        //when
        Mail.build()
            .withBuilder(Email.create()
                    .from("Jon Snow <jon.snow@winterfell.com>")
                    .to("sansa.stark@westeros.com")
                    .subject("Lord of light")
                    .attachment(EmailAttachment.with()
                            .name("some name")
                            .content(file)))
            .templateMessage("emails/multipart.ftl", content)
            .send();
        
        //then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(file.delete(), equalTo(true)));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1)));
    }
    
    @Test
    public void testBody() throws MangooMailerException, IOException, FolderException, MessagingException, InterruptedException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(0));
        
        //when
        Mail.build()
            .withBuilder(Email.create()
                    .from("Jon Snow <jon.snow@winterfell.com>")
                    .to("sansa.stark@westeros.com")
                    .subject("Lord of light")
                    .textMessage("what is dead may never die"))
            .send();
        
        //then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1)));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getContent().toString(), containsString("what is dead may never die")));
    }
    
    @Test
    public void testPlainEncoding() throws FolderException, MangooMailerException, IOException, MessagingException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(0));
        
        //when
        Mail.build()
            .withBuilder(Email.create()
                    .from("Jon Snow <jon.snow@winterfell.com>")
                    .to("sansa.stark@westeros.com")
                    .subject("ÄÜÖ")
                    .textMessage("This is a body with üäö"))
            .send();
        
        //then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1)));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getSubject().toString(), equalTo("ÄÜÖ")));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getContent().toString(), equalTo("This is a body with üäö\r\n")));
    }
    
    @Test
    public void testHtmlEncoding() throws FolderException, MangooMailerException, MessagingException, IOException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(0));
        
        //when
        Mail.build()
            .withBuilder(Email.create()
                    .from("Jon Snow <jon.snow@winterfell.com>")
                    .to("sansa.stark@westeros.com")
                    .subject("ÄÜÖ")
                    .htmlMessage("This is a body with üäö"))
            .send();
        
        //then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1)));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getSubject().toString(), equalTo("ÄÜÖ")));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat((greenMail.getReceivedMessagesForDomain("westeros.com")[0].getContent()), equalTo("This is a body with üäö\r\n")));
    }
    
    @AfterAll
    public static void shutdown() {
        smtp.stop();
    }
}