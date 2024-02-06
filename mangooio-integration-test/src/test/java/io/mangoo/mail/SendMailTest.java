package io.mangoo.mail;

import com.google.common.io.Files;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.email.Mail;
import io.mangoo.exceptions.MangooMailerException;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.test.email.SmtpMock;
import io.mangoo.utils.MangooUtils;
import jakarta.mail.MessagingException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class SendMailTest {
    private static GreenMail greenMail;
    private static SmtpMock smtp;

    @BeforeAll
    public static void init() throws FolderException {
        smtp = Application.getInstance(SmtpMock.class);
        smtp.start();
        greenMail = smtp.getGreenMail();
    }
    
    @Test
    void testHtmlEmail() throws MangooMailerException, FolderException, IOException, MangooTemplateEngineException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("thewall.com").length, equalTo(0));
        Map<String, Object> content = new HashMap<>();
        content.put("king", "kong");
        
        //when
        Mail.newMail()
            .from("Jon Snow", "jon.snow@winterfell.com")
            .to("sansa.stark@thewall.com")
            .subject("Lord of light")
            .htmlMessage("emails/html.ftl", content)
            .send();
        
        //then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("thewall.com").length, equalTo(1)));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("thewall.com")[0].getContent().toString(), containsString("kong")));
    }
    
    @Test
    void testMultiPartEmailFile() throws MangooMailerException, IOException, FolderException, MangooTemplateEngineException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(0));
        File file = new File(UUID.randomUUID().toString() + ".txt");
        file.createNewFile();
        Files.asCharSink(file, StandardCharsets.UTF_8).write(UUID.randomUUID().toString());
        
        Map<String, Object> content = new HashMap<>();
        content.put("name", "raven");
        content.put("king", "none");
        
        //when
        Mail.newMail()
            .from("Jon Snow", "jon.snow@winterfell.com")
            .to("sansa.stark@westeros.com")
            .subject("Lord of light")
            .attachment(file.toPath())
            .htmlMessage("emails/multipart.ftl", content)
            .send();
        
        //then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(file.delete(), equalTo(true)));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1)));
    }
    
    @Test
    void testBody() throws MangooMailerException, IOException, FolderException, InterruptedException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(0));
        
        //when
        Mail.newMail()
            .from("Jon snow", "jon.snow@winterfell.com")
            .to("sansa.stark@westeros.com")
            .subject("Lord of light")
            .textMessage("what is dead may never die")
            .send();
        
        //then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1)));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getContent().toString(), containsString("what is dead may never die")));
    }
    
    @Test
    void testConcurrentBody() throws MangooMailerException, IOException, FolderException, InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String domain = MangooUtils.randomString(16) + ".com";
            String subject = MangooUtils.randomString(32);
            
            //when
            Mail.newMail()
                .from("Jon snow", "jon.snow@winterfell.com")
                .to("sansa.stark@" + domain)
                .subject(subject)
                .textMessage("what is dead may never die")
                .send();
            
            // then
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(domain).length, equalTo(1)));

            return greenMail.getReceivedMessagesForDomain(domain)[0].getSubject().equals(subject);
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testPlainEncoding() throws FolderException, MangooMailerException, IOException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(0));
        
        //when
        Mail.newMail()
            .from("John Snow", "jon.snow@winterfell.com")
            .to("sansa.stark@westeros.com")
            .subject("ÄÜÖ")
            .textMessage("This is a body with üäö")
            .send();
        
        //then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1)));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getSubject().toString(), equalTo("ÄÜÖ")));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getContent().toString(), equalTo("This is a body with üäö")));
    }
    
    @Test
    void testHtmlEncoding() throws FolderException, MangooMailerException, MessagingException, IOException {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(0));
        
        //when
        Mail.newMail()
            .from("John Snow", "jon.snow@winterfell.com")
            .to("sansa.stark@westeros.com")
            .subject("ÄÜÖ")
            .htmlMessage("This is a body with üäö")
            .send();
        
        //then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1)));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com")[0].getSubject().toString(), equalTo("ÄÜÖ")));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat((greenMail.getReceivedMessagesForDomain("westeros.com")[0].getContent()), equalTo("This is a body with üäö")));
    }
}