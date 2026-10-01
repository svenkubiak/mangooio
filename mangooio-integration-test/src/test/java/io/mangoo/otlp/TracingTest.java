package io.mangoo.otlp;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Only runs in the tracing-tests surefire execution with -Dotlp.enable=true, otherwise every Trace method returns immediately and the tests pass vacuously.
 * Assertions are made on the Trace diagnostics logs, as span defects do not surface as failing requests.
 */
@ExtendWith({TestExtension.class})
@Execution(ExecutionMode.SAME_THREAD)
class TracingTest {
    private static final String[] IMBALANCE_MARKERS = {
            "already traced",           // start called twice for one exchange
            "none was open",            // end called more often than start
            "No parent span",           // child span without a parent
            "Failed to end span"        // span could not be closed
    };

    private static final String TRACE_LOGGER = "io.mangoo.utils.internal.Trace";

    private CapturingAppender appender;
    private Level previousLevel;

    @BeforeEach
    void attachAppender() {
        appender = new CapturingAppender();
        appender.start();

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        LoggerConfig traceLogger = context.getConfiguration().getLoggerConfig(TRACE_LOGGER);

        // getLoggerConfig falls back to the root config for undeclared loggers, and raising that to debug would flood the build log.
        assertEquals(TRACE_LOGGER, traceLogger.getName(),
                "log4j2-test.xml must declare a logger for " + TRACE_LOGGER);

        // Trace reports underflows on debug, which the test configuration does not emit, so the assertions would otherwise pass vacuously.
        previousLevel = traceLogger.getLevel();
        traceLogger.setLevel(Level.DEBUG);
        traceLogger.addAppender(appender, Level.DEBUG, null);
        context.updateLoggers();
    }

    @AfterEach
    void detachAppender() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        LoggerConfig traceLogger = context.getConfiguration().getLoggerConfig(TRACE_LOGGER);

        traceLogger.removeAppender(appender.getName());
        traceLogger.setLevel(previousLevel);
        context.updateLoggers();
        appender.stop();
    }

    @Test
    void tracingIsActuallyEnabled() {
        assertTrue(Application.getInstance(Config.class).isOtlpEnable(),
                "This test class is meaningless without tracing enabled, run it via the tracing-tests execution");
    }

    @Test
    void parallelRequestsOnTheSamePathDoNotInterfere() throws Exception {
        int requests = 400;
        var succeeded = new AtomicInteger();
        var errors = new CopyOnWriteArrayList<String>();

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (var i = 0; i < requests; i++) {
                futures.add(pool.submit(() -> {
                    try {
                        TestResponse response = TestRequest.get("/").execute();
                        if (response.getStatusCode() == StatusCodes.OK) {
                            succeeded.incrementAndGet();
                        } else {
                            errors.add("status " + response.getStatusCode());
                        }
                    } catch (Exception e) {
                        errors.add(e.getClass().getSimpleName() + ": " + e.getMessage());
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get(60, TimeUnit.SECONDS);
            }
        }

        assertEquals(requests, succeeded.get(), "All requests must succeed, got: " + errors);
        assertNoImbalance();
    }

    @Test
    void abortedRequestsDoNotLeaveSpansOpen() {
        // The AuthenticationHandler ends the exchange before the ResponseHandler, so the completion listener has to close the root span.
        for (var i = 0; i < 100; i++) {
            TestRequest.get("/authenticationrequired").execute();
        }

        assertNoImbalance();
    }

    @Test
    void notFoundRequestsAreNotTraced() {
        // The fallback handler runs outside the dispatcher, so no span is started and ending it must not produce a warning.
        for (var i = 0; i < 20; i++) {
            TestRequest.get("/this-route-does-not-exist").execute();
        }

        assertNoImbalance();
    }

    private void assertNoImbalance() {
        List<String> found = appender.messages().stream()
                .filter(message -> {
                    for (String marker : IMBALANCE_MARKERS) {
                        if (message.contains(marker)) {
                            return true;
                        }
                    }
                    return false;
                })
                .toList();

        assertTrue(found.isEmpty(), "Trace reported an inconsistent span state: " + found);
    }

    private static final class CapturingAppender extends AbstractAppender {
        private final List<String> messages = new CopyOnWriteArrayList<>();

        private CapturingAppender() {
            super("tracing-test-capture", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }

        private List<String> messages() {
            return messages;
        }
    }
}
