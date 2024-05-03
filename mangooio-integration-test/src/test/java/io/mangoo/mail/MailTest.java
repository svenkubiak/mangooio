package io.mangoo.mail;

import com.google.common.io.Files;
import io.mangoo.TestExtension;
import io.mangoo.email.Mail;
import io.mangoo.exceptions.MangooTemplateEngineException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class MailTest {
    
    @Test
    void testTos() {
        //given
        String toA = UUID.randomUUID().toString() + "@bar.com";
        String toB = UUID.randomUUID().toString() + "@foo.com";
        String [] tos = {toA, toB};
        
        //when
        Mail mail = Mail.newMail().to(tos);
        
        //then
        assertThat(mail.getMailTos(), not(nullValue()));
        assertThat(mail.getMailTos().stream().anyMatch(toA::equals), equalTo(true));
        assertThat(mail.getMailTos().stream().anyMatch(toB::equals), equalTo(true));
    }
    
    @Test
    void testTo() {
        //given
        String to = UUID.randomUUID().toString() + "@bar.com";
        
        //when
        Mail mail = Mail.newMail().to(to);
        
        //then
        assertThat(mail.getMailTos(), not(nullValue()));
        assertThat(mail.getMailTos().stream().anyMatch(to::equals), equalTo(true));
    }
    
    @Test
    void testCcs() {
        //given
        String ccA = UUID.randomUUID().toString() + "@bar.com";
        String ccB = UUID.randomUUID().toString() + "@foo.com";
        String [] ccs = {ccA, ccB};
        
        //when
        Mail mail = Mail.newMail().cc(ccs);
        
        //then
        assertThat(mail.getMailCcs(), not(nullValue()));
        assertThat(mail.getMailCcs().stream().anyMatch(ccA::equals), equalTo(true));
        assertThat(mail.getMailCcs().stream().anyMatch(ccB::equals), equalTo(true));
    }
    
    @Test
    void testCc() {
        //given
        String cc = UUID.randomUUID().toString() + "@bar.com";
        
        //when
        Mail mail = Mail.newMail().cc(cc);
        
        //then
        assertThat(mail.getMailCcs(), not(nullValue()));
        assertThat(mail.getMailCcs().stream().anyMatch(cc::equals), equalTo(true));
    }
    
    @Test
    void testBccs() {
        //given
        String bccA = UUID.randomUUID().toString() + "@bar.com";
        String bccB = UUID.randomUUID().toString() + "@foo.com";
        String [] bccs = {bccA, bccB};
        
        //when
        Mail mail = Mail.newMail().bcc(bccs);
        
        //then
        assertThat(mail.getMailBccs(), not(nullValue()));
        assertThat(mail.getMailBccs().stream().anyMatch(bccA::equals), equalTo(true));
        assertThat(mail.getMailBccs().stream().anyMatch(bccB::equals), equalTo(true));
    }
    
    @Test
    void testBcc() {
        //given
        String bcc = UUID.randomUUID().toString() + "@bar.com";
        
        //when
        Mail mail = Mail.newMail().bcc(bcc);
        
        //then
        assertThat(mail.getMailBccs(), not(nullValue()));
        assertThat(mail.getMailBccs().stream().anyMatch(bcc::equals), equalTo(true));
    }
    
    @Test
    void testSubject() {
        //given
        String subject = UUID.randomUUID().toString();
        
        //when
        Mail mail = Mail.newMail().subject(subject);
        
        //then
        assertThat(mail.getMailSubject(), not(nullValue()));
        assertThat(mail.getMailSubject(), equalTo(subject));
    }
    
    @Test
    void testFromAddress() {
        //given
        String from = "foo@bar.com";
        
        //when
        Mail mail = Mail.newMail().from(from);
        
        //then
        assertThat(mail.getMailFromAddress(), not(nullValue()));
        assertThat(mail.getMailFromAddress(), equalTo(from));
    }
    
    @Test
    void testFromAddressAndName() {
        //given
        String fromAddress = "foo@bar.com";
        String fromName = "Peter Parker";
        
        //when
        Mail mail = Mail.newMail().from(fromName, fromAddress);
        
        //then
        assertThat(mail.getMailFromName(), not(nullValue()));
        assertThat(mail.getMailFromName(), equalTo(fromName));
        assertThat(mail.getMailFromAddress(), not(nullValue()));
        assertThat(mail.getMailFromAddress(), equalTo(fromAddress));
    }
    
    @Test
    void testHeader() {
        //given
        String myHeader = "myHeader";
        String myHeaderValue = "myHeaderValue";
        
        //when
        Mail mail = Mail.newMail().header(myHeader, myHeaderValue);
        
        //then
        assertThat(mail.getMailHeaders(), not(nullValue()));
        assertThat(mail.getMailHeaders().get(myHeader), equalTo(myHeaderValue));
    }
    
    @Test
    void testReplyTo() {
        //given
        String replyTo = "foo@bar.com";
        
        //when
        Mail mail = Mail.newMail().replyTo(replyTo);
        
        //then
        assertThat(mail.getMailReplyTo(), not(nullValue()));
        assertThat(mail.getMailReplyTo(), equalTo(replyTo));
    }
    
    @Test
    void testPriority() {
        //given
        int priority = 4;
        
        //when
        Mail mail = Mail.newMail().priority(priority);
        
        //then
        assertThat(mail.getMailHeaders(), not(nullValue()));
        assertThat(mail.getMailHeaders().get("X-Priority"), equalTo(String.valueOf(priority)));
    }
    
    @Test
    void testAttachment() throws IOException {
        //given
        File file = new File(UUID.randomUUID().toString() + ".txt");
        file.createNewFile();
        Files.asCharSink(file, StandardCharsets.UTF_8).write(UUID.randomUUID().toString());
        
        //when
        Mail mail = Mail.newMail().attachment(file.toPath());
        
        //then
        assertThat(mail.hasAttachments(), equalTo(true));
        assertThat(mail.getMailAttachments().stream().anyMatch(file.toPath()::equals), equalTo(true));
        assertThat(file.delete(), equalTo(true));
    }
    
    @Test
    void testAttachments() throws IOException {
        //given
        File fileA = new File(UUID.randomUUID().toString() + ".txt");
        fileA.createNewFile();
        Files.asCharSink(fileA, StandardCharsets.UTF_8).write(UUID.randomUUID().toString());
        
        File fileB = new File(UUID.randomUUID().toString() + ".txt");
        fileB.createNewFile();
        
        Files.asCharSink(fileB, StandardCharsets.UTF_8).write(UUID.randomUUID().toString());
        
        //when
        Mail mail = Mail.newMail().attachments(List.of(fileA.toPath(), fileB.toPath()));
        
        //then
        assertThat(mail.hasAttachments(), equalTo(true));
        assertThat(mail.getMailAttachments().stream().anyMatch(fileA.toPath()::equals), equalTo(true));
        assertThat(mail.getMailAttachments().stream().anyMatch(fileB.toPath()::equals), equalTo(true));
        assertThat(fileA.delete(), equalTo(true));
        assertThat(fileB.delete(), equalTo(true));
    }
    
    @Test
    void testTextMessage() {
        //given
        String message = UUID.randomUUID().toString();
        
        //when
        Mail mail = Mail.newMail().textMessage(message);
        
        //then
        assertThat(mail.getMailText(), not(nullValue()));
        assertThat(mail.getMailText(), equalTo(message));
    }
    
    @Test
    void testHtmlMessage() {
        //given
        String message = UUID.randomUUID().toString();
        
        //when
        Mail mail = Mail.newMail().htmlMessage(message);
        
        //then
        assertThat(mail.isMailHtml(), equalTo(true));
        assertThat(mail.getMailText(), not(nullValue()));
        assertThat(mail.getMailText(), equalTo(message));
    }
    
    @Test
    void testTextMessageWithTemplate() throws MangooTemplateEngineException {
        //given
        String message = UUID.randomUUID().toString();
        Map<String, Object> content = new HashMap<>();
        content.put("king", message);
        
        //when
        Mail mail = Mail.newMail()
                .textMessage("emails/html.ftl", content);
        
        //then
        assertThat(mail.getMailText(), not(nullValue()));
        assertThat(mail.getMailText(), equalTo("html template\n" + message));
    }
    
    @Test
    void testHtmlMessageWithTemplate() throws MangooTemplateEngineException {
        //given
        String message = UUID.randomUUID().toString();
        Map<String, Object> content = new HashMap<>();
        content.put("king", message);
        
        //when
        Mail mail = Mail.newMail()
                .htmlMessage("emails/html.ftl", content);
        
        //then
        assertThat(mail.isMailHtml(), equalTo(true));
        assertThat(mail.getMailText(), not(nullValue()));
        assertThat(mail.getMailText(), equalTo("html template\n" + message));
    }
}