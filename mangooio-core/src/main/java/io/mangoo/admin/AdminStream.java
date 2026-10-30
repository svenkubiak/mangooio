package io.mangoo.admin;

import com.nimbusds.jwt.JWTClaimsSet;
import io.mangoo.constants.Required;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.routing.handlers.ServerSentEventHandler;
import io.mangoo.templating.TemplateContext;
import io.mangoo.templating.TemplateEngine;
import io.mangoo.utils.internal.MangooUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Cookies;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Pushes the sections of the dashboard every five seconds to the open dashboards of this instance as server sent events,
 * one event per section. The stream is only opened for a valid admin cookie and closed once the cookie expires, so the
 * page reconnects, gets a 401 and shows the login.
 */
@Singleton
public class AdminStream {
    private static final Logger LOG = LogManager.getLogger(AdminStream.class);
    private static final AttachmentKey<Instant> EXPIRES = AttachmentKey.create(Instant.class);
    private static final List<String> SECTIONS = List.of("instances", "instance", "events");
    private static final List<String> METRIC_SECTIONS = List.of("requests", "status");
    private static final Pattern LINE_BREAKS = Pattern.compile("\\s*\\R\\s*");
    private static final String TEMPLATE = "@admin/stream.ftl";
    private static final long INTERVAL_SECONDS = 5;
    private static final int MAX_COOKIES = 200;
    private final Dashboard dashboard;
    private final TemplateEngine templateEngine;
    private final Config config;
    private final io.undertow.server.handlers.sse.ServerSentEventHandler events;
    private ScheduledExecutorService executor;

    @Inject
    public AdminStream(Dashboard dashboard, TemplateEngine templateEngine, Config config) {
        this.dashboard = Objects.requireNonNull(dashboard, "dashboard can not be null");
        this.templateEngine = Objects.requireNonNull(templateEngine, "templateEngine can not be null");
        this.config = Objects.requireNonNull(config, Required.CONFIG);
        this.events = ServerSentEventHandler.serverSentEvents(this::connected);
    }

    /** Answers 401 instead of opening the stream if the admin cookie is missing, invalid or waits for the second factor. */
    public HttpHandler handler() {
        HttpHandler stream = ServerSentEventHandler.withHeaders(events);

        return exchange -> {
            Cookie cookie = exchange.getRequestCookie(MangooUtils.getAdminCookieName());
            if (expiresAt(cookie == null ? null : cookie.getValue()).isEmpty()) {
                exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
                exchange.endExchange();
                return;
            }

            stream.handleRequest(exchange);
        };
    }

    public synchronized void start() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().daemon().name("mangooio-admin-stream").factory());
            executor.scheduleWithFixedDelay(this::push, INTERVAL_SECONDS, INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    public synchronized void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        events.getConnections().forEach(ServerSentEventConnection::shutdown);
    }

    // The handshake was checked by the handler; the expiry is kept to close the stream with the cookie
    private void connected(ServerSentEventConnection connection, String lastEventId) {
        Map<String, Cookie> cookies = Cookies.parseRequestCookies(MAX_COOKIES, false, connection.getRequestHeaders().get(Headers.COOKIE));
        Cookie cookie = cookies.get(MangooUtils.getAdminCookieName());
        Optional<Instant> expires = expiresAt(cookie == null ? null : cookie.getValue());
        if (expires.isEmpty()) {
            connection.shutdown();
            return;
        }

        connection.putAttachment(EXPIRES, expires.orElseThrow());
    }

    static Optional<Instant> expiresAt(String cookie) {
        return MangooUtils.parseAdminCookie(cookie)
                .filter(claims -> !MangooUtils.isTwoFactorPending(claims))
                .map(JWTClaimsSet::getExpirationTime)
                .map(Date::toInstant);
    }

    // An exception would cancel all further runs of the scheduled task
    private void push() {
        try {
            var connections = events.getConnections();
            if (connections.isEmpty()) {
                return;
            }

            Map<String, String> sections = render();
            var now = Instant.now();
            for (ServerSentEventConnection connection : connections) {
                Instant expires = connection.getAttachment(EXPIRES);
                if (expires == null || !expires.isAfter(now)) {
                    connection.shutdown();
                } else {
                    sections.forEach((name, html) -> connection.send(html, name, null, null));
                }
            }
        } catch (MangooTemplateEngineException | RuntimeException e) {
            LOG.error("Failed to push the admin dashboard", e);
        }
    }

    Map<String, String> render() throws MangooTemplateEngineException {
        Map<String, Object> content = dashboard.getContent();
        content.put("mangooioAdminLocale", config.getApplicationAdminLocale());

        List<String> names = config.isMetricsEnable()
                ? Stream.concat(SECTIONS.stream(), METRIC_SECTIONS.stream()).toList()
                : SECTIONS;

        Map<String, String> sections = new LinkedHashMap<>();
        for (String name : names) {
            content.put("section", name);
            String html = templateEngine.renderTemplate(new TemplateContext(content).withTemplatePath(TEMPLATE));
            // An event is one data line, the markup of a section does not depend on line breaks
            sections.put(name, LINE_BREAKS.matcher(html.strip()).replaceAll(" "));
        }

        return sections;
    }
}
