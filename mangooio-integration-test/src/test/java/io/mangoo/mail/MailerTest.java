package io.mangoo.mail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Resources;
import com.icegreen.greenmail.util.GreenMail;

import io.mangoo.core.Application;
import io.mangoo.email.Mailer;
import io.mangoo.email.Smtp;

public class MailerTest {
    private static Mailer mailer;
    private static GreenMail greenMail;
    private static Smtp smtp;

    @BeforeClass
    public static void init() {
        mailer = Application.getInstance(Mailer.class);
        smtp = Application.getInstance(Smtp.class);
        smtp.start();
        greenMail = smtp.getGreenMail();
    }

    @Test
    public void textTest() throws Exception {
        //given
        assertThat(greenMail.getReceviedMessagesForDomain("text.com").length, equalTo(0));
        final Email email = new SimpleEmail();
        email.setFrom("user@test.com");
        email.setSubject("plainTextTest");
        email.setMsg("This is a test plan text message");
        email.addTo("foo@text.com");

        //when
        mailer.send(email);

        //then
        assertThat(greenMail.getReceviedMessagesForDomain("text.com").length, equalTo(1));
    }

    @Test
    public void multipartTest() throws Exception {
        //given
        assertThat(greenMail.getReceviedMessagesForDomain("multipart.com").length, equalTo(0));
        final EmailAttachment attachment = new EmailAttachment();
        attachment.setPath(Resources.getResource("attachment.txt").getPath());
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setDescription("Picture of John");
        attachment.setName("John");

        final MultiPartEmail email = new MultiPartEmail();
        email.addTo("jdoe@multipart.com", "John Doe");
        email.setFrom("me@apache.org", "Me");
        email.setSubject("The picture");
        email.setMsg("Here is the picture you wanted");
        email.attach(attachment);

        //when
        mailer.send(email);

        //then
        assertThat(greenMail.getReceviedMessagesForDomain("multipart.com").length, equalTo(1));
    }

    @Test
    public void htmlTest() throws Exception {
        //given
        assertThat(greenMail.getReceviedMessagesForDomain("html.org").length, equalTo(0));
        final HtmlEmail email = new HtmlEmail();
        email.setHostName("mail.myserver.com");
        email.addTo("jdoe@html.org", "John Doe");
        email.setFrom("me@apache.org", "Me");
        email.setSubject("Test email with inline image");
        email.setHtmlMsg("<html>The apache logo - </html>");
        email.setTextMsg("Your email client does not support HTML messages");

        //when
        mailer.send(email);

        //then
        assertThat(greenMail.getReceviedMessagesForDomain("html.org").length, equalTo(1));
    }

    @AfterClass
    public static void shutdown() {
        smtp.stop();
    }
}