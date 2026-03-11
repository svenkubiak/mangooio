package io.mangoo.routing.handlers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.filters.OncePerRequestFilter;
import io.mangoo.routing.Attachment;
import io.mangoo.templating.TemplateEngine;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.internal.Trace;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public final class DispatcherHandler implements HttpHandler {
    private static final int INITIAL_CAPACITY = 1000;
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

        Map<String, Class<?>> parameters = new LinkedHashMap<>(INITIAL_CAPACITY);
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
        if ((RequestUtils.isPostPutPatch(exchange) || blocking) && exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        Trace.start(exchange.getRequestPath());
        Attachment attachment = Attachment.build()
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
                .withAuthentication(authentication)
                .withTemplateEngine(templateEngine);

        exchange.putAttachment(RequestUtils.getAttachmentKey(), attachment);
        nextHandler(exchange);
    }

    private void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(LocaleHandler.class).handleRequest(exchange);
    }
}