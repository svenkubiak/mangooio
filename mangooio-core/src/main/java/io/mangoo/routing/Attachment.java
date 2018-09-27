package io.mangoo.routing;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import io.mangoo.enums.Required;
import io.mangoo.i18n.Messages;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.bindings.Session;
import io.mangoo.templating.TemplateEngine;

/**
 *
 * @author svenkubiak
 *
 */
public class Attachment {
    private final long start = System.currentTimeMillis();
    private Authentication authentication;
    private String controllerClassName;
    private String controllerMethodName;
    private String body;
    private String username;
    private String password;
    private List<Annotation> classAnnotations;
    private Class<?> controllerClass;
    private Object controllerInstance;
    private Flash flash;
    private Form form;
    private Locale locale;
    private Messages messages;
    private Method method;
    private List<Annotation> methodAnnotations;
    private Map<String, Class<?>> methodParameters;
    private Request request;
    private Map<String, String> requestParameter;
    private Response response;
    private Session session;
    private TemplateEngine templateEngine;
    private int limit;
    private int methodParametersCount;
    private boolean requestFilter;
    private boolean requiresAuthentication;
    private boolean requiresAuthorization;

    public static Attachment build() {
        return new Attachment();
    }
    
    public Authentication getAuthentication() {
        return this.authentication;
    }

    public String getBody() {
        return this.body;
    }

    public List<Annotation> getClassAnnotations() {
        return new ArrayList<>(this.classAnnotations);
    }

    public String getControllerAndMethod() {
        return this.controllerClassName + ":" + this.controllerMethodName;
    }

    public Class<?> getControllerClass() {
        return this.controllerClass;
    }

    public String getControllerClassName() {
        return this.controllerClassName;
    }

    public Object getControllerInstance() {
        return this.controllerInstance;
    }

    public String getControllerMethodName() {
        return this.controllerMethodName;
    }

    public Flash getFlash() {
        return this.flash;
    }
    
    public Form getForm() {
        return this.form;
    }

    public int getLimit() {
        return this.limit;
    }

    public Locale getLocale() {
        return this.locale;
    }
    
    public Messages getMessages() {
        return this.messages;
    }
    
    public Method getMethod() {
        return this.method;
    }
    
    public List<Annotation> getMethodAnnotations() {
        return new ArrayList<>(this.methodAnnotations);
    }

    public Map<String, Class<?>> getMethodParameters() {
        return this.methodParameters;
    }
    
    public int getMethodParametersCount() {
        return this.methodParametersCount;
    }
    
    public String getPassword() {
        return this.password;
    }

    public Request getRequest() {
        return this.request;
    }

    public  Map<String, String> getRequestParameter() {
        return this.requestParameter;
    }

    public Response getResponse() {
        return this.response;
    }

    public long getResponseTime() {
        return System.currentTimeMillis() - this.start;
    }
    
    public Session getSession() {
        return this.session;
    }

    public TemplateEngine getTemplateEngine() {
        return this.templateEngine;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean hasAuthentication() {
        return this.requiresAuthentication;
    }
    
    public boolean hasBasicAuthentication() {
        return StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.password);
    }

    public boolean hasLimit() {
        return this.limit > 0;
    }

    public boolean hasRequestFilter() {
        return this.requestFilter;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setFlash(Flash flash) {
        this.flash = flash;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Attachment withClassAnnotations(List<Annotation> classAnnotations) {
        this.classAnnotations = new ArrayList<>(classAnnotations);
        return this;
    }

    public Attachment withControllerClass(Class<?> controllerClass) {
        this.controllerClass = Objects.requireNonNull(controllerClass, Required.CONTROLLER_CLASS.toString());
        return this;
    }

    public Attachment withControllerClassName(String controllerClassName) {
        this.controllerClassName = Objects.requireNonNull(controllerClassName, Required.CONTROLLER_NAME.toString());
        return this;
    }

    public Attachment withControllerInstance(Object controllerInstance) {
        this.controllerInstance = Objects.requireNonNull(controllerInstance, Required.CONTROLLER_INSTANCE.toString());
        return this;
    }

    public Attachment withControllerMethodName(String controllerMethodName) {
        this.controllerMethodName = Objects.requireNonNull(controllerMethodName, Required.CONTROLLER_METHOD.toString());
        return this;
    }

    public Attachment withLimit(int limit) {
        this.limit = limit;
        return this;
    }
    
    public Attachment withLocale(Locale locale) {
        this.locale = Objects.requireNonNull(locale, Required.LOCALE.toString());
        return this;
    }
    
    public Attachment withMessages(Messages messages) {
        this.messages = Objects.requireNonNull(messages, Required.MESSAGES.toString());
        return this;
    }
    
    public Attachment withMethod(Method method) {
        this.method = Objects.requireNonNull(method, Required.METHOD.toString());
        return this;
    }
    
    public Attachment withMethodAnnotations(List<Annotation> methodAnnotations) {
        this.methodAnnotations = new ArrayList<>(methodAnnotations);
        return this;
    }
    
    public Attachment withMethodParameterCount(int methodParametersCount) {
        this.methodParametersCount = methodParametersCount;
        return this;
    }
    
    public Attachment withMethodParameters(Map<String, Class<?>> methodParameters) {
        this.methodParameters = Objects.requireNonNull(methodParameters, Required.METHOD_PARAMETERS.toString());
        return this;
    }

    public Attachment withBasicAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
        
        return this;
    }
    
    public Attachment withRequestFilter(boolean hasRequestFilter) {
        this.requestFilter = hasRequestFilter;
        return this;
    }

    public Attachment withRequestParameter(Map<String, String> requestParameter) {
        this.requestParameter = Objects.requireNonNull(requestParameter, Required.REQUEST_PARAMETER.toString());
        return this;
    }
    
    public Attachment withTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = Objects.requireNonNull(templateEngine, Required.TEMPLATE_ENGINE.toString());
        return this;
    }

    public Attachment withAuthentication(boolean authentication) {
        this.requiresAuthentication = authentication;
        return this;
    }
    
    public Attachment withAuthorization(boolean authorization) {
        this.requiresAuthorization = authorization;
        return this;
    }

    public boolean hasAuthorization() {
        return this.requiresAuthorization;
    }
}