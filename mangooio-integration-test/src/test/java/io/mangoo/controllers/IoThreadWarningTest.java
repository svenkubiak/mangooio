package io.mangoo.controllers;

import io.mangoo.TestExtension;
import io.mangoo.routing.handlers.DispatcherHandler;
import io.mangoo.test.http.TestRequest;
import io.undertow.util.StatusCodes;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({TestExtension.class})
class IoThreadWarningTest {
    private static final String CONTROLLER = "ThreadController.slow";

    @Test
    void testSlowControllerOnTheIoThreadIsWarned() throws InterruptedException {
        var events = capture("/thread/slow");

        assertEquals(1, events.size());
        assertEquals(Level.WARN, events.getFirst().getLevel());
        assertThat(events.getFirst().getMessage().getFormattedMessage(), containsString("GET " + CONTROLLER + " ran "));
        assertThat(events.getFirst().getMessage().getFormattedMessage(), containsString(" on I/O thread "));
    }

    @Test
    void testSlowControllerOnAWorkerIsNotWarned() throws InterruptedException {
        assertTrue(capture("/thread/slow-worker").isEmpty());
    }

    // The warning is written after the response is sent, so it can arrive after the client has the response.
    private static List<LogEvent> capture(String path) throws InterruptedException {
        var logger = (org.apache.logging.log4j.core.Logger) LogManager.getLogger(DispatcherHandler.class);
        var appender = new CapturingAppender();
        appender.start();
        logger.addAppender(appender);
        try {
            var response = TestRequest.get(path).execute();
            assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
            for (int i = 0; i < 40 && appender.events.isEmpty(); i++) {
                Thread.sleep(25);
            }
            return List.copyOf(appender.events);
        } finally {
            logger.removeAppender(appender);
            appender.stop();
        }
    }

    private static final class CapturingAppender extends AbstractAppender {
        private final List<LogEvent> events = new CopyOnWriteArrayList<>();

        private CapturingAppender() {
            super("iothread-warning-integration-test", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            if (event.getMessage().getFormattedMessage().contains(" " + CONTROLLER + " ")) {
                events.add(event.toImmutable());
            }
        }
    }
}
