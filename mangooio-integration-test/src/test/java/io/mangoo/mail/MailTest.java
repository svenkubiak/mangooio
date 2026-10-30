package io.mangoo.mail;

import io.mangoo.TestExtension;
import io.mangoo.constants.Required;
import io.mangoo.email.Mail;
import io.mangoo.exceptions.MangooTemplateEngineException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({TestExtension.class})
class MailTest {
    
    @Test
    void testHeaderValueWithLineBreakIsRejected() {
        for (String value : new String[] {"a\r\nBcc: evil@x.com", "a\rBcc: evil@x.com", "a\nBcc: evil@x.com"}) {
            assertThrows(IllegalArgumentException.class, () -> Mail.newMail().header("X-Ref", value));
        }
    }

    @Test
    void testInvalidHeaderNameIsRejected() {
        for (String name : new String[] {"X-A: b", "X A", "X-A\r\nBcc", ""}) {
            assertThrows(IllegalArgumentException.class, () -> Mail.newMail().header(name, "value"));
        }
    }

    @Test
    void testValidHeaderIsAccepted() {
        //when
        Mail mail = Mail.newMail().header("X-Campaign", "welcome");

        //then
        assertThat(mail.getMailHeaders().get("X-Campaign"), equalTo("welcome"));
    }

    @Test
    void testPriority() {
        //given
        int priority = 4;
        
        //when
        Mail mail = Mail.newMail().priority(priority);
        
        //then
        assertThat(mail.getMailHeaders().get("X-Priority"), equalTo(String.valueOf(priority)));
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

    @Test
    void testTemplateWithImmutableContent() throws MangooTemplateEngineException {
        //given
        String message = UUID.randomUUID().toString();
        Map<String, Object> content = Map.of("king", message);

        //when
        Mail mail = Mail.newMail()
                .textMessage("emails/html.ftl", content);

        //then
        assertThat(mail.getMailText(), equalTo("html template\n" + message));
        assertThat(content.size(), equalTo(1));
    }

    @Test
    void testTemplateWithoutContentIsRejected() {
        //when
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> Mail.newMail().textMessage("emails/html.ftl", null));

        //then
        assertThat(exception.getMessage(), equalTo(Required.CONTENT));
    }

    @Test
    void testFromWithoutNameIsRejected() {
        //when
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> Mail.newMail().from(null, "jon.snow@winterfell.com"));

        //then
        assertThat(exception.getMessage(), equalTo(Required.NAME));
    }

    @Test
    void testFromWithoutAddressIsRejected() {
        //when
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> Mail.newMail().from("Jon Snow", null));

        //then
        assertThat(exception.getMessage(), equalTo(Required.FROM));
    }
}
