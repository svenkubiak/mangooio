package io.mangoo.routing.handlers;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import freemarker.template.TemplateException;
import io.mangoo.annotations.FilterWith;
import io.mangoo.core.Application;
import io.mangoo.enums.Binding;
import io.mangoo.enums.Default;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.interfaces.MangooRequestFilter;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.JsonUtils;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * Main class that handles all controller requests
 *
 * @author skubiak
 *
 */
public class RequestHandler implements HttpHandler {
    private Attachment requestAttachment;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        this.requestAttachment = exchange.getAttachment(RequestUtils.ATTACHMENT_KEY);
        this.requestAttachment.setBody(getRequestBody(exchange));
        this.requestAttachment.setRequest(getRequest(exchange));

        final Response response = getResponse(exchange);
        response.getCookies().forEach(exchange::setResponseCookie);

        this.requestAttachment.setResponse(response);

        exchange.putAttachment(RequestUtils.ATTACHMENT_KEY, this.requestAttachment);
        nextHandler(exchange);
    }

    /**
     * Creates a new request object containing the current request data
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected Request getRequest(HttpServerExchange exchange) {
        final String authenticityToken = Optional.ofNullable(this.requestAttachment.getRequestParameter()
                .get(Default.AUTHENTICITY_TOKEN.toString())).orElse(this.requestAttachment.getForm().get(Default.AUTHENTICITY_TOKEN.toString()));
        
        return new Request(exchange, this.requestAttachment.getSession(), authenticityToken, this.requestAttachment.getAuthentication(), this.requestAttachment.getRequestParameter(), this.requestAttachment.getBody());
    }

    /**
     * Execute filters if exists in the following order:
     * RequestFilter, ControllerFilter, MethodFilter
     *
     * @param exchange The Undertow HttpServerExchange
     * @return A Response object that will be merged to the final response
     *
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws TemplateException
     * @throws IOException
     * @throws MangooTemplateEngineException 
     */
    protected Response getResponse(HttpServerExchange exchange) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException, TemplateException, MangooTemplateEngineException {
        //execute global request filter
        Response response = Response.withOk();
        if (this.requestAttachment.hasRequestFilter()) {
            final MangooRequestFilter mangooRequestFilter = Application.getInstance(MangooRequestFilter.class);
            response = mangooRequestFilter.execute(this.requestAttachment.getRequest(), response);
        }

        if (response.isEndResponse()) {
            return response;
        }

        //execute controller filters
        response = executeFilter(this.requestAttachment.getControllerClass().getAnnotations(), response);
        if (response.isEndResponse()) {
            return response;
        }

        //execute method filters
        response = executeFilter(this.requestAttachment.getMethod().getAnnotations(), response);
        if (response.isEndResponse()) {
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
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IOException
     * @throws TemplateException
     * @throws MangooTemplateEngineException 
     */
    protected Response invokeController(HttpServerExchange exchange, Response response) throws IllegalAccessException, InvocationTargetException, IOException, TemplateException, MangooTemplateEngineException {
        Response invokedResponse;

        if (this.requestAttachment.getMethodParameters().isEmpty()) {
            invokedResponse = (Response) this.requestAttachment.getMethod().invoke(this.requestAttachment.getControllerInstance());
        } else {
            final Object [] convertedParameters = getConvertedParameters(exchange);
            invokedResponse = (Response) this.requestAttachment.getMethod().invoke(this.requestAttachment.getControllerInstance(), convertedParameters);
        }

        invokedResponse.andContent(response.getContent());
        invokedResponse.andHeaders(response.getHeaders());
        if (!invokedResponse.isRendered()) {
            invokedResponse.andBody(this.requestAttachment.getTemplateEngine().render(
                    this.requestAttachment.getFlash(),
                    this.requestAttachment.getSession(),
                    this.requestAttachment.getForm(),
                    this.requestAttachment.getMessages(),
                    getTemplatePath(invokedResponse),
                    invokedResponse.getContent()));
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
        return StringUtils.isBlank(response.getTemplate()) ? (this.requestAttachment.getControllerClassName() + "/" + this.requestAttachment.getTemplateEngine().getTemplateName(this.requestAttachment.getControllerMethodName())) : response.getTemplate();
    }

    /**
     * Creates an array with the request controller method parameter and sets the appropriate values
     *
     * @param exchange The Undertow HttpServerExchange
     * @return an array with the request controller method parameter and sets the appropriate values
     *
     * @throws IOException
     */
    protected Object[] getConvertedParameters(HttpServerExchange exchange) throws IOException {
        final Object [] convertedParameters = new Object[this.requestAttachment.getMethodParametersCount()];

        int index = 0;
        for (final Map.Entry<String, Class<?>> entry : this.requestAttachment.getMethodParameters().entrySet()) {
            final String key = entry.getKey();
            final Class<?> clazz = entry.getValue();
            final Binding binding = Optional.ofNullable(Binding.fromString(clazz.getName())).orElse(Binding.UNDEFINED);

            switch (binding) {
            case FORM:
                convertedParameters[index] = this.requestAttachment.getForm();
                break;
            case AUTHENTICATION:
                convertedParameters[index] = this.requestAttachment.getAuthentication();
                break;
            case SESSION:
                convertedParameters[index] = this.requestAttachment.getSession();
                break;
            case FLASH:
                convertedParameters[index] = this.requestAttachment.getFlash();
                break;
            case REQUEST:
                convertedParameters[index] = this.requestAttachment.getRequest();
                break;
            case LOCALDATE:
                convertedParameters[index] = StringUtils.isBlank(this.requestAttachment.getRequestParameter().get(key)) ? null : LocalDate.parse(this.requestAttachment.getRequestParameter().get(key));
                break;
            case LOCALDATETIME:
                convertedParameters[index] = StringUtils.isBlank(this.requestAttachment.getRequestParameter().get(key)) ? null : LocalDateTime.parse(this.requestAttachment.getRequestParameter().get(key));
                break;
            case STRING:
                convertedParameters[index] = StringUtils.isBlank(this.requestAttachment.getRequestParameter().get(key)) ? null : this.requestAttachment.getRequestParameter().get(key);
                break;
            case INT_PRIMITIVE:
            case INTEGER:
                convertedParameters[index] = StringUtils.isBlank(this.requestAttachment.getRequestParameter().get(key)) ? null : Integer.valueOf(this.requestAttachment.getRequestParameter().get(key));
                break;
            case DOUBLE_PRIMITIVE:
            case DOUBLE:
                convertedParameters[index] = StringUtils.isBlank(this.requestAttachment.getRequestParameter().get(key)) ? null : Double.valueOf(this.requestAttachment.getRequestParameter().get(key));
                break;
            case FLOAT_PRIMITIVE:
            case FLOAT:
                convertedParameters[index] = StringUtils.isBlank(this.requestAttachment.getRequestParameter().get(key)) ? null : Float.valueOf(this.requestAttachment.getRequestParameter().get(key));
                break;
            case LONG_PRIMITIVE:
            case LONG:
                convertedParameters[index] = StringUtils.isBlank(this.requestAttachment.getRequestParameter().get(key)) ? null : Long.valueOf(this.requestAttachment.getRequestParameter().get(key));
                break;
            case UNDEFINED:
                convertedParameters[index] = RequestUtils.isJsonRequest(exchange) ? JsonUtils.fromJson(this.requestAttachment.getBody(), clazz) : null;
                break;
            default:
                convertedParameters[index] = null;
                break;
            }

            index++;
        }

        return convertedParameters;
    }

    /**
     * Executes all filters on controller and method level
     *
     * @param annotations An array of @FilterWith annotated classes and methods
     * @param response
     * @return True if the request should continue after filter execution, false otherwise
     *
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    protected Response executeFilter(Annotation[] annotations, Response response) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (final Annotation annotation : annotations) {
            if (annotation.annotationType().equals(FilterWith.class)) {
                final FilterWith filterWith = (FilterWith) annotation;
                for (final Class<?> clazz : filterWith.value()) {
                    if (response.isEndResponse()) {
                        return response;
                    } else {
                        final Method classMethod = clazz.getMethod(Default.FILTER_METHOD.toString(), Request.class, Response.class);
                        response = (Response) classMethod.invoke(Application.getInstance(clazz), this.requestAttachment.getRequest(), response);
                    }
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
     * @throws IOException
     */
    protected String getRequestBody(HttpServerExchange exchange) throws IOException {
        String body = "";
        if (RequestUtils.isPostOrPut(exchange)) {
            exchange.startBlocking();
            body = IOUtils.toString(exchange.getInputStream());
        }

        return body;
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    @SuppressWarnings("all")
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(OutboundCookiesHandler.class).handleRequest(exchange);
    }
}