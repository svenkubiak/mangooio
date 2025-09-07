package io.mangoo.routing.handlers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.filters.OncePerRequestFilter;
import io.mangoo.routing.Attachment;
import io.mangoo.templating.TemplateEngine;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

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
    private int limit;
    private int methodParametersCount;
    private boolean requestFilter;
    private boolean blocking;
    private boolean authentication;

    public DispatcherHandler dispatch(Class<?> controllerClass, String controllerMethodName) {
        Objects.requireNonNull(controllerClass, Required.CONTROLLER_CLASS);
        Objects.requireNonNull(controllerMethodName, Required.CONTROLLER_METHOD);

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

    public DispatcherHandler withAuthentication(boolean authentication) {
        this.authentication = authentication;
        return this;
    }

    public DispatcherHandler withLimit(int limit) {
        this.limit = limit;
        return this;
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if ( (RequestUtils.isPostPutPatch(exchange) || blocking) && exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        final var attachment = Attachment.build()
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
            .withMessages(messages)
            .withLimit(limit)
            .withAuthentication(authentication)
            .withTemplateEngine(templateEngine);

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

        var targetMethod = Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(m -> m.getName().equals(controllerMethodName) && m.getParameterCount() > 0)
                .findFirst()
                .orElse(null);

        if (targetMethod != null) {
            for (Parameter parameter : targetMethod.getParameters()) {
                parameters.put(parameter.getName(), parameter.getType());
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
        Application.getInstance(LocaleHandler.class).handleRequest(exchange);
    }
}