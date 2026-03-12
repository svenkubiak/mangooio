package io.mangoo.routing.handlers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.annotations.FilterWith;
import io.mangoo.constants.Const;
import io.mangoo.constants.Header;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.Binding;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.interfaces.filters.OncePerRequestFilter;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.bindings.UnprocessableContent;
import io.mangoo.templating.TemplateContext;
import io.mangoo.utils.JsonUtils;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.internal.MangooUtils;
import io.mangoo.utils.internal.Trace;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class RequestHandler implements HttpHandler {
    private static final String FILTER_METHOD = "execute";
    private final Config config;
    private Attachment attachment;

    @Inject
    public RequestHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG);
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        attachment.setBody(getRequestBody(exchange));
        attachment.setRequest(getRequest(exchange));

        Trace.startChild(exchange.getRequestPath(), Const.INVOKE_CONTROLLER);
        var response = getResponse(exchange);
        response.getCookies().forEach(exchange::setResponseCookie);

        attachment.setResponse(response);
        Trace.end(Const.INVOKE_CONTROLLER);

        exchange.putAttachment(RequestUtils.getAttachmentKey(), attachment);
        nextHandler(exchange);
    }

    /**
     * Creates a new request object containing the current request data
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected Request getRequest(HttpServerExchange exchange) {
        final String csrf = Optional
                .ofNullable(exchange.getRequestHeaders().getFirst(Const.CSRF_TOKEN))
                .orElseGet(() -> attachment.getForm().get(Const.CSRF_TOKEN));

        return new Request(exchange)
                .withSession(attachment.getSession())
                .withAuthentication(attachment.getAuthentication())
                .withParameter(attachment.getRequestParameter())
                .withCsrf(csrf)
                .withBody(attachment.getBody());
    }

    /**
     * Execute filters if exists in the following order:
     * RequestFilter, ControllerFilter, MethodFilter
     *
     * @param exchange The Undertow HttpServerExchange
     * @return A Response object that will be merged to the final response
     *
     * @throws NoSuchMethodException when no method is found
     * @throws IllegalAccessException when an illegal access occurs
     * @throws InvocationTargetException when an invocation fails
     * @throws MangooTemplateEngineException when the template rendering fails
     */
    protected Response getResponse(HttpServerExchange exchange) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, MangooTemplateEngineException {
        //execute global request filter
        var response = Response.ok();
        if (attachment.hasRequestFilter()) {
            final OncePerRequestFilter mangooRequestFilter = Application.getInstance(OncePerRequestFilter.class);
            response = mangooRequestFilter.execute(attachment.getRequest(), response);
        }

        if (response.isEndResponse()) {
            return response;
        }

        //execute controller filters
        response = executeFilter(attachment.getClassAnnotations(), response);
        if (response.isEndResponse()) {
            return response;
        }

        //execute method filters
        response = executeFilter(attachment.getMethodAnnotations(), response);
        if (response.isEndResponse()) {
            return response;
        }

        if (response.isRedirect()) {
            return response;
        }

        return invokeController(exchange, response);
    }

    /**
     * Invokes the controller methods and retrieves the response which
     * is later send to the client
     *
     * @param exchange The Undertow HttpServerExchange
     * @return A response object
     *
     * @throws IllegalAccessException when an illegal access occurs
     * @throws InvocationTargetException when an invocation fails
     * @throws MangooTemplateEngineException when the template rendering fails
     */
    protected Response invokeController(HttpServerExchange exchange, Response response) throws IllegalAccessException, InvocationTargetException, MangooTemplateEngineException {
        Response invokedResponse;

        if (attachment.getMethodParameters().isEmpty()) {
            invokedResponse = (Response) attachment.getMethod().invoke(attachment.getControllerInstance());
        } else {
            final Object [] convertedParameters = getConvertedParameters(exchange);
            if (Arrays.stream(convertedParameters).anyMatch(UnprocessableContent.class::isInstance)) {
                return Response.status(422).end();
            }

            Set<ConstraintViolation<Object>> violations =
                    MangooUtils.validator().validateParameters(
                            attachment.getControllerInstance(),
                            attachment.getMethod(),
                            convertedParameters);

            if (!violations.isEmpty()) {
                if (config.isValidationPassthrough()) {
                    Map<String, String> errors = new HashMap<>();
                    violations.forEach(validation -> {
                        var path = validation.getPropertyPath();
                        Path.Node last = null;
                        for (Path.Node node : path) {
                            last = node;
                        }
                        String fieldName = last != null ? last.getName() : null;
                        String message = validation.getMessage();

                        errors.put(fieldName, message);
                    });

                    return Response.badRequest()
                            .bodyJson(Map.of("errors", errors))
                            .end();
                } else {
                    return Response.badRequest()
                            .bodyDefault()
                            .end();
                }
            }

            invokedResponse = (Response) attachment.getMethod().invoke(attachment.getControllerInstance(), convertedParameters);
        }

        if (invokedResponse.isRendered() && response.getContent() != null && !response.getContent().isEmpty()) {
            invokedResponse.render(response.getContent());
        }
        invokedResponse.headers(response.getHeaders());

        if (!invokedResponse.isRedirect() && invokedResponse.isRendered()) {
            var templateContext = new TemplateContext(invokedResponse.getContent())
                    .withFlash(attachment.getFlash())
                    .withSession(attachment.getSession())
                    .withForm(attachment.getForm())
                    .withMessages(attachment.getMessages())
                    .withController(attachment.getControllerAndMethod())
                    .withPrettyTime(attachment.getLocale())
                    .withCsrfForm(attachment.getSession())
                    .withCsrfToken(attachment.getSession())
                    .withTemplatePath(getTemplatePath(invokedResponse));
            
            invokedResponse.bodyHtml(attachment.getTemplateEngine().renderTemplate(templateContext));
        }

        for (Cookie cookie : response.getCookies()) {
            invokedResponse.cookie(cookie);
        }

        return invokedResponse;
    }

    /**
     * Returns the complete path to the template based on the
     * controller and method name
     *
     * @param response The current response
     *
     * @return A case-sensitive template path, e.g. /ApplicationController/index.ftl
     */
    protected String getTemplatePath(Response response) {
        return StringUtils.isBlank(response.getTemplate()) ? (attachment.getControllerClassName() + "/" + attachment.getTemplateEngine().getTemplateName(attachment.getControllerMethodName())) : response.getTemplate();
    }

    /**
     * Creates an array with the request controller method parameter and sets the appropriate values
     *
     * @param exchange The Undertow HttpServerExchange
     * @return an array with the request controller method parameter and sets the appropriate values
     */
    @SuppressFBWarnings(justification = "Intentionally adding unrelated types", value = "UCC_UNRELATED_COLLECTION_CONTENTS")
    protected Object[] getConvertedParameters(HttpServerExchange exchange) {
        final var convertedParameters = new Object[attachment.getMethodParametersCount()];

        var index = 0;
        for (final Map.Entry<String, Class<?>> entry : attachment.getMethodParameters().entrySet()) {
            final String key = entry.getKey();
            final Class<?> clazz = entry.getValue();
            final var binding = Optional.ofNullable(Binding.fromString(clazz.getName())).orElse(Binding.UNDEFINED);

            convertedParameters[index] = switch (binding) {

                case FORM -> attachment.getForm();
                case AUTHENTICATION -> attachment.getAuthentication();
                case SESSION -> attachment.getSession();
                case FLASH -> attachment.getFlash();
                case REQUEST -> attachment.getRequest();
                case MESSAGES -> attachment.getMessages();

                case LOCAL_DATE -> {
                    String value = attachment.getRequestParameter().get(key);
                    if (StringUtils.isBlank(value)) {
                        yield null;
                    }
                    try {
                        yield LocalDate.parse(value);
                    } catch (Exception e) {
                        yield new UnprocessableContent();
                    }
                }

                case LOCAL_DATE_TIME -> {
                    String value = attachment.getRequestParameter().get(key);
                    if (StringUtils.isBlank(value)) {
                        yield null;
                    }
                    try {
                        yield LocalDateTime.parse(value);
                    } catch (Exception e) {
                        yield new UnprocessableContent();
                    }
                }

                case STRING -> {
                    String value = attachment.getRequestParameter().get(key);
                    yield StringUtils.isBlank(value) ? null : value;
                }

                case INT_PRIMITIVE -> {
                    String value = attachment.getRequestParameter().get(key);
                    if (StringUtils.isBlank(value)) {
                        yield 0;
                    }
                    try {
                        yield Integer.valueOf(value);
                    } catch (NumberFormatException e) {
                        yield new UnprocessableContent();
                    }
                }

                case INTEGER -> {
                    String value = attachment.getRequestParameter().get(key);
                    if (StringUtils.isBlank(value)) {
                        yield null;
                    }
                    try {
                        yield Integer.valueOf(value);
                    } catch (NumberFormatException e) {
                        yield new UnprocessableContent();
                    }
                }

                case DOUBLE_PRIMITIVE -> {
                    String value = attachment.getRequestParameter().get(key);
                    if (StringUtils.isBlank(value)) {
                        yield 0d;
                    }
                    try {
                        yield Double.valueOf(value);
                    } catch (NumberFormatException e) {
                        yield new UnprocessableContent();
                    }
                }

                case DOUBLE -> {
                    String value = attachment.getRequestParameter().get(key);
                    if (StringUtils.isBlank(value)) {
                        yield null;
                    }
                    try {
                        yield Double.valueOf(value);
                    } catch (NumberFormatException e) {
                        yield new UnprocessableContent();
                    }
                }

                case FLOAT_PRIMITIVE -> {
                    String value = attachment.getRequestParameter().get(key);
                    if (StringUtils.isBlank(value)) {
                        yield 0f;
                    }
                    try {
                        yield Float.parseFloat(value);
                    } catch (NumberFormatException e) {
                        yield new UnprocessableContent();
                    }
                }

                case FLOAT -> {
                    String value = attachment.getRequestParameter().get(key);
                    if (StringUtils.isBlank(value)) {
                        yield null;
                    }
                    try {
                        yield Float.valueOf(value);
                    } catch (NumberFormatException e) {
                        yield new UnprocessableContent();
                    }
                }

                case LONG_PRIMITIVE -> {
                    String value = attachment.getRequestParameter().get(key);
                    if (StringUtils.isBlank(value)) {
                        yield 0L;
                    }
                    try {
                        yield Long.valueOf(value);
                    } catch (NumberFormatException e) {
                        yield new UnprocessableContent();
                    }
                }

                case LONG -> {
                    String value = attachment.getRequestParameter().get(key);
                    if (StringUtils.isBlank(value)) {
                        yield null;
                    }
                    try {
                        yield Long.valueOf(value);
                    } catch (NumberFormatException e) {
                        yield new UnprocessableContent();
                    }
                }

                case OPTIONAL -> {
                    String value = attachment.getRequestParameter().get(key);
                    yield StringUtils.isBlank(value) ? Optional.empty() : Optional.of(value);
                }

                case BOOLEAN_PRIMITIVE -> {
                    String value = attachment.getRequestParameter().get(key);
                    if (StringUtils.isBlank(value)) {
                        yield Boolean.FALSE;
                    }
                    if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                        yield new UnprocessableContent();
                    }
                    yield Boolean.valueOf(value);
                }

                case BOOLEAN -> {
                    String value = attachment.getRequestParameter().get(key);
                    if (StringUtils.isBlank(value)) {
                        yield null;
                    }
                    if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                        yield new UnprocessableContent();
                    }
                    yield Boolean.valueOf(value);
                }

                case UNDEFINED ->
                        RequestUtils.isJsonRequest(exchange)
                                ? JsonUtils.toObjectWithFallback(attachment.getBody(), clazz)
                                : null;

                default -> null;
            };

            index++;
        }

        return convertedParameters;
    }

    /**
     * Executes all filters on controller and method level
     *
     * @param annotations An array of @FilterWith annotated classes and methods
     * @param response The response to use
     * @return The updated response
     *
     * @throws NoSuchMethodException when the method is not found
     * @throws IllegalAccessException when an illegal access occurs
     * @throws InvocationTargetException when the target is not found
     */
    protected Response executeFilter(List<Annotation> annotations, Response response) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (final Annotation annotation : annotations) { //NOSONAR
            final var filterWith = (FilterWith) annotation;
            for (final Class<?> clazz : filterWith.value()) {
                if (response.isEndResponse()) {
                    return response;
                } else {
                    final var classMethod = clazz.getMethod(FILTER_METHOD, Request.class, Response.class);
                    response = (Response) classMethod.invoke(Application.getInstance(clazz), attachment.getRequest(), response);
                }
            }
        }

        return response;
    }

    /**
     * Retrieves the complete request body from the request
     *
     * @param exchange The Undertow HttpServerExchange
     * @return A body object containing the request body
     *
     * @throws IOException when setting the body fails
     */
    protected String getRequestBody(HttpServerExchange exchange) throws IOException {
        if (!RequestUtils.isPostPutPatch(exchange)) {
            return Strings.EMPTY;
        }

        String contentType = exchange.getRequestHeaders().getFirst(Header.CONTENT_TYPE);
        if (RequestUtils.isJsonRequest(exchange)) {
            exchange.startBlocking();
            return IOUtils.toString(exchange.getInputStream(), StandardCharsets.UTF_8);
        }

        if (contentType != null &&
                (contentType.startsWith("multipart/") ||
                 contentType.startsWith("application/x-www-form-urlencoded"))) {
            return Strings.EMPTY;
        }

        exchange.startBlocking();
        return IOUtils.toString(exchange.getInputStream(), StandardCharsets.UTF_8);
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(OutboundCookiesHandler.class).handleRequest(exchange);
    }
}