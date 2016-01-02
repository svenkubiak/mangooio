package io.mangoo.routing.handlers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.exceptions.MangooRequestException;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.MangooRequestFilter;
import io.mangoo.routing.RequestAttachment;
import io.mangoo.routing.listeners.MetricsListener;
import io.mangoo.templating.TemplateEngine;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * Main class for dispatching a request to the request chain.
 * The request chain contains the following handlers in order:
 *
 * DispatcherHandler
 * LocalHandler
 * InCookiesHandler
 * FormHandler
 * RequestHandler
 * OutCookiesHandler
 * ResponseHandler
 *
 * @author svenkubiak
 *
 */
public class DispatcherHandler implements HttpHandler {
    private static final Logger LOG = LogManager.getLogger(DispatcherHandler.class);
    private Method method;
    private final TemplateEngine templateEngine;
    private final Messages messages;
    private final Crypto crypto;
    private final MetricsListener metricsListener;
    private final Map<String, Class<?>> methodParameters;
    private final Class<?> controllerClass;
    private final Config config;
    private final String controllerClassName;
    private final String controllerMethodName;
    private final int methodParametersCount;
    private final boolean metrics;
    private final boolean async;
    private final boolean hasRequestFilter;

    public DispatcherHandler(Class<?> controllerClass, String controllerMethod, boolean async) {
        Objects.requireNonNull(controllerClass, "controllerClass can not be null");
        Objects.requireNonNull(controllerMethod, "controllerMethod can not be null");

        this.templateEngine = Application.getInstance(TemplateEngine.class);
        this.messages = Application.getInstance(Messages.class);
        this.metricsListener = Application.getInstance(MetricsListener.class);
        this.crypto = Application.getInstance(Crypto.class);
        this.controllerClass = controllerClass;
        this.controllerMethodName = controllerMethod;
        this.controllerClassName = controllerClass.getSimpleName();
        this.methodParameters = getMethodParameters();
        this.methodParametersCount = this.methodParameters.size();
        this.config = Application.getInstance(Config.class);
        this.async = async;
        this.hasRequestFilter = Application.getInjector().getAllBindings().containsKey(com.google.inject.Key.get(MangooRequestFilter.class));
        this.metrics = Application.getInstance(Config.class).isAdminMetricsEnabled();

        try {
            this.method = Application.getInstance(this.controllerClass)
                    .getClass()
                    .getMethod(this.controllerMethodName, this.methodParameters.values().toArray(new Class[0]));
        } catch (NoSuchMethodException | SecurityException e) {
            LOG.error("Failed to create DispatcherHandler", e);
        }
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if ( (RequestUtils.isPostOrPut(exchange) || this.async) && exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        if (this.metrics) {
            exchange.addResponseCommitListener(this.metricsListener);
        }

        final RequestAttachment requestAttachment = RequestAttachment.build()
            .withControllerInstance(Application.getInstance(this.controllerClass))
            .withControllerClass(this.controllerClass)
            .withControllerClassName(this.controllerClassName)
            .withControllerMethodName(this.controllerMethodName)
            .withMethodParameters(this.methodParameters)
            .withMethod(this.method)
            .withMethodParameterCount(this.methodParametersCount)
            .withRequestFilter(this.hasRequestFilter)
            .withRequestParameter(RequestUtils.getRequestParameters(exchange))
            .withMessages(this.messages)
            .withTemplateEngine(this.templateEngine)
            .withCrypto(this.crypto)
            .withConfig(this.config);

        exchange.putAttachment(RequestUtils.REQUEST_ATTACHMENT, requestAttachment);
        nextHandler(exchange);
    }

    /**
     * Converts the method parameter of a mapped controller method to a map
     *
     * @return A Map containing the declared methods of the method parameters and their class type
     */
    private Map<String, Class<?>> getMethodParameters() {
        final Map<String, Class<?>> parameters = new LinkedHashMap<>(); //NOSONAR
        for (final Method declaredMethod : this.controllerClass.getDeclaredMethods()) {
            if (declaredMethod.getName().equals(this.controllerMethodName) && declaredMethod.getParameterCount() > 0) {
                Arrays.asList(declaredMethod.getParameters()).forEach(parameter -> parameters.put(parameter.getName(), parameter.getType())); //NOSONAR
                break;
            }
        }

        return parameters;
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    private void nextHandler(HttpServerExchange exchange) throws MangooRequestException {
        try {
            new LocaleHandler().handleRequest(exchange);
        } catch (final Exception e) {
            throw new MangooRequestException(e);
        }
    }
}