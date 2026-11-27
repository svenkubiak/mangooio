package io.mangoo.routing.handlers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.constants.Required;
import io.mangoo.constants.Template;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.filters.OncePerRequestFilter;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Request;
import io.mangoo.templating.TemplateEngine;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.internal.Trace;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public final class DispatcherHandler implements HttpHandler {
    private static final Logger LOG = LogManager.getLogger(DispatcherHandler.class);
    static final AttachmentKey<byte[]> RAW_BODY = AttachmentKey.create(byte[].class);
    private static final int INITIAL_CAPACITY = 50;
    private final Method method;
    private final List<Annotation> methodAnnotations;
    private final List<Annotation> classAnnotations;
    private final TemplateEngine templateEngine;
    private final Messages messages;
    private final Map<String, Class<?>> methodParameters;
    private final Class<?> controllerClass;
    private final String controllerClassName;
    private final String controllerMethodName;
    private final int methodParametersCount;
    private final boolean requestFilter;
    private final boolean blocking;
    private final boolean authentication;
    private final boolean parameterStrict;
    private final long ioThreadWarnAfterMillis;
    private final HttpHandler dispatchHandler = ExceptionHandler.wrap(this);

    public DispatcherHandler(Class<?> controllerClass, String controllerMethodName, boolean blocking, boolean authentication) {
        Objects.requireNonNull(controllerClass, Required.CONTROLLER_CLASS);
        Objects.requireNonNull(controllerMethodName, Required.CONTROLLER_METHOD);

        this.controllerClass = controllerClass;
        this.controllerMethodName = controllerMethodName;
        this.controllerClassName = controllerClass.getSimpleName();
        this.blocking = blocking;
        this.authentication = authentication;
        this.templateEngine = Application.getInstance(TemplateEngine.class);
        this.messages = Application.getInstance(Messages.class);
        var config = Application.getInstance(Config.class);
        this.parameterStrict = config.isParameterStrict();
        this.ioThreadWarnAfterMillis = config.getApplicationIoThreadWarnAfter();
        this.requestFilter =
                Application.getInjector()
                        .getAllBindings()
                        .containsKey(com.google.inject.Key.get(OncePerRequestFilter.class));

        Method resolvedMethod = Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(m -> m.getName().equals(controllerMethodName))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("Controller method not found: "
                                + controllerClass.getName() + "." + controllerMethodName)
                );

        this.method = resolvedMethod;

        Map<String, Class<?>> parameters = LinkedHashMap.newLinkedHashMap(INITIAL_CAPACITY);
        for (Parameter parameter : resolvedMethod.getParameters()) {
            parameters.put(parameter.getName(), parameter.getType());
        }

        this.methodParameters = Collections.unmodifiableMap(parameters);
        this.methodParametersCount = parameters.size();

        List<Annotation> methodAnn = new ArrayList<>();
        for (Annotation annotation : resolvedMethod.getAnnotations()) {
            if (annotation.annotationType().equals(FilterWith.class)) {
                methodAnn.add(annotation);
            }
        }
        this.methodAnnotations = Collections.unmodifiableList(methodAnn);

        List<Annotation> classAnn = new ArrayList<>();
        for (Annotation annotation : controllerClass.getAnnotations()) {
            if (annotation.annotationType().equals(FilterWith.class)) {
                classAnn.add(annotation);
            }
        }
        this.classAnnotations = Collections.unmodifiableList(classAnn);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread() && leavesIoThread(exchange, blocking)) {
            exchange.dispatch(dispatchHandler);
            return;
        }

        // readAllBytes() is refused on an I/O thread. The bytes are delivered on this thread once they have arrived.
        if (exchange.isInIoThread() && RequestUtils.isSmallJsonRequest(exchange) && exchange.getAttachment(RAW_BODY) == null) {
            exchange.getRequestReceiver().receiveFullBytes(this::continueWithJson, this::rejectJson);
            return;
        }

        if (RequestUtils.hasMultipleParameterValues(exchange)) {
            endRequest(exchange);
            return;
        }

        if (parameterStrict && RequestUtils.hasAmbiguousParameters(exchange)) {
            endRequest(exchange);
            return;
        }

        Trace.start(exchange, exchange.getRequestPath());
        var attachment = Attachment.build()
                .withControllerInstance(Application.getInstance(controllerClass))
                .withControllerClass(controllerClass)
                .withControllerClassName(controllerClassName)
                .withControllerMethodName(controllerMethodName)
                .withClassAnnotations(classAnnotations)
                .withMethodAnnotations(methodAnnotations)
                .withMethodParameters(methodParameters)
                .withMethod(method)
                .withMethodParameterCount(methodParametersCount)
                .withRequestFilter(requestFilter)
                .withRequestParameter(RequestUtils.getRequestParameters(exchange))
                .withPathParameter(RequestUtils.getPathParameters(exchange))
                .withQueryParameter(RequestUtils.getQueryParameters(exchange))
                .withMessages(messages)
                .withAuthentication(authentication)
                .withReadsAuthenticationCookie(readsAuthenticationCookie(
                        authentication,
                        requestFilter,
                        !classAnnotations.isEmpty() || !methodAnnotations.isEmpty(),
                        methodParameters.values()))
                .withTemplateEngine(templateEngine);

        exchange.putAttachment(RequestUtils.getAttachmentKey(), attachment);

        if (ioThreadWarnAfterMillis == 0 || !exchange.isInIoThread()) {
            nextHandler(exchange);
            return;
        }

        long start = System.nanoTime();
        try {
            nextHandler(exchange);
        } finally {
            warnIfSlowOnIoThread(LOG, System.nanoTime() - start, ioThreadWarnAfterMillis,
                    exchange.getRequestMethod().toString(), controllerClassName + "." + controllerMethodName,
                    Thread.currentThread().getName());
        }
    }

    // Logs the controller, not the request path: a path can carry identifiers.
    static void warnIfSlowOnIoThread(Logger log, long elapsedNanos, long warnAfterMillis, String method, String controller, String thread) {
        long elapsedMillis = elapsedNanos / 1_000_000;
        if (warnAfterMillis > 0 && elapsedMillis >= warnAfterMillis) {
            log.warn("{} {} ran {} ms on I/O thread {}; mark the route withBlocking() if its controller waits on I/O",
                    method, controller, elapsedMillis, thread);
        }
    }

    private void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(LocaleHandler.class).handleRequest(exchange);
    }

    /**
     * Whether the request moves to a worker before its body is read.
     * Forms and every other POST, PUT or PATCH move right away.
     * A JSON body of at most {@link io.mangoo.constants.Default#SMALL_JSON_MAX_BYTES} is read on the I/O thread first
     * and then moves, unless the route called {@code withBlocking()}.
     */
    static boolean leavesIoThread(boolean blocking, boolean postPutPatch, boolean form, boolean smallJson) {
        if (blocking) {
            return true;
        }
        if (!postPutPatch) {
            return false;
        }
        if (form) {
            return true;
        }
        return !smallJson;
    }

    private void continueWithJson(HttpServerExchange exchange, byte[] data) {
        exchange.putAttachment(RAW_BODY, data == null ? new byte[0] : data);
        // A controller that blocks on a call needing a callback into this server deadlocks when the
        // callback's connection is bound to this I/O thread.
        exchange.dispatch(dispatchHandler);
    }

    private void rejectJson(HttpServerExchange exchange, Exception exception) {
        LOG.warn("Failed to read JSON body of {} {}", exchange.getRequestMethod(), exchange.getRequestURI(), exception);
        exchange.setStatusCode(StatusCodes.BAD_REQUEST);
        exchange.endExchange();
    }

    private static boolean leavesIoThread(HttpServerExchange exchange, boolean blocking) {
        boolean postPutPatch = RequestUtils.isPostPutPatch(exchange);
        boolean form = postPutPatch && RequestUtils.isFormRequest(exchange);
        boolean smallJson = postPutPatch && RequestUtils.isSmallJsonRequest(exchange);
        return leavesIoThread(blocking, postPutPatch, form, smallJson);
    }

    // Request.getAuthentication() is available on every route that receives Request, Authentication, or a filter.
    static boolean readsAuthenticationCookie(boolean routeAuthentication, boolean requestFilter, boolean hasFilter, Collection<Class<?>> parameterTypes) {
        if (routeAuthentication || requestFilter || hasFilter) {
            return true;
        }

        for (Class<?> type : parameterTypes) {
            if (type == Authentication.class || type == Request.class) {
                return true;
            }
        }

        return false;
    }

    // Rejects requests with ambiguous multi-valued parameters (HTTP parameter pollution).
    private void endRequest(HttpServerExchange exchange) {
        exchange.setStatusCode(StatusCodes.BAD_REQUEST);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html; charset=utf-8");
        exchange.getResponseSender().send(Template.badRequest());
    }
}