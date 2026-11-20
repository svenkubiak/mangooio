package io.mangoo.utils;

import com.nimbusds.jose.JOSEException;
import io.mangoo.exceptions.MangooJwtException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtUtilsLogTest {

    @Test
    void testExpectedRejectionIsDebugWithoutStack() {
        var events = capture(() -> JwtUtils.logRejection(
                LogManager.getLogger(JwtUtilsLogTest.class),
                "Authorization",
                new MangooJwtException(new JOSEException("Token expired"))));

        assertEquals(1, events.size());
        assertEquals(Level.DEBUG, events.getFirst().getLevel());
        assertEquals("Authorization rejected: Token expired", events.getFirst().getMessage().getFormattedMessage());
        assertNull(events.getFirst().getThrown());
    }

    @Test
    void testClockSkewRejectionsAreDebug() {
        for (String reason : List.of("Token not yet valid", "Token issued in the future")) {
            var events = capture(() -> JwtUtils.logRejection(
                    LogManager.getLogger(JwtUtilsLogTest.class),
                    "Flash cookie",
                    new MangooJwtException(new JOSEException(reason))));

            assertEquals(1, events.size());
            assertEquals(Level.DEBUG, events.getFirst().getLevel());
            assertEquals("Flash cookie rejected: " + reason, events.getFirst().getMessage().getFormattedMessage());
            assertNull(events.getFirst().getThrown());
        }
    }

    @Test
    void testUnexpectedRejectionIsWarnWithoutStack() {
        var events = capture(() -> JwtUtils.logRejection(
                LogManager.getLogger(JwtUtilsLogTest.class),
                "Authentication cookie",
                new MangooJwtException(new JOSEException("Issuer mismatch"))));

        assertEquals(1, events.size());
        assertEquals(Level.WARN, events.getFirst().getLevel());
        assertEquals("Failed to parse authentication cookie: Issuer mismatch", events.getFirst().getMessage().getFormattedMessage());
        assertNull(events.getFirst().getThrown());
    }

    @Test
    void testParseExceptionIsWarnWithoutStack() {
        var events = capture(() -> JwtUtils.logRejection(
                LogManager.getLogger(JwtUtilsLogTest.class),
                "Admin cookie",
                new ParseException("Invalid JWT serialization", 0)));

        assertEquals(1, events.size());
        assertEquals(Level.WARN, events.getFirst().getLevel());
        assertEquals("Failed to parse admin cookie: Invalid JWT serialization", events.getFirst().getMessage().getFormattedMessage());
        assertNull(events.getFirst().getThrown());
    }

    private static List<LogEvent> capture(Runnable action) {
        var logger = (org.apache.logging.log4j.core.Logger) LogManager.getLogger(JwtUtilsLogTest.class);
        Level previous = logger.getLevel();
        var appender = new CapturingAppender();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
        try {
            action.run();
            return List.copyOf(appender.events);
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(previous);
            appender.stop();
        }
    }

    private static final class CapturingAppender extends AbstractAppender {
        private final List<LogEvent> events = new ArrayList<>();

        private CapturingAppender() {
            super("jwt-log-test", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }
    }
}
