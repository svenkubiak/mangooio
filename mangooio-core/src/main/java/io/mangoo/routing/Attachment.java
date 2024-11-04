package io.mangoo.routing;

import io.mangoo.constants.NotNull;
import io.mangoo.i18n.Messages;
import io.mangoo.routing.bindings.*;
import io.mangoo.templating.TemplateEngine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class Attachment {
    private Authentication authentication;
    private String controllerClassName;
    private String controllerMethodName;
    private String body;
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

    public static Attachment build() {
        return new Attachment();
    }
    
    public Authentication getAuthentication() {
        return this.authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
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

    public void setFlash(Flash flash) {
        this.flash = flash;
    }
    
    public Form getForm() {
        return this.form;
    }
    
    public void setForm(Form form) {
        this.form = form;
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

    public Request getRequest() {
        return this.request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public  Map<String, String> getRequestParameter() {
        return this.requestParameter;
    }

    public Response getResponse() {
        return this.response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Session getSession() {
        return this.session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public TemplateEngine getTemplateEngine() {
        return this.templateEngine;
    }

    public boolean hasAuthentication() {
        return this.requiresAuthentication;
    }

    public boolean hasRequestFilter() {
        return this.requestFilter;
    }

    public Attachment withClassAnnotations(Collection<Annotation> classAnnotations) {
        this.classAnnotations = new ArrayList<>(classAnnotations);
        return this;
    }

    public Attachment withControllerClass(Class<?> controllerClass) {
        this.controllerClass = Objects.requireNonNull(controllerClass, NotNull.CONTROLLER_CLASS);
        return this;
    }

    public Attachment withControllerClassName(String controllerClassName) {
        this.controllerClassName = Objects.requireNonNull(controllerClassName, NotNull.CONTROLLER_NAME);
        return this;
    }

    public Attachment withControllerInstance(Object controllerInstance) {
        this.controllerInstance = Objects.requireNonNull(controllerInstance, NotNull.CONTROLLER_INSTANCE);
        return this;
    }

    public Attachment withControllerMethodName(String controllerMethodName) {
        this.controllerMethodName = Objects.requireNonNull(controllerMethodName, NotNull.CONTROLLER_METHOD);
        return this;
    }

    public Attachment withLimit(int limit) {
        this.limit = limit;
        return this;
    }
    
    public Attachment withLocale(Locale locale) {
        this.locale = Objects.requireNonNull(locale, NotNull.LOCALE);
        return this;
    }
    
    public Attachment withMessages(Messages messages) {
        this.messages = Objects.requireNonNull(messages, NotNull.MESSAGES);
        return this;
    }
    
    public Attachment withMethod(Method method) {
        this.method = Objects.requireNonNull(method, NotNull.METHOD);
        return this;
    }
    
    public Attachment withMethodAnnotations(Collection<Annotation> methodAnnotations) {
        this.methodAnnotations = new ArrayList<>(methodAnnotations);
        return this;
    }
    
    public Attachment withMethodParameterCount(int methodParametersCount) {
        this.methodParametersCount = methodParametersCount;
        return this;
    }
    
    public Attachment withMethodParameters(Map<String, Class<?>> methodParameters) {
        this.methodParameters = Objects.requireNonNull(methodParameters, NotNull.METHOD_PARAMETERS);
        return this;
    }

    public Attachment withRequestFilter(boolean hasRequestFilter) {
        this.requestFilter = hasRequestFilter;
        return this;
    }

    public Attachment withRequestParameter(Map<String, String> requestParameter) {
        this.requestParameter = Objects.requireNonNull(requestParameter, NotNull.REQUEST_PARAMETER);
        return this;
    }
    
    public Attachment withTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = Objects.requireNonNull(templateEngine, NotNull.TEMPLATE_ENGINE);
        return this;
    }

    public Attachment withAuthentication(boolean authentication) {
        this.requiresAuthentication = authentication;
        return this;
    }
}