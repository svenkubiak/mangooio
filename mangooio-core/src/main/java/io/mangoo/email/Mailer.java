package io.mangoo.email;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.exceptions.MangooMailerException;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Mailer {
    private DefaultAuthenticator defaultAuthenticator;
    private final String host;
    private final int port;
    private final boolean ssl;

    @Inject
    public Mailer(Config config) {
        this.host = config.getSmtpHost();
        this.port = config.getSmtpPort();
        this.ssl = config.isSmtpSSL();

        final String username = config.getSmtpUsername();
        final String password = config.getSmtpPassword();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            this.defaultAuthenticator = new DefaultAuthenticator(username, password);
        }
    }

    /**
     * Sends a plain text message using the SMTP configuration in application.conf
     *
     * For more information @see <a href="https://commons.apache.org/proper/commons-email/userguide.html">https://commons.apache.org/proper/commons-email/userguide.html</a>
     *
     * @param email The email to send
     * @throws MangooMailerException
     */
    public void send(Email email) throws MangooMailerException {
        Objects.requireNonNull(email, "email can not be null");

        email.setHostName(this.host);
        email.setSmtpPort(this.port);
        email.setAuthenticator(this.defaultAuthenticator);
        email.setSSLOnConnect(this.ssl);

        try {
            email.send();
        } catch (final EmailException e) {
            throw new MangooMailerException(e);
        }
    }

    /**
     * Sends a multi part email using the SMTP configuration in application.conf
     *
     * For more information @see <a href="https://commons.apache.org/proper/commons-email/userguide.html">https://commons.apache.org/proper/commons-email/userguide.html</a>
     *
     * @param multiPartEmail The multi part email to send
     * @throws MangooMailerException
     */
    public void send(MultiPartEmail multiPartEmail) throws MangooMailerException {
        Objects.requireNonNull(multiPartEmail, "email can not be null");

        multiPartEmail.setHostName(this.host);
        multiPartEmail.setSmtpPort(this.port);
        multiPartEmail.setAuthenticator(this.defaultAuthenticator);
        multiPartEmail.setSSLOnConnect(this.ssl);

        try {
            multiPartEmail.send();
        } catch (final EmailException e) {
            throw new MangooMailerException(e);
        }
    }

    /**
     * Sends a HTML email using the SMTP configuration in application.conf
     *
     * For more information @see <a href="https://commons.apache.org/proper/commons-email/userguide.html">https://commons.apache.org/proper/commons-email/userguide.html</a>
     *
     * @param htmlEmail The HTML email to send
     * @throws MangooMailerException
     */
    public void send(HtmlEmail htmlEmail) throws MangooMailerException {
        Objects.requireNonNull(htmlEmail, "htmlEmail can not be null");

        htmlEmail.setHostName(this.host);
        htmlEmail.setSmtpPort(this.port);
        htmlEmail.setAuthenticator(this.defaultAuthenticator);
        htmlEmail.setSSLOnConnect(this.ssl);

        try {
            htmlEmail.send();
        } catch (final EmailException e) {
            throw new MangooMailerException(e);
        }
    }
}