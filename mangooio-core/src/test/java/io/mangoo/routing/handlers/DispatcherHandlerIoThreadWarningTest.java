package io.mangoo.routing.handlers;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DispatcherHandlerIoThreadWarningTest {

    @Test
    void testSlowControllerIsWarned() {
        var events = capture(TimeUnit.MILLISECONDS.toNanos(1500), 1000);

        assertEquals(1, events.size());
        assertEquals(Level.WARN, events.getFirst().getLevel());
        assertEquals("GET ExportController.export ran 1500 ms on I/O thread XNIO-1 I/O-3; mark the route withBlocking() if its controller waits on I/O",
                events.getFirst().getMessage().getFormattedMessage());
    }

    @Test
    void testControllerAtTheThresholdIsWarned() {
        assertEquals(1, capture(TimeUnit.MILLISECONDS.toNanos(1000), 1000).size());
    }

    @Test
    void testFastControllerIsNotWarned() {
        assertTrue(capture(TimeUnit.MILLISECONDS.toNanos(999), 1000).isEmpty());
    }

    @Test
    void testZeroDisablesTheWarning() {
        assertTrue(capture(TimeUnit.SECONDS.toNanos(30), 0).isEmpty());
    }

    private static List<LogEvent> capture(long elapsedNanos, long warnAfterMillis) {
        var logger = (org.apache.logging.log4j.core.Logger) LogManager.getLogger(DispatcherHandlerIoThreadWarningTest.class);
        Level previous = logger.getLevel();
        var appender = new CapturingAppender();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
        try {
            DispatcherHandler.warnIfSlowOnIoThread(logger, elapsedNanos, warnAfterMillis,
                    "GET", "ExportController.export", "XNIO-1 I/O-3");
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
            super("iothread-warning-test", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }
    }
}
