package io.mangoo.routing;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import io.mangoo.authentication.Authentication;
import io.mangoo.configuration.Config;
import io.mangoo.crypto.Crypto;
import io.mangoo.i18n.Messages;
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
public class RequestAttachment {
    private int methodParametersCount;
    private Class<?> controllerClass;
    private String controllerMethodName;
    private Object controllerInstance;
    private Map<String, Class<?>> methodParameters;
    private String controllerClassName;
    private String body;
    private Config config;
    private Method method;
    private Authentication authentication;
    private Session session;
    private Flash flash;
    private Form form;
    private Request request;
    private Response response;
    private Map<String, String> requestParameter;
    private Crypto crypto;
    private Messages messages;
    private TemplateEngine templateEngine;
    private boolean hasRequestFilter;

    public static RequestAttachment build() {
        return new RequestAttachment();
    }

    public RequestAttachment withControllerClass(Class<?> controllerClass) {
        this.controllerClass = Objects.requireNonNull(controllerClass, "controllerClass can not be null");
        return this;
    }

    public RequestAttachment withControllerClassName(String controllerClassName) {
        this.controllerClassName = Objects.requireNonNull(controllerClassName, "controllerClassName can not be null");
        return this;
    }

    public RequestAttachment withControllerMethodName(String controllerMethodName) {
        this.controllerMethodName = Objects.requireNonNull(controllerMethodName, "controllerMethodName can not be null");
        return this;
    }

    public RequestAttachment withConfig(Config config) {
        this.config = Objects.requireNonNull(config, "config can not be null");
        return this;
    }

    public RequestAttachment withControllerInstance(Object controllerInstance) {
        this.controllerInstance = Objects.requireNonNull(controllerInstance, "controllerInstance can no be null");
        return this;
    }

    public RequestAttachment withCrypto(Crypto crypto) {
        this.crypto = Objects.requireNonNull(crypto, "crypto can no be null");
        return this;
    }

    public RequestAttachment withMethodParameters(Map<String, Class<?>> methodParameters) {
        this.methodParameters = Objects.requireNonNull(methodParameters, "methodParameters can no be null");
        return this;
    }

    public RequestAttachment withMessages(Messages messages) {
        this.messages = Objects.requireNonNull(messages, "messages can no be null");
        return this;
    }

    public RequestAttachment withTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = Objects.requireNonNull(templateEngine, "templateEngine can no be null");
        return this;
    }

    public RequestAttachment withRequestParameter(Map<String, String> requestParameter) {
        this.requestParameter = Objects.requireNonNull(requestParameter, "requestParameter can no be null");
        return this;
    }

    public RequestAttachment withMethod(Method method) {
        this.method = Objects.requireNonNull(method, "method can no be null");
        return this;
    }

    public RequestAttachment withMethodParameterCount(int methodParametersCount) {
        this.methodParametersCount = methodParametersCount;
        return this;
    }

    public RequestAttachment withRequestFilter(boolean hasRequestFilter) {
        this.hasRequestFilter = hasRequestFilter;
        return this;
    }

    public Config getConfig() {
        return this.config;
    }

    public Messages getMessages() {
        return this.messages;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setFlash(Flash flash) {
        this.flash = flash;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public Session getSession() {
        return this.session;
    }

    public Flash getFlash() {
        return this.flash;
    }

    public Authentication getAuthentication() {
        return this.authentication;
    }

    public Crypto getCrypto() {
        return this.crypto;
    }

    public boolean hasRequestFilter() {
        return this.hasRequestFilter;
    }

    public Method getMethod() {
        return this.method;
    }

    public Class<?> getControllerClass() {
        return this.controllerClass;
    }

    public Request getRequest() {
        return this.request;
    }

    public Map<String, Class<?>> getMethodParameters() {
        return this.methodParameters;
    }

    public Object getControllerInstance() {
        return this.controllerInstance;
    }

    public TemplateEngine getTemplateEngine() {
        return this.templateEngine;
    }

    public Form getForm() {
        return this.form;
    }

    public String getControllerClassName() {
        return this.controllerClassName;
    }

    public String getControllerMethodName() {
        return this.controllerMethodName;
    }

    public int getMethodParametersCount() {
        return this.methodParametersCount;
    }

    public  Map<String, String> getRequestParameter() {
        return this.requestParameter;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return this.response;
    }
}