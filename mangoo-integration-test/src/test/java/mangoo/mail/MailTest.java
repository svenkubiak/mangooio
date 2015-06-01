package mangoo.mail;

import static org.junit.Assert.assertEquals;
import mangoo.io.core.Application;
import mangoo.io.mail.Mailer;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Resources;
import com.icegreen.greenmail.util.GreenMail;

public class MailTest {
    private static Mailer mailer;
    private static GreenMail fakeSMTP;
    
    @Before
    public void init() {
        mailer = Application.getInjector().getInstance(Mailer.class);
        fakeSMTP = Application.getFakeSMTP();
    }
    
    @Test
    public void textTest() throws Exception {
        assertEquals(0, fakeSMTP.getReceviedMessagesForDomain("text.com").length);
        
        Email email = new SimpleEmail();
        email.setFrom("user@test.com");
        email.setSubject("plainTextTest");
        email.setMsg("This is a test plan text message");
        email.addTo("foo@text.com");
        
        mailer.send(email);
        
        assertEquals(1, fakeSMTP.getReceviedMessagesForDomain("text.com").length);
    }
    
    @Test
    public void multipartTest() throws Exception {
        assertEquals(0, fakeSMTP.getReceviedMessagesForDomain("multipart.com").length);
        
        EmailAttachment attachment = new EmailAttachment();
        attachment.setPath(Resources.getResource("attachment.txt").getPath());
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setDescription("Picture of John");
        attachment.setName("John");

        MultiPartEmail email = new MultiPartEmail();
        email.addTo("jdoe@multipart.com", "John Doe");
        email.setFrom("me@apache.org", "Me");
        email.setSubject("The picture");
        email.setMsg("Here is the picture you wanted");

        email.attach(attachment);
        
        mailer.send(email);
        
        assertEquals(1, fakeSMTP.getReceviedMessagesForDomain("multipart.com").length);
    }
    
    @Test
    public void htmlTest() throws Exception {
        assertEquals(0, fakeSMTP.getReceviedMessagesForDomain("html.org").length);
        
        HtmlEmail email = new HtmlEmail();
        email.setHostName("mail.myserver.com");
        email.addTo("jdoe@html.org", "John Doe");
        email.setFrom("me@apache.org", "Me");
        email.setSubject("Test email with inline image");
        
        email.setHtmlMsg("<html>The apache logo - </html>");
        email.setTextMsg("Your email client does not support HTML messages");
        
        mailer.send(email);
        
        assertEquals(1, fakeSMTP.getReceviedMessagesForDomain("html.org").length);
    }
}