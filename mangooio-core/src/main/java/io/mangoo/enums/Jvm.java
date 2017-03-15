package io.mangoo.enums;

/**
 * Key strings for reading JVM properties
 *
 * @author svenkubiak
 *
 */
public enum Jvm {
    APPLICATION_CONFIG("application.config"),
    APPLICATION_MODE("application.mode"),
    APPLICATION_LOG("application.log"),
    HTTP_HOST("http.host"),
    HTTP_PORT("http.port"),
    AJP_HOST("ajp.host"),
    AJP_PORT("ajp.port"),;

    private final String value;

    Jvm (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}