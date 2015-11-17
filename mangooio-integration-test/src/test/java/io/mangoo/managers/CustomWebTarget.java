package io.mangoo.managers;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 * 
 * @author svenkubiak
 *
 */
public class CustomWebTarget implements WebTarget {
    private WebTarget base;
    private Cookie cookie;

    public CustomWebTarget(WebTarget webTarget, Cookie cookie) {
        this.base = webTarget;
        this.cookie = cookie;
    }

    public Builder request() {
        return base.request().cookie(cookie);
    }

    public Builder request(String... params) {
        return base.request(params).cookie(cookie);
    }

    public Builder request(MediaType... params) {
        return base.request(params).cookie(cookie);
    }

    public Configuration getConfiguration() {
        return base.getConfiguration();
    }

    public URI getUri() {
        return base.getUri();
    }

    public UriBuilder getUriBuilder() {
        return base.getUriBuilder();
    }

    public WebTarget path(String paramString) {
        return base.path(paramString);
    }

    public WebTarget matrixParam(String paramString, Object... params) {
        return base.matrixParam(paramString, params);
    }

    public WebTarget property(String paramString, Object paramObject) {
        return base.property(paramString, paramObject);
    }

    public WebTarget queryParam(String paramString, Object... params) {
        return base.queryParam(paramString, params);
    }

    public WebTarget register(Class<?> paramClass, Class<?>... params) {
        return base.register(paramClass, params);
    }

    public WebTarget register(Class<?> paramClass, int paramInt) {
        return base.register(paramClass, paramInt);
    }

    public WebTarget register(Class<?> paramClass, Map<Class<?>, Integer> paramMap) {
        return base.register(paramClass, paramMap);
    }

    public WebTarget register(Class<?> paramClass) {
        return base.register(paramClass);
    }

    public WebTarget register(Object paramObject, Class<?>... params) {
        return base.register(paramObject, params);
    }

    public WebTarget register(Object paramObject, int paramInt) {
        return base.register(paramObject, paramInt);
    }

    public WebTarget register(Object paramObject, Map<Class<?>, Integer> paramMap) {
        return base.register(paramObject, paramMap);
    }

    public WebTarget register(Object paramObject) {
        return base.register(paramObject);
    }

    public WebTarget resolveTemplate(String paramString, Object paramObject) {
        return base.resolveTemplate(paramString, paramObject);
    }

    public WebTarget resolveTemplate(String paramString, Object paramObject, boolean paramBoolean) {
        return base.resolveTemplate(paramString, paramObject, paramBoolean);
    }

    public WebTarget resolveTemplateFromEncoded(String paramString, Object paramObject) {
        return base.resolveTemplateFromEncoded(paramString, paramObject);
    }

    public WebTarget resolveTemplates(Map<String, Object> paramMap) {
        return base.resolveTemplates(paramMap);
    }

    public WebTarget resolveTemplates(Map<String, Object> paramMap, boolean paramBoolean) {
        return base.resolveTemplates(paramMap, paramBoolean);
    }

    public WebTarget resolveTemplatesFromEncoded(Map<String, Object> paramMap) {
        return base.resolveTemplatesFromEncoded(paramMap);
    }
}