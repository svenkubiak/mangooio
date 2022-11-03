package io.mangoo.enums;

public enum CacheName {
    APPLICATION("mangooio-application-cache"),
    AUTH("mangooio-auth-cache"),
    RESPONSE("mangooio-response-cache");

    private final String value;

    CacheName (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}