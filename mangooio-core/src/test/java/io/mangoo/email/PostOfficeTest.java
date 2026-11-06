package io.mangoo.email;

import io.mangoo.constants.Default;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostOfficeTest {

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
