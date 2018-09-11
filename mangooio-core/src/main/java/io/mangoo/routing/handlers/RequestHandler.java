package io.mangoo.routing.handlers;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
import io.mangoo.templating.TemplateContext;
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
    private Attachment attachment;
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        this.attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        this.attachment.setBody(getRequestBody(exchange));
        this.attachment.setRequest(getRequest(exchange));

        final Response response = getResponse(exchange);
        response.getCookies().forEach(exchange::setResponseCookie);

        this.attachment.setResponse(response);

        exchange.putAttachment(RequestUtils.getAttachmentKey(), this.attachment);
        nextHandler(exchange);
    }

    /**
     * Creates a new request object containing the current request data
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected Request getRequest(HttpServerExchange exchange) {
        final String authenticity = Optional.ofNullable(this.attachment.getRequestParameter()
                .get(Default.AUTHENTICITY.toString()))
                .orElse(this.attachment.getForm().get(Default.AUTHENTICITY.toString()));
        
        return new Request(exchange)
                .withSession(this.attachment.getSession())
                .withAuthenticity(authenticity)
                .withAuthentication(this.attachment.getAuthentication())
                .withParameter(this.attachment.getRequestParameter())
                .withBody(this.attachment.getBody());
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
    protected Response getResponse(HttpServerExchange exchange) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, MangooTemplateEngineException {
        //execute global request filter
        Response response = Response.withOk();
        if (this.attachment.hasRequestFilter()) {
            final MangooRequestFilter mangooRequestFilter = Application.getInstance(MangooRequestFilter.class);
            response = mangooRequestFilter.execute(this.attachment.getRequest(), response);
        }

        if (response.isEndResponse()) {
            return response;
        }

        //execute controller filters
        response = executeFilter(this.attachment.getClassAnnotations(), response);
        if (response.isEndResponse()) {
            return response;
        }

        //execute method filters
        response = executeFilter(this.attachment.getMethodAnnotations(), response);
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
    protected Response invokeController(HttpServerExchange exchange, Response response) throws IllegalAccessException, InvocationTargetException, MangooTemplateEngineException {
        Response invokedResponse;

        if (this.attachment.getMethodParameters().isEmpty()) {
            invokedResponse = (Response) this.attachment.getMethod().invoke(this.attachment.getControllerInstance());
        } else {
            final Object [] convertedParameters = getConvertedParameters(exchange);
            invokedResponse = (Response) this.attachment.getMethod().invoke(this.attachment.getControllerInstance(), convertedParameters);
        }

        invokedResponse.andContent(response.getContent());
        invokedResponse.andHeaders(response.getHeaders());
        
        if (!invokedResponse.isRendered()) {
            TemplateContext templateContext = new TemplateContext(invokedResponse.getContent())
                    .withFlash(this.attachment.getFlash())
                    .withSession(this.attachment.getSession())
                    .withForm(this.attachment.getForm())
                    .withMessages(this.attachment.getMessages())
                    .withController(this.attachment.getControllerAndMethod())
                    .withPrettyTime(this.attachment.getLocale())
                    .withAuthenticity(this.attachment.getSession())
                    .withAuthenticityForm(this.attachment.getSession())
                    .withTemplatePath(getTemplatePath(invokedResponse));
            
            invokedResponse.andBody(this.attachment.getTemplateEngine().renderTemplate(templateContext));
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
        return StringUtils.isBlank(response.getTemplate()) ? (this.attachment.getControllerClassName() + "/" + this.attachment.getTemplateEngine().getTemplateName(this.attachment.getControllerMethodName())) : response.getTemplate();
    }

    /**
     * Creates an array with the request controller method parameter and sets the appropriate values
     *
     * @param exchange The Undertow HttpServerExchange
     * @return an array with the request controller method parameter and sets the appropriate values
     *
     * @throws IOException
     */
    protected Object[] getConvertedParameters(HttpServerExchange exchange) {
        final Object [] convertedParameters = new Object[this.attachment.getMethodParametersCount()];

        int index = 0;
        for (final Map.Entry<String, Class<?>> entry : this.attachment.getMethodParameters().entrySet()) {
            final String key = entry.getKey();
            final Class<?> clazz = entry.getValue();
            final Binding binding = Optional.ofNullable(Binding.fromString(clazz.getName())).orElse(Binding.UNDEFINED);

            switch (binding) {
            case FORM:
                convertedParameters[index] = this.attachment.getForm(); //NOSONAR
                break;
            case AUTHENTICATION:
                convertedParameters[index] = this.attachment.getAuthentication();
                break;
            case SESSION:
                convertedParameters[index] = this.attachment.getSession();
                break;
            case FLASH:
                convertedParameters[index] = this.attachment.getFlash();
                break;
            case REQUEST:
                convertedParameters[index] = this.attachment.getRequest();
                break;
            case MESSAGES:
                convertedParameters[index] = this.attachment.getMessages();
                break;
            case LOCALDATE:
                convertedParameters[index] = StringUtils.isBlank(this.attachment.getRequestParameter().get(key)) ? null : LocalDate.parse(this.attachment.getRequestParameter().get(key));
                break;
            case LOCALDATETIME:
                convertedParameters[index] = StringUtils.isBlank(this.attachment.getRequestParameter().get(key)) ? null : LocalDateTime.parse(this.attachment.getRequestParameter().get(key));
                break;
            case STRING:
                convertedParameters[index] = StringUtils.isBlank(this.attachment.getRequestParameter().get(key)) ? null : this.attachment.getRequestParameter().get(key);
                break;
            case INT_PRIMITIVE:
                convertedParameters[index] = StringUtils.isBlank(this.attachment.getRequestParameter().get(key)) ? 0 : Integer.parseInt(this.attachment.getRequestParameter().get(key));
                break;               
            case INTEGER:
                convertedParameters[index] = StringUtils.isBlank(this.attachment.getRequestParameter().get(key)) ? null : Integer.valueOf(this.attachment.getRequestParameter().get(key));
                break;
            case DOUBLE_PRIMITIVE:
                convertedParameters[index] = StringUtils.isBlank(this.attachment.getRequestParameter().get(key)) ? 0 : Double.parseDouble(this.attachment.getRequestParameter().get(key));
                break;
            case DOUBLE:
                convertedParameters[index] = StringUtils.isBlank(this.attachment.getRequestParameter().get(key)) ? null : Double.valueOf(this.attachment.getRequestParameter().get(key));
                break;
            case FLOAT_PRIMITIVE:
                convertedParameters[index] = StringUtils.isBlank(this.attachment.getRequestParameter().get(key)) ? 0 : Float.parseFloat(this.attachment.getRequestParameter().get(key));
                break;                
            case FLOAT:
                convertedParameters[index] = StringUtils.isBlank(this.attachment.getRequestParameter().get(key)) ? null : Float.valueOf(this.attachment.getRequestParameter().get(key));
                break;
            case LONG_PRIMITIVE:
                convertedParameters[index] = StringUtils.isBlank(this.attachment.getRequestParameter().get(key)) ? 0 : Long.parseLong(this.attachment.getRequestParameter().get(key));
                break;
            case LONG:
                convertedParameters[index] = StringUtils.isBlank(this.attachment.getRequestParameter().get(key)) ? null : Long.valueOf(this.attachment.getRequestParameter().get(key));
                break;
            case OPTIONAL:
                convertedParameters[index] = StringUtils.isBlank(this.attachment.getRequestParameter().get(key)) ? Optional.empty() : Optional.of(this.attachment.getRequestParameter().get(key));
                break;                
            case UNDEFINED:
                convertedParameters[index] = RequestUtils.isJsonRequest(exchange) ? JsonUtils.fromJson(this.attachment.getBody(), clazz) : null;
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
    protected Response executeFilter(List<Annotation> annotations, Response response) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (final Annotation annotation : annotations) {
            final FilterWith filterWith = (FilterWith) annotation;
            for (final Class<?> clazz : filterWith.value()) {
                if (response.isEndResponse()) {
                    return response;
                } else {
                    final Method classMethod = clazz.getMethod(Default.FILTER_METHOD.toString(), Request.class, Response.class);
                    response = (Response) classMethod.invoke(Application.getInstance(clazz), this.attachment.getRequest(), response); //NOSONAR
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
    @SuppressWarnings("all")
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(OutboundCookiesHandler.class).handleRequest(exchange);
    }
}