package io.mangoo.email;

import io.mangoo.constants.Default;
import jakarta.mail.MessagingException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.Test;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostOfficeTest {

    @Test
    void testSmtpsEnablesImplicitTls() {
        // when
        Properties properties = transport("smtps");

        // then
        assertEquals("true", properties.getProperty("mail.smtp.ssl.enable"));
        assertEquals("true", properties.getProperty("mail.smtp.ssl.checkserveridentity"));
        assertNull(properties.getProperty("mail.smtp.starttls.enable"));
        assertNull(properties.getProperty("mail.smtp.starttls.required"));
        assertTimeouts(properties);
    }

    @Test
    void testSmtptlsRequiresStartTls() {
        // when
        Properties properties = transport("SMTPTLS");

        // then
        assertEquals("true", properties.getProperty("mail.smtp.starttls.enable"));
        assertEquals("true", properties.getProperty("mail.smtp.starttls.required"));
        assertEquals("true", properties.getProperty("mail.smtp.ssl.checkserveridentity"));
        assertNull(properties.getProperty("mail.smtp.ssl.enable"));
        assertTimeouts(properties);
    }

    @Test
    void testSmtpStaysCleartext() {
        // when
        Properties properties = transport(" smtp ");

        // then
        assertNull(properties.getProperty("mail.smtp.ssl.enable"));
        assertNull(properties.getProperty("mail.smtp.starttls.enable"));
        assertNull(properties.getProperty("mail.smtp.starttls.required"));
        assertTimeouts(properties);
    }

    @Test
    void testUnknownProtocolIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> transport("starttls"));
    }

    @Test
    void testTimeoutsAreLoggedWhenTheTransportIsConfigured() {
        // when
        List<String> messages = capture(() -> transport("smtps"));

        // then
        assertTrue(messages.stream().anyMatch(message -> message.contains("connection timeout is " + Default.SMTP_CONNECTION_TIMEOUT + " ms")
                && message.contains("read timeout is " + Default.SMTP_READ_TIMEOUT + " ms")));
    }

    @Test
    void testATimedOutSendIsLoggedWithTheTimeouts() {
        // given
        var failure = new MessagingException("send failed");
        failure.setNextException(new SocketTimeoutException("Read timed out"));

        // when
        List<String> messages = capture(() -> PostOffice.logSendFailure(failure, 2500, 4000));

        // then
        assertTrue(messages.stream().anyMatch(message -> message.contains("connection timeout 2500 ms")
                && message.contains("read timeout 4000 ms")));
    }

    @Test
    void testASendFailureThatIsNotATimeoutKeepsTheGenericLog() {
        // when
        List<String> messages = capture(() -> PostOffice.logSendFailure(new MessagingException("rejected"), 2500, 4000));

        // then
        assertTrue(messages.stream().anyMatch("Failed to send mail"::equals));
        assertFalse(messages.stream().anyMatch(message -> message.contains("timed out")));
    }

    @Test
    void testTimeoutNestedAsCauseIsRecognized() {
        assertTrue(PostOffice.causedByTimeout(new MessagingException("send", new SocketTimeoutException("connect timed out"))));
        assertFalse(PostOffice.causedByTimeout(new MessagingException("rejected")));
    }

    @Test
    void testConfiguredTimeoutsAreAppliedAndLogged() {
        // when
        Properties properties = transport("smtps", 2500, 4000);
        List<String> messages = capture(() -> transport("smtps", 2500, 4000));

        // then
        assertEquals("2500", properties.getProperty("mail.smtp.connectiontimeout"));
        assertEquals("4000", properties.getProperty("mail.smtp.timeout"));
        assertTrue(messages.stream().anyMatch(message -> message.contains("connection timeout is 2500 ms")
                && message.contains("read timeout is 4000 ms")));
    }

    private static Properties transport(String protocol) {
        return transport(protocol, Default.SMTP_CONNECTION_TIMEOUT, Default.SMTP_READ_TIMEOUT);
    }

    private static Properties transport(String protocol, int connectionTimeout, int readTimeout) {
        var properties = new Properties();
        PostOffice.applyTransport(properties, protocol, connectionTimeout, readTimeout);
        return properties;
    }

    private static void assertTimeouts(Properties properties) {
        assertEquals(Integer.toString(Default.SMTP_CONNECTION_TIMEOUT), properties.getProperty("mail.smtp.connectiontimeout"));
        assertEquals(Integer.toString(Default.SMTP_READ_TIMEOUT), properties.getProperty("mail.smtp.timeout"));
    }

    private static List<String> capture(Runnable action) {
        var logger = (org.apache.logging.log4j.core.Logger) LogManager.getLogger(PostOffice.class);
        Level previous = logger.getLevel();
        var appender = new CapturingAppender();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);
        try {
            action.run();
            return List.copyOf(appender.messages);
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(previous);
            appender.stop();
        }
    }

    private static final class CapturingAppender extends AbstractAppender {
        private final List<String> messages = new ArrayList<>();

        private CapturingAppender() {
            super("postoffice-test", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }
    }
}
