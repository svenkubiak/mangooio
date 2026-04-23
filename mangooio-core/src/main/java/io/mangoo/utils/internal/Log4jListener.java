package io.mangoo.utils.internal;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.status.StatusData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Log4jListener implements org.apache.logging.log4j.status.StatusListener {
    private final List<StatusData> events = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void log(StatusData data) {
        if (data.getLevel().isMoreSpecificThan(Level.WARN)) {
            events.add(data);
        }
    }

    @Override
    public Level getStatusLevel() {
        return Level.WARN;
    }

    public void validateAndCollect() {
        var ctx      = (LoggerContext) LogManager.getContext(false);
        var config   = ctx.getConfiguration();
        var appenders = config.getAppenders();

        appenders.forEach((name, appender) -> {
            if (!appender.isStarted())
                addSynthetic(Level.ERROR, "Appender '" + name + "' is not started");
        });

        config.getLoggers().forEach((name, lc) -> {
            lc.getAppenderRefs().forEach(ref -> {
                if (!appenders.containsKey(ref.getRef()))
                    addSynthetic(Level.ERROR, "Logger '" + name + "' references unknown appender '" + ref.getRef() + "'");
            });
        });
    }

    private void addSynthetic(Level level, String message) {
        events.add(new StatusData(null, level,
                new SimpleMessage(message), null, null));
    }

    public List<StatusData> getEvents() {
        return List.copyOf(events);
    }

    public boolean hasErrors() {
        return events.stream()
                .anyMatch(e -> e.getLevel().isMoreSpecificThan(Level.ERROR));
    }

    public void clear() {
        events.clear();
    }

    @Override
    public void close() throws IOException {
    }
}