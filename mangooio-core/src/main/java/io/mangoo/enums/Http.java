package io.mangoo.enums;

import io.undertow.util.HttpString;

/**
 * 
 * @author svenkubiak
 *
 */
public enum Http {
    DELETE("delete"),
    GET("get"),
    HEAD("head"),
    OPTIONS("options"),
    PATCH("patch"),
    POST("post"),
    PUT("put");

    private final String value;

    Http (String value) {
        this.value = value;
    }

    public HttpString toHttpString() {
        return new HttpString(this.value);
    }
    
    @Override
    public String toString() {
        return this.value;
    }
}