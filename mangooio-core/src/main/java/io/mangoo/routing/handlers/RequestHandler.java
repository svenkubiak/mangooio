package io.mangoo.routing.handlers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.core.Application;
import io.mangoo.enums.Binding;
import io.mangoo.enums.Default;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.interfaces.filters.OncePerRequestFilter;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.Response;
import io.mangoo.routing.annotations.FilterWith;
import io.mangoo.routing.bindings.Request;
import io.mangoo.templating.TemplateContext;
import io.mangoo.utils.JsonUtils;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RequestHandler implements HttpHandler {
    private Attachment attachment;
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        attachment.setBody(getRequestBody(exchange));
        attachment.setRequest(getRequest(exchange));

        var response = getResponse(exchange);
        response.getCookies().forEach(exchange::setResponseCookie);

        attachment.setResponse(response);

        exchange.putAttachment(RequestUtils.getAttachmentKey(), attachment);
        nextHandler(exchange);
    }

    /**
     * Creates a new request object containing the current request data
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected Request getRequest(HttpServerExchange exchange) {
        return new Request(exchange)
                .withSession(attachment.getSession())
                .withAuthentication(attachment.getAuthentication())
                .withParameter(attachment.getRequestParameter())
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
        var response = Response.withOk();
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
            invokedResponse = (Response) attachment.getMethod().invoke(attachment.getControllerInstance(), convertedParameters);
        }

        invokedResponse.andContent(response.getContent());
        invokedResponse.andHeaders(response.getHeaders());
        
        if (invokedResponse.isRendered()) {
            var templateContext = new TemplateContext(invokedResponse.getContent())
                    .withFlash(attachment.getFlash())
                    .withSession(attachment.getSession())
                    .withForm(attachment.getForm())
                    .withMessages(attachment.getMessages())
                    .withController(attachment.getControllerAndMethod())
                    .withPrettyTime(attachment.getLocale())
                    .withTemplatePath(getTemplatePath(invokedResponse));
            
            invokedResponse.andHtmlBody(attachment.getTemplateEngine().renderTemplate(templateContext));
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
                case LOCALDATE -> StringUtils.isBlank(attachment.getRequestParameter().get(key)) ? null : LocalDate.parse(attachment.getRequestParameter().get(key));
                case LOCALDATETIME -> StringUtils.isBlank(attachment.getRequestParameter().get(key)) ? null : LocalDateTime.parse(attachment.getRequestParameter().get(key));
                case STRING -> StringUtils.isBlank(attachment.getRequestParameter().get(key)) ? null : attachment.getRequestParameter().get(key);
                case INT_PRIMITIVE -> StringUtils.isBlank(attachment.getRequestParameter().get(key)) ? 0 : Integer.parseInt(attachment.getRequestParameter().get(key));
                case INTEGER -> StringUtils.isBlank(attachment.getRequestParameter().get(key)) ? null : Integer.valueOf(attachment.getRequestParameter().get(key));
                case DOUBLE_PRIMITIVE -> StringUtils.isBlank(attachment.getRequestParameter().get(key)) ? 0 : Double.parseDouble(attachment.getRequestParameter().get(key));
                case DOUBLE -> StringUtils.isBlank(attachment.getRequestParameter().get(key)) ? null : Double.valueOf(attachment.getRequestParameter().get(key));
                case FLOAT_PRIMITIVE -> StringUtils.isBlank(attachment.getRequestParameter().get(key)) ? 0 : Float.parseFloat(attachment.getRequestParameter().get(key));
                case FLOAT -> StringUtils.isBlank(attachment.getRequestParameter().get(key)) ? null : Float.valueOf(attachment.getRequestParameter().get(key));
                case LONG_PRIMITIVE -> StringUtils.isBlank(attachment.getRequestParameter().get(key)) ? 0 : Long.parseLong(attachment.getRequestParameter().get(key));
                case LONG -> StringUtils.isBlank(attachment.getRequestParameter().get(key)) ? null : Long.valueOf(attachment.getRequestParameter().get(key));
                case OPTIONAL -> StringUtils.isBlank(attachment.getRequestParameter().get(key)) ? Optional.empty() : Optional.of(attachment.getRequestParameter().get(key));
                case UNDEFINED -> RequestUtils.isJsonRequest(exchange) ? JsonUtils.fromJson(attachment.getBody(), clazz) : null;
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
     * @return True if the request should continue after filter execution, false otherwise
     *
     * @throws NoSuchMethodException when the method is not found
     * @throws IllegalAccessException when an illegal access occurs
     * @throws InvocationTargetException when the target is not found
     */
    protected Response executeFilter(List<Annotation> annotations, Response response) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (final Annotation annotation : annotations) {
            final var filterWith = (FilterWith) annotation;
            for (final Class<?> clazz : filterWith.value()) {
                if (response.isEndResponse()) {
                    return response;
                } else {
                    final var classMethod = clazz.getMethod(Default.FILTER_METHOD.toString(), Request.class, Response.class);
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
        var body = "";
        if (RequestUtils.isPostPutPatch(exchange)) {
            exchange.startBlocking();
            body = IOUtils.toString(exchange.getInputStream(), Default.ENCODING.toString());
        }

        return body;
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