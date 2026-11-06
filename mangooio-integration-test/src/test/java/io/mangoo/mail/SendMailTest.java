package io.mangoo.mail;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.email.Mail;
import io.mangoo.exceptions.MangooMailerException;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.test.concurrent.ConcurrentRunner;
import io.mangoo.test.email.SmtpMock;
import io.mangoo.utils.CommonUtils;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith({TestExtension.class})
@Execution(ExecutionMode.SAME_THREAD)
class SendMailTest {
    private static GreenMail greenMail;
    private static SmtpMock smtp;

    @BeforeAll
    public static void init() {
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
    void testAttachmentIsReadFromGivenPath() throws Exception {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        String fileName = "invoice-" + UUID.randomUUID() + ".txt";
        Path directory = java.nio.file.Files.createTempDirectory("mangooio-attachment");
        Path attachment = directory.resolve(fileName);
        Path decoy = Path.of(fileName);
        java.nio.file.Files.writeString(attachment, "correct attachment");
        java.nio.file.Files.writeString(decoy, "foreign file from the working directory");

        try {
            //when
            Mail.newMail()
                .from("Jon Snow", "jon.snow@winterfell.com")
                .to("arya.stark@westeros.com")
                .subject("Invoice")
                .attachment(attachment)
                .textMessage("See attachment")
                .send();

            //then
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1)));
            var multipart = (Multipart) greenMail.getReceivedMessagesForDomain("westeros.com")[0].getContent();
            var attachmentPart = multipart.getBodyPart(1);
            assertThat(attachmentPart.getFileName(), equalTo(fileName));
            assertThat(new String(attachmentPart.getInputStream().readAllBytes(), StandardCharsets.UTF_8), equalTo("correct attachment"));
        } finally {
            java.nio.file.Files.deleteIfExists(decoy);
            java.nio.file.Files.deleteIfExists(attachment);
            java.nio.file.Files.deleteIfExists(directory);
        }
    }

    @Test
    void testHtmlMailWithAttachmentKeepsHtmlBody() throws Exception {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        Path attachment = java.nio.file.Files.createTempFile("mangooio-attachment", ".txt");
        java.nio.file.Files.writeString(attachment, "attachment");
        Map<String, Object> content = new HashMap<>();
        content.put("name", "raven");
        content.put("king", "none");

        try {
            //when
            Mail.newMail()
                .from("Jon Snow", "jon.snow@winterfell.com")
                .to("arya.stark@westeros.com")
                .subject("Html with attachment")
                .attachment(attachment)
                .htmlMessage("emails/multipart.ftl", content)
                .send();

            //then
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1)));
            var multipart = (Multipart) greenMail.getReceivedMessagesForDomain("westeros.com")[0].getContent();
            assertThat(multipart.getBodyPart(0).isMimeType("text/html"), equalTo(true));
            assertThat(multipart.getBodyPart(1).getFileName(), equalTo(attachment.getFileName().toString()));
        } finally {
            java.nio.file.Files.deleteIfExists(attachment);
        }
    }

    @Test
    void testTextMailWithAttachmentKeepsTextBody() throws Exception {
        //given
        greenMail.purgeEmailFromAllMailboxes();
        Path attachment = java.nio.file.Files.createTempFile("mangooio-attachment", ".txt");
        java.nio.file.Files.writeString(attachment, "attachment");

        try {
            //when
            Mail.newMail()
                .from("Jon Snow", "jon.snow@winterfell.com")
                .to("arya.stark@westeros.com")
                .subject("Text with attachment")
                .attachment(attachment)
                .textMessage("plain text")
                .send();

            //then
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1)));
            var multipart = (Multipart) greenMail.getReceivedMessagesForDomain("westeros.com")[0].getContent();
            assertThat(multipart.getBodyPart(0).isMimeType("text/plain"), equalTo(true));
        } finally {
            java.nio.file.Files.deleteIfExists(attachment);
        }
    }

    @Test
    void testMailWithoutFromUsesSmtpFrom() throws Exception {
        //given
        greenMail.purgeEmailFromAllMailboxes();

        //when
        Mail.newMail()
            .to("sansa.stark@westeros.com")
            .subject("Default sender")
            .textMessage("Sent without from()")
            .send();

        //then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain("westeros.com").length, equalTo(1)));
        var from = (InternetAddress) greenMail.getReceivedMessagesForDomain("westeros.com")[0].getFrom()[0];
        assertThat(from.getAddress(), equalTo("noreply@mangoo.local"));
        assertThat(from.getPersonal(), equalTo("mangoo"));
    }

    @Test
    void testConcurrentBody() {
        MatcherAssert.assertThat(t -> {
            //given
            String domain = CommonUtils.uuidV6() + ".com";
            String subject = CommonUtils.randomString(32);
            
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
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
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

    @Test
    void testCcBccReplyToAndHeadersAreDelivered() throws Exception {
        //given
        greenMail.purgeEmailFromAllMailboxes();

        //when
        Mail.newMail()
            .from("Jon Snow", "jon.snow@winterfell.com")
            .to("sansa.stark@westeros.com")
            .cc("arya.stark@riverrun.com")
            .bcc("bran.stark@eyrie.com")
            .replyTo("night.watch@thewall.com")
            .header("X-Mangoo-Test", "value")
            .priority(1)
            .subject("Recipients")
            .textMessage("body")
            .send();

        //then
        // One delivery per recipient: to, cc and bcc
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(greenMail.getReceivedMessages().length, equalTo(3)));

        var message = greenMail.getReceivedMessages()[0];
        assertThat(((InternetAddress) message.getRecipients(Message.RecipientType.CC)[0]).getAddress(), equalTo("arya.stark@riverrun.com"));
        assertThat(((InternetAddress) message.getReplyTo()[0]).getAddress(), equalTo("night.watch@thewall.com"));
        assertThat(message.getHeader("X-Mangoo-Test")[0], equalTo("value"));
        assertThat(message.getHeader("X-Priority")[0], equalTo("1"));
        assertThat(message.getHeader("Bcc"), nullValue());
        assertThat(message.getRecipients(Message.RecipientType.BCC), nullValue());
    }
}
