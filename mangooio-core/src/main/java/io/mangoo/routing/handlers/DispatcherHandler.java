package io.mangoo.routing.handlers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.interfaces.MangooRequestFilter;
import io.mangoo.routing.listeners.MetricsListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings("all")
public class DispatcherHandler implements HttpHandler {
    private Map<String, Class<?>> methodParameters;
    private Class<?> controllerInstance;
    private Class<?> controllerClass;
    private Config config;
    private String controllerClassName;
    private String controllerMethodName;
    private int methodParametersCount;
    private boolean metrics;
    private boolean async;
    private boolean hasRequestFilter;

    public DispatcherHandler(Class<?> controllerClass, String controllerMethod, boolean async) {
        Objects.requireNonNull(controllerClass, "controllerClass can not be null");
        Objects.requireNonNull(controllerMethod, "controllerMethod can not be null");

        this.controllerClass = controllerClass;
        this.controllerMethodName = controllerMethod;
        this.controllerClassName = controllerClass.getSimpleName();
        this.methodParameters = getMethodParameters();
        this.methodParametersCount = this.methodParameters.size();
        this.config = Application.getInstance(Config.class);
        this.async = async;
        this.hasRequestFilter = Application.getInjector().getAllBindings().containsKey(com.google.inject.Key.get(MangooRequestFilter.class));
        this.metrics = Application.getInstance(Config.class).isAdminMetricsEnabled();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (this.metrics) {
            exchange.addResponseCommitListener(Application.getInstance(MetricsListener.class));
        }

        new RequestHandler()
            .controllerInstance(Application.getInstance(this.controllerClass))
            .controllerClass(this.controllerClass)
            .controllerClassName(this.controllerClassName)
            .controllerMethodName(this.controllerMethodName)
            .methodParameters(this.methodParameters)
            .methodParameterCount(this.methodParametersCount)
            .hasRequestFilter(this.hasRequestFilter)
            .config(this.config)
            .async(this.async)
            .handleRequest(exchange);
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
}