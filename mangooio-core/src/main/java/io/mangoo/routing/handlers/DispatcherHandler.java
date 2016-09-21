package io.mangoo.routing.handlers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.annotations.FilterWith;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.MangooRequestFilter;
import io.mangoo.routing.Attachment;
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
 * LimitHandler
 * LocalHandler
 * InboundCookiesHandler
 * FormHandler
 * RequestHandler
 * OutboundCookiesHandler
 * ResponseHandler
 *
 * @author svenkubiak
 *
 */
public class DispatcherHandler implements HttpHandler {
    private static final Config CONFIG = Application.getConfig();
    private static final Logger LOG = LogManager.getLogger(DispatcherHandler.class);
    private Method method;
    private List<Annotation> methodAnnotations = new ArrayList<>();
    private List<Annotation> classAnnotations = new ArrayList<>();
    private final TemplateEngine templateEngine;
    private final Messages messages;
    private final Crypto crypto;
    private final Map<String, Class<?>> methodParameters;
    private final Class<?> controllerClass;
    private final String controllerClassName;
    private final String controllerMethodName;
    private final boolean hasRequestFilter;
    private String username;
    private String password;    
    private int limit;
    private final int methodParametersCount;
    private boolean blocking;
    private boolean internalTemplateEngine;
    private boolean timer;

    public DispatcherHandler(Class<?> controllerClass, String controllerMethod) {
        Objects.requireNonNull(controllerClass, "controllerClass can not be null");
        Objects.requireNonNull(controllerMethod, "controllerMethod can not be null");

        this.templateEngine = this.internalTemplateEngine ? Application.getInternalTemplateEngine() : Application.getInstance(TemplateEngine.class);
        this.messages = Application.getInstance(Messages.class);
        this.crypto = Application.getInstance(Crypto.class);
        this.controllerClass = controllerClass;
        this.controllerMethodName = controllerMethod;
        this.controllerClassName = controllerClass.getSimpleName();
        this.methodParameters = getMethodParameters();
        this.methodParametersCount = this.methodParameters.size();
        this.hasRequestFilter = Application.getInjector().getAllBindings().containsKey(com.google.inject.Key.get(MangooRequestFilter.class));

        try {
            this.method = Application.getInstance(this.controllerClass)
                    .getClass()
                    .getMethod(this.controllerMethodName, this.methodParameters.values().toArray(new Class[0]));
            
            for (Annotation annotation : this.method.getAnnotations()) {
                if (annotation.annotationType().equals(FilterWith.class)) {
                    this.methodAnnotations.add(annotation);
                }
            }
        } catch (NoSuchMethodException | SecurityException e) {
            LOG.error("Failed to create DispatcherHandler", e);
        }
        
        for (Annotation annotation : controllerClass.getAnnotations()) {
            if (annotation.annotationType().equals(FilterWith.class)) {
                this.classAnnotations.add(annotation);
            }
        }
    }
    
    public DispatcherHandler isBlocking(boolean blocking) {
        this.blocking = blocking;
        return this;
    }

    public DispatcherHandler withInternalTemplateEngine(boolean internalTemplateEngine) {
        this.internalTemplateEngine = internalTemplateEngine;
        return this;
    }
    
    public DispatcherHandler withTimer(boolean timer) {
        this.timer = timer;
        return this;
    }
    
    public DispatcherHandler withUsername(String username) {
        this.username = username;
        return this;
    }
    
    public DispatcherHandler withPassword(String password) {
        this.password = password;
        return this;
    }
    
    public DispatcherHandler withLimit(int limit) {
        this.limit = limit;
        return this;
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if ( (RequestUtils.isPostOrPut(exchange) || this.blocking) && exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        if (CONFIG.isAdminEnabled()) {
            exchange.addExchangeCompleteListener(new MetricsListener(System.currentTimeMillis()));
        }

        final Attachment attachment = Attachment.build()
            .withControllerInstance(Application.getInstance(this.controllerClass))
            .withControllerClass(this.controllerClass)
            .withControllerClassName(this.controllerClassName)
            .withControllerMethodName(this.controllerMethodName)
            .withClassAnnotations(this.classAnnotations)
            .withMethodAnnotations(this.methodAnnotations)
            .withMethodParameters(this.methodParameters)
            .withMethod(this.method)
            .withMethodParameterCount(this.methodParametersCount)
            .withRequestFilter(this.hasRequestFilter)
            .withRequestParameter(RequestUtils.getRequestParameters(exchange))
            .withMessages(this.messages)
            .withTimer(this.timer)
            .withLimit(this.limit)
            .withUsername(this.username)
            .withPassword(this.password)
            .withTemplateEngine(this.templateEngine)
            .withCrypto(this.crypto);

        exchange.putAttachment(RequestUtils.ATTACHMENT_KEY, attachment);
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
    @SuppressWarnings("all")
    private void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(LimitHandler.class).handleRequest(exchange);
    }
}