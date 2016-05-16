package io.mangoo.mail;

import static org.junit.Assert.assertEquals;

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

public class MailTest {
    private static Mailer mailer;
    private static GreenMail greenMail;
    private static FakeSMTP fakeSMTP;

    @BeforeClass
    public static void init() {
        mailer = Application.getInstance(Mailer.class);
        fakeSMTP = Application.getInstance(FakeSMTP.class);
        fakeSMTP.start();
        greenMail = fakeSMTP.getGreenMail();
    }

    @Test
    public void textTest() throws Exception {
        assertEquals(0, greenMail.getReceviedMessagesForDomain("text.com").length);

        final Email email = new SimpleEmail();
        email.setFrom("user@test.com");
        email.setSubject("plainTextTest");
        email.setMsg("This is a test plan text message");
        email.addTo("foo@text.com");

        mailer.send(email);

        assertEquals(1, greenMail.getReceviedMessagesForDomain("text.com").length);
    }

    @Test
    public void multipartTest() throws Exception {
        assertEquals(0, greenMail.getReceviedMessagesForDomain("multipart.com").length);

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

        mailer.send(email);

        assertEquals(1, greenMail.getReceviedMessagesForDomain("multipart.com").length);
    }

    @Test
    public void htmlTest() throws Exception {
        assertEquals(0, greenMail.getReceviedMessagesForDomain("html.org").length);

        final HtmlEmail email = new HtmlEmail();
        email.setHostName("mail.myserver.com");
        email.addTo("jdoe@html.org", "John Doe");
        email.setFrom("me@apache.org", "Me");
        email.setSubject("Test email with inline image");

        email.setHtmlMsg("<html>The apache logo - </html>");
        email.setTextMsg("Your email client does not support HTML messages");

        mailer.send(email);

        assertEquals(1, greenMail.getReceviedMessagesForDomain("html.org").length);
    }

    @AfterClass
    public static void shutdown() {
        fakeSMTP.stop();
    }
}