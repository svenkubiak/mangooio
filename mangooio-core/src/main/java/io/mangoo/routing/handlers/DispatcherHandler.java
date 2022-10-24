package io.mangoo.routing.handlers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.annotations.FilterWith;
import io.mangoo.core.Application;
import io.mangoo.enums.Required;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.filters.OncePerRequestFilter;
import io.mangoo.routing.Attachment;
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
    private static final Logger LOG = LogManager.getLogger(DispatcherHandler.class);
    private Method method;
    private List<Annotation> methodAnnotations = new ArrayList<>();
    private List<Annotation> classAnnotations = new ArrayList<>();
    private TemplateEngine templateEngine;
    private Messages messages;
    private Map<String, Class<?>> methodParameters;
    private Class<?> controllerClass;
    private String controllerClassName;
    private String controllerMethodName;
    private String username;
    private String password;
    private int limit;
    private int methodParametersCount;
    private long maxEntitySize;
    private boolean requestFilter;
    private boolean blocking;
    private boolean authentication;

    public DispatcherHandler dispatch(Class<?> controllerClass, String controllerMethodName) {
        Objects.requireNonNull(controllerClass, Required.CONTROLLER_CLASS.toString());
        Objects.requireNonNull(controllerMethodName, Required.CONTROLLER_METHOD.toString());

        this.templateEngine = Application.getInstance(TemplateEngine.class);
        this.messages = Application.getInstance(Messages.class);
        this.controllerClass = controllerClass;
        this.controllerMethodName = controllerMethodName;
        this.controllerClassName = controllerClass.getSimpleName();
        this.methodParameters = getMethodParameters();
        this.methodParametersCount = this.methodParameters.size();
        this.requestFilter = Application.getInjector().getAllBindings().containsKey(com.google.inject.Key.get(OncePerRequestFilter.class));

        try {
            this.method = Application.getInstance(this.controllerClass)
                    .getClass()
                    .getDeclaredMethod(this.controllerMethodName, this.methodParameters.values().toArray(new Class[0]));
            
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
        
        return this;
    }
    
    public DispatcherHandler isBlocking(boolean blocking) {
        this.blocking = blocking;
        return this;
    }
    
    public DispatcherHandler withBasicAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
        
        return this;
    }

    public DispatcherHandler withAuthentication(boolean authentication) {
        this.authentication = authentication;
        return this;
    }

    public DispatcherHandler withMaxEntitySize(long maxEntitySize) {
        this.maxEntitySize = maxEntitySize;
        return this;
    }
    
    public DispatcherHandler withLimit(int limit) {
        this.limit = limit;
        return this;
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (this.maxEntitySize > 0) {
            exchange.setMaxEntitySize(maxEntitySize);
        }
        
        if ( (RequestUtils.isPostPutPatch(exchange) || this.blocking) && exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        final var attachment = Attachment.build()
            .withControllerInstance(Application.getInstance(this.controllerClass))
            .withControllerClass(this.controllerClass)
            .withControllerClassName(this.controllerClassName)
            .withControllerMethodName(this.controllerMethodName)
            .withClassAnnotations(this.classAnnotations)
            .withMethodAnnotations(this.methodAnnotations)
            .withMethodParameters(this.methodParameters)
            .withMethod(this.method)
            .withMethodParameterCount(this.methodParametersCount)
            .withRequestFilter(this.requestFilter)
            .withRequestParameter(RequestUtils.getRequestParameters(exchange))
            .withMessages(this.messages)
            .withLimit(this.limit)
            .withAuthentication(this.authentication)
            .withBasicAuthentication(this.username, this.password)
            .withTemplateEngine(this.templateEngine);

        exchange.putAttachment(RequestUtils.getAttachmentKey(), attachment);
        nextHandler(exchange);
    }

    /**
     * Converts the method parameter of a mapped controller method to a map
     *
     * @return A Map containing the declared methods of the method parameters and their class type
     */
    private Map<String, Class<?>> getMethodParameters() {
        final Map<String, Class<?>> parameters = new LinkedHashMap<>();
        for (final Method declaredMethod : this.controllerClass.getDeclaredMethods()) {
            if (declaredMethod.getName().equals(this.controllerMethodName) && declaredMethod.getParameterCount() > 0) {
                Arrays.stream(declaredMethod.getParameters()).forEach(parameter -> parameters.put(parameter.getName(), parameter.getType()));
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
    private void nextHandler(HttpServerExchange exchange) throws Exception {
        if (StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.password)) {
            HttpHandler httpHandler = RequestUtils.wrapBasicAuthentication(
                    Application.getInstance(LocaleHandler.class),
                    this.username,
                    this.password);
            
            httpHandler.handleRequest(exchange);
        } else {
            Application.getInstance(LocaleHandler.class).handleRequest(exchange);    
        }
    }
}